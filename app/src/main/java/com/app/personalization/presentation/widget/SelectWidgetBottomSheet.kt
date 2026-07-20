package com.app.personalization.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.app.personalization.R
import com.app.personalization.data.EventBus
import com.app.personalization.data.Subscribe
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentSelectWidgetBottomSheetBinding
import com.app.personalization.presentation.widget.Widget2x2Provider
import com.app.personalization.presentation.widget.Widget4x2Provider
import com.app.personalization.presentation.widget.Widget4x4Provider
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.personalization.presentation.widget.event.WidgetAddSucceedEvent
import com.app.personalization.data.database.entity.WidgetItem
import com.app.personalization.data.database.entity.WidgetSize
import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.di.ServiceLocator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.app.personalization.cscthemeapp.widget.ui.widget.widget.PreviewWidgetView
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Color
import android.graphics.Typeface
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectWidgetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectWidgetBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var theme: KeyboardTheme
    private lateinit var widgetType: String // "clock", "weather", "calendar"
    private lateinit var size: String // "2x2", "4x2", "4x4"
    private var previewUrl: String? = null
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            continueDownloadAndPin()
        } else {
            Toast.makeText(context, "Location permission is required for weather widget.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setParams(theme: KeyboardTheme, widgetType: String, size: String, previewUrl: String? = null, widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID) {
        this.theme = theme
        this.widgetType = widgetType
        this.size = size
        this.previewUrl = previewUrl
        this.widgetId = widgetId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectWidgetBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        binding.adContainer.visibility = View.GONE
        binding.llAction.visibility = View.GONE
        binding.clInstall.visibility = View.VISIBLE
        binding.tvDownload.text = if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "Add to Home" else "Apply Widget"

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.clInstall.setOnClickListener {
            downloadAndPinWidget()
        }

        // Setup RecyclerView preview
        val resolvedPreview = previewUrl ?: run {
            val isMedium = size.lowercase() == "4x2" || size.lowercase() == "medium"
            val fileName = if (isMedium) {
                "bg_preview_medium.png"
            } else {
                "bg_preview_large.png"
            }
            val typeFolder = when (widgetType.lowercase()) {
                "clock" -> "clocks"
                "calendar", "date" -> "today"
                "weather" -> "weather"
                "image" -> "image"
                else -> widgetType.lowercase()
            }
            "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/${theme.path}/widgets/$typeFolder/$fileName"
        }

        val density = resources.displayMetrics.density
        val targetWidth = when (size.lowercase()) {
            "2x2", "small" -> (180 * density).toInt()
            "4x2", "medium" -> (320 * density).toInt()
            "4x4", "large" -> (280 * density).toInt()
            "2x4" -> (160 * density).toInt()
            else -> (180 * density).toInt()
        }
        val targetHeight = when (size.lowercase()) {
            "2x2", "small" -> (180 * density).toInt()
            "4x2", "medium" -> (160 * density).toInt()
            "4x4", "large" -> (280 * density).toInt()
            "2x4" -> (320 * density).toInt()
            else -> (180 * density).toInt()
        }

        binding.recyclerView.layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                lp?.let {
                    it.width = targetWidth
                    it.height = targetHeight
                }
                return true
            }
        }
        binding.recyclerView.adapter = SelectWidgetAdapter(resolvedPreview)
        val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper)
    }
 
    private inner class SelectWidgetAdapter(private val imageUrl: String) : RecyclerView.Adapter<SelectWidgetAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val cardView: CardView = view.findViewById(R.id.cardView)
            val previewView: PreviewWidgetView = view.findViewById(R.id.previewView)
        }
 
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_widget_preview_layout, parent, false)
            val density = parent.context.resources.displayMetrics.density
            val targetWidth = when (size.lowercase()) {
                "2x2", "small" -> (180 * density).toInt()
                "4x2", "medium" -> (320 * density).toInt()
                "4x4", "large" -> (280 * density).toInt()
                "2x4" -> (160 * density).toInt()
                else -> (180 * density).toInt()
            }
            val targetHeight = when (size.lowercase()) {
                "2x2", "small" -> (180 * density).toInt()
                "4x2", "medium" -> (160 * density).toInt()
                "4x4", "large" -> (280 * density).toInt()
                "2x4" -> (320 * density).toInt()
                else -> (180 * density).toInt()
            }
 
            view.layoutParams = ViewGroup.LayoutParams(
                targetWidth,
                targetHeight
            )
 
            val cardView = view.findViewById<View>(R.id.cardView)
            val lp = cardView.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            cardView.layoutParams = lp
 
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            
            val widgetSize = when (size.lowercase()) {
                "2x2", "small" -> WidgetSize.SMALL
                "4x2", "medium" -> WidgetSize.MEDIUM
                "4x4", "large" -> WidgetSize.LARGE
                else -> WidgetSize.SMALL
            }

            val layoutRes = when (widgetSize) {
                WidgetSize.SMALL -> R.layout.widget_layout_2x2
                WidgetSize.MEDIUM -> R.layout.widget_layout_4x2
                WidgetSize.LARGE -> R.layout.widget_layout_4x4
            }

            val widgetItem = WidgetItem(
                id = theme.id,
                themeFolder = theme.path,
                name = theme.name,
                widgetType = widgetType,
                size = size,
                isFree = true,
                isFavorite = false
            )

            val placeholderBmp = WidgetRenderHelper.getSnapshotImage(
                context = context,
                layoutId = layoutRes,
                widgetSize = widgetSize,
                widgetItem = widgetItem,
                widgetId = widgetId,
                preloadedBackground = null
            )
            if (placeholderBmp != null) {
                holder.ivPreview.visibility = View.VISIBLE
                holder.cardView.visibility = View.GONE
                holder.ivPreview.setImageBitmap(placeholderBmp)
            } else {
                holder.ivPreview.visibility = View.GONE
                holder.cardView.visibility = View.VISIBLE
            }

            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val realBmp = WidgetRenderHelper.getSnapshotImage(
                            context = context,
                            layoutId = layoutRes,
                            widgetSize = widgetSize,
                            widgetItem = widgetItem,
                            widgetId = widgetId,
                            preloadedBackground = resource
                        )
                        if (realBmp != null) {
                            holder.ivPreview.visibility = View.VISIBLE
                            holder.cardView.visibility = View.GONE
                            holder.ivPreview.setImageBitmap(realBmp)
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        private fun drawRotatedHand(canvas: Canvas, handBmp: Bitmap?, angle: Float, size: Int) {
            if (handBmp == null) return
            val matrix = Matrix()
            val scale = size.toFloat() / handBmp.height.toFloat()
            matrix.postScale(scale, scale)
            val dx = (size / 2f) - (handBmp.width / 2f * scale)
            val dy = (size / 2f) - (handBmp.height / 2f * scale)
            matrix.postTranslate(dx, dy)
            matrix.postRotate(angle, size / 2f, size / 2f)
            canvas.drawBitmap(handBmp, matrix, null)
        }

        override fun getItemCount(): Int = 1
    }

    private fun downloadAndPinWidget() {
        val activity = activity ?: return
        if (widgetType.lowercase().contains("weather")) {
            val hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
                return
            }
        }
        continueDownloadAndPin()
    }

    private fun continueDownloadAndPin() {
        val activity = requireActivity()
        val fileName = when (size) {
            "2x2" -> "bg_medium.png"
            "4x2" -> "bg_medium.png"
            "4x4" -> "bg_large.png"
            else -> "bg_medium.png"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val folder = try {
                val uuid = java.util.UUID.fromString(theme.id)
                val db = com.app.personalization.data.database.ThemeDatabase.getDatabase(activity)
                val diyWidgets = db.widgetDao().getWidgetsByTheme(uuid)
                val matchingWidget = diyWidgets.firstOrNull { it.type.lowercase() == widgetType.lowercase() }
                matchingWidget?.templatePath ?: com.app.personalization.data.ResourceConfig.getThemeFolderByPath(activity, theme.path)
            } catch (e: Exception) {
                com.app.personalization.data.ResourceConfig.getThemeFolderByPath(activity, theme.path)
            }

            val cdnUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$folder/widgets/$fileName"

            withContext(Dispatchers.Main) {
                val downloadDialog = DownloadDialogFragment()
                downloadDialog.setParams(cdnUrl, object : DownloadDialogFragment.DownloadCallback {
                    override fun onDownloadComplete(bitmap: Bitmap) {
                        saveWidgetBackground(activity, bitmap)
                        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                            requestPinWidget(activity)
                        } else {
                            applyToExistingWidget(activity, bitmap)
                        }
                    }

                    override fun onDownloadFailed() {
                        Toast.makeText(activity, "Failed to download widget assets", Toast.LENGTH_SHORT).show()
                    }
                })
                downloadDialog.show(parentFragmentManager, "download")
            }
        }
    }

    private fun saveWidgetBackground(context: Context, bitmap: Bitmap) {
        try {
            val fileName = "widget_bg_${theme.id}_${widgetType}_$size.png"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("bg_path_${theme.id}_${widgetType}_$size", fileName)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPinWidget(context: Context) {
        val widgetSize = when (size.lowercase()) {
            "2x2", "small" -> WidgetSize.SMALL
            "4x2", "medium" -> WidgetSize.MEDIUM
            "4x4", "large" -> WidgetSize.LARGE
            else -> WidgetSize.SMALL
        }

        val providerClass = when (widgetSize) {
            WidgetSize.SMALL -> Widget2x2Provider::class
            WidgetSize.MEDIUM -> Widget4x2Provider::class
            WidgetSize.LARGE -> Widget4x4Provider::class
        }

        val widgetItem = WidgetItem(
            id = theme.id,
            themeFolder = theme.path,
            name = theme.name,
            widgetType = widgetType,
            size = size,
            isFree = true,
            isFavorite = false
        )

        context.addWidget(providerClass, widgetItem, isMineOrCustom = false)
    }

    private fun applyToExistingWidget(activity: FragmentActivity, bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fileName = "widget_bg_${theme.id}_${widgetType}_$size.png"
                val config = WidgetConfig(
                    widgetId = widgetId,
                    bgType = "IMAGE",
                    solidColor = 0,
                    imageUri = Uri.fromFile(activity.getFileStreamPath(fileName)).toString(),
                    textColor = android.graphics.Color.WHITE,
                    fontStyle = "normal",
                    gradientStartColor = 0,
                    gradientEndColor = 0
                )

                activity.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("widget_type_$widgetId", widgetType)
                    .apply()
                ServiceLocator.getWidgetConfigDao(activity).saveConfig(config)

                withContext(Dispatchers.Main) {
                    val appWidgetManager = AppWidgetManager.getInstance(activity)
                    Widget2x2Provider().updateWidget(activity, appWidgetManager, widgetId)
                    Widget4x2Provider().updateWidget(activity, appWidgetManager, widgetId)
                    Widget4x4Provider().updateWidget(activity, appWidgetManager, widgetId)

                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    }
                    activity.setResult(Activity.RESULT_OK, resultValue)
                    Toast.makeText(activity, "Widget applied successfully!", Toast.LENGTH_SHORT).show()
                    dismissAllowingStateLoss()
                    activity.finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Failed to apply widget style", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Subscribe
    fun onWidgetAddSucceed(event: WidgetAddSucceedEvent) {
        val activity = activity as? FragmentActivity
        dismissAllowingStateLoss()
        if (activity != null) {
            SetupSucceedDialogFragment().show(activity.supportFragmentManager, "success")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
    }
}
