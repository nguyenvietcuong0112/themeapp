package com.app.personalization.feature_widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.core.data.EventBus
import com.app.personalization.core.data.ResourceConfig
import com.app.personalization.core.data.Subscribe
import com.app.personalization.core.di.ServiceLocator
import com.app.personalization.core.ui.PreviewWidgetView
import com.app.personalization.databinding.FragmentSelectWidgetBottomSheetBinding
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.feature_widget.data.entity.WidgetConfig
import com.app.personalization.feature_widget.data.entity.WidgetItem
import com.app.personalization.feature_widget.data.entity.WidgetSize
import com.app.personalization.feature_widget.event.WidgetAddSucceedEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectWidgetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectWidgetBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var theme: KeyboardTheme
    private var widgetList: MutableList<ThemeWidgetItem> = mutableListOf()
    private var currentIndex: Int = 0
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            continueDownloadAndPin()
        } else {
            Toast.makeText(context, "Location permission is required for weather widget.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setParams(
        theme: KeyboardTheme,
        widgetType: String,
        size: String,
        previewUrl: String? = null,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    ) {
        this.theme = theme
        this.widgetId = widgetId
        val resolvedPreview = previewUrl ?: run {
            val isMedium = size.lowercase() == "4x2" || size.lowercase() == "medium"
            val fileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
            val typeFolder = when (widgetType.lowercase()) {
                "clock" -> "clocks"
                "calendar", "date" -> "today"
                "weather" -> "weather"
                "image" -> "image"
                else -> widgetType.lowercase()
            }
            "${ResourceConfig.ASSET_BASE_URL}/assets_theme/${theme.path}/widgets/$typeFolder/$fileName"
        }
        val providerClass = if (size == "4x2") Widget4x2Provider::class.java else Widget2x2Provider::class.java
        this.widgetList = mutableListOf(
            ThemeWidgetItem(
                id = "${theme.id}_widget_${widgetType}_$size",
                name = "$widgetType $size",
                size = size,
                providerClass = providerClass,
                previewUrl = resolvedPreview,
                isSelected = true
            )
        )
        this.currentIndex = 0
    }

    fun setParams(
        theme: KeyboardTheme,
        widgetList: List<ThemeWidgetItem>,
        initialIndex: Int = 0,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    ) {
        this.theme = theme
        this.widgetList = widgetList.toMutableList()
        this.currentIndex = initialIndex.coerceIn(0, (widgetList.size - 1).coerceAtLeast(0))
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

        binding.clInstall.visibility = View.VISIBLE
        binding.tvDownload.text = if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) "Add to Home" else "Apply Widget"

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.clInstall.setOnClickListener {
            downloadAndPinWidget()
        }

        setupCarousel()
    }

    private fun setupCarousel() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = SelectWidgetAdapter(widgetList)
        binding.recyclerView.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        binding.indicator.attachToRecyclerView(binding.recyclerView, snapHelper)
        adapter.registerAdapterDataObserver(binding.indicator.adapterDataObserver)

        if (currentIndex in widgetList.indices) {
            binding.recyclerView.scrollToPosition(currentIndex)
            binding.recyclerView.post {
                snapHelper.findSnapView(layoutManager)?.let { snapView ->
                    val pos = layoutManager.getPosition(snapView)
                    if (pos in widgetList.indices) {
                        currentIndex = pos
                    }
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val pos = layoutManager.getPosition(snapView)
                    if (pos in widgetList.indices) {
                        currentIndex = pos
                    }
                }
            }
        })
    }

    private inner class SelectWidgetAdapter(
        private val list: List<ThemeWidgetItem>
    ) : RecyclerView.Adapter<SelectWidgetAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val cardView: CardView = view.findViewById(R.id.cardView)
            val previewView: PreviewWidgetView = view.findViewById(R.id.previewView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_preview_layout, parent, false
            )
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val density = context.resources.displayMetrics.density

            val typeId = item.id.substringAfter("_widget_").substringBefore("_2x2").substringBefore("_4x2")
            val currentType = when (typeId) {
                "today", "today2" -> "calendar"
                "clocks" -> "clock"
                "weather" -> "weather"
                "image" -> "image"
                else -> "clock"
            }
            val currentSize = item.size

            val widgetSize = when (currentSize.lowercase()) {
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

            val targetWidth = if (currentSize == "4x2") (320 * density).toInt() else (180 * density).toInt()
            val targetHeight = if (currentSize == "4x2") (160 * density).toInt() else (180 * density).toInt()

            holder.cardView.layoutParams = holder.cardView.layoutParams.apply {
                width = targetWidth
                height = targetHeight
            }
            holder.ivPreview.layoutParams = holder.ivPreview.layoutParams.apply {
                width = targetWidth
                height = targetHeight
            }

            val widgetItem = WidgetItem(
                id = theme.id,
                themeFolder = theme.path,
                name = theme.name,
                widgetType = currentType,
                size = currentSize,
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
                .load(item.previewUrl)
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

        override fun getItemCount(): Int = list.size
    }

    private fun getCurrentWidgetInfo(): Pair<String, String> {
        val item = widgetList.getOrNull(currentIndex)
        val currentSize = item?.size ?: "2x2"
        val currentType = item?.let {
            val typeId = it.id.substringAfter("_widget_").substringBefore("_2x2").substringBefore("_4x2")
            when (typeId) {
                "today", "today2" -> "calendar"
                "clocks" -> "clock"
                "weather" -> "weather"
                "image" -> "image"
                else -> "clock"
            }
        } ?: "clock"
        return Pair(currentType, currentSize)
    }

    private fun downloadAndPinWidget() {
        val activity = activity ?: return
        val (currentType, _) = getCurrentWidgetInfo()
        if (currentType.lowercase().contains("weather")) {
            val hasFine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return
            }
        }
        continueDownloadAndPin()
    }

    private fun continueDownloadAndPin() {
        val activity = requireActivity()
        val (currentType, currentSize) = getCurrentWidgetInfo()

        val fileName = when (currentSize) {
            "2x2" -> "bg_medium.png"
            "4x2" -> "bg_medium.png"
            "4x4" -> "bg_large.png"
            else -> "bg_medium.png"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val folder = try {
                val uuid = java.util.UUID.fromString(theme.id)
                val db = com.app.personalization.feature_theme.data.ThemeDatabase.getDatabase(activity)
                val diyWidgets = db.widgetDao().getWidgetsByTheme(uuid)
                val matchingWidget = diyWidgets.firstOrNull { it.type.lowercase() == currentType.lowercase() }
                matchingWidget?.templatePath ?: ResourceConfig.getThemeFolderByPath(activity, theme.path)
            } catch (e: Exception) {
                ResourceConfig.getThemeFolderByPath(activity, theme.path)
            }

            val cdnUrl = if (folder.startsWith("assets_collection/")) {
                val clean = folder.removePrefix("assets_collection/").removePrefix("widget/")
                "${ResourceConfig.ASSET_BASE_URL}/assets_collection/widget/$clean/$fileName"
            } else {
                "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$folder/widgets/$fileName"
            }

            withContext(Dispatchers.Main) {
                val downloadDialog = DownloadDialogFragment()
                downloadDialog.setParams(cdnUrl, object : DownloadDialogFragment.DownloadCallback {
                    override fun onDownloadComplete(bitmap: Bitmap) {
                        saveWidgetBackground(activity, bitmap, currentType, currentSize)
                        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                            dismissAllowingStateLoss()
                            requestPinWidget(activity, currentType, currentSize)
                        } else {
                            applyToExistingWidget(activity, bitmap, currentType, currentSize)
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

    private fun saveWidgetBackground(context: Context, bitmap: Bitmap, currentType: String, currentSize: String) {
        try {
            val cleanId = theme.id.replace('/', '_').replace('\\', '_')
            val fileName = "widget_bg_${cleanId}_${currentType}_$currentSize.png"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("bg_path_${cleanId}_${currentType}_$currentSize", fileName)
                .apply()

            val repo = com.app.personalization.feature_collections.data.CollectionRepository(context)
            lifecycleScope.launch(Dispatchers.IO) {
                val typeFolder = when (currentType.lowercase()) {
                    "clock" -> "clocks"
                    "calendar", "date" -> "today"
                    "weather" -> "weather"
                    "image" -> "image"
                    else -> currentType.lowercase()
                }
                val previewFileName = if (currentSize.lowercase() == "4x2") "bg_preview_medium.png" else "bg_preview_large.png"
                val resolvedPreview = if (theme.path.startsWith("category/")) {
                    "file:///android_asset/assets_theme/${theme.path}/widgets/$typeFolder/$previewFileName"
                } else {
                    "${ResourceConfig.ASSET_BASE_URL}/assets_theme/${theme.path}/widgets/$typeFolder/$previewFileName"
                }
                repo.markAsDownloaded(
                    id = "widget_${theme.path.replace('/', '_')}_${currentType}_$currentSize",
                    name = "${theme.name} ${currentType.replaceFirstChar { it.uppercase() }}",
                    category = "Widget",
                    targetPath = theme.path,
                    previewPath = resolvedPreview,
                    rawType = currentType,
                    extra = currentSize
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPinWidget(context: Context, currentType: String, currentSize: String) {
        val widgetSize = when (currentSize.lowercase()) {
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
            widgetType = currentType,
            size = currentSize,
            isFree = true,
            isFavorite = false
        )

        context.addWidget(providerClass, widgetItem, isMineOrCustom = false)
    }

    private fun applyToExistingWidget(activity: FragmentActivity, bitmap: Bitmap, currentType: String, currentSize: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cleanId = theme.id.replace('/', '_').replace('\\', '_')
                val fileName = "widget_bg_${cleanId}_${currentType}_$currentSize.png"
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
                    .putString("widget_type_$widgetId", currentType)
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
