package com.app.personalization.presentation.widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.EventBus
import com.app.personalization.data.Subscribe
import com.app.personalization.data.ShortcutEvent
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentAppBottomSheetDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class SelectIconBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAppBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var theme: KeyboardTheme
    private lateinit var selectedIcons: List<ThemeIconItem>
    private val installQueue = ArrayList<ThemeIconItem>()
    private var fallbackJob: Job? = null

    private val shortcutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.app.personalization.SHORTCUT_INSTALLED") {
                EventBus.getDefault().post(ShortcutEvent())
            }
        }
    }

    fun setParams(theme: KeyboardTheme, selectedIcons: List<ThemeIconItem>) {
        this.theme = theme
        this.selectedIcons = selectedIcons
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        
        val filter = IntentFilter("com.app.personalization.SHORTCUT_INSTALLED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(shortcutReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            requireContext().registerReceiver(shortcutReceiver, filter)
        }

        binding.tvTitle.text = "Add Icons to Home Screen"
        binding.tvDone.text = "Add"

        binding.tvCancel.setOnClickListener {
            dismiss()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = SelectedIconsPreviewAdapter(selectedIcons, theme.path)

        binding.tvDone.setOnClickListener {
            binding.tvDone.isEnabled = false
            startInstallation()
        }
    }

    private fun startInstallation() {
        installQueue.clear()
        installQueue.addAll(selectedIcons)
        processNextShortcut()
    }

    private fun processNextShortcut() {
        fallbackJob?.cancel()
        if (installQueue.isNotEmpty()) {
            val nextItem = installQueue.removeAt(0)
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = loadThemeIconBitmap(nextItem)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        addShortcut(requireContext(), nextItem, bitmap)
                        
                        // Fallback timeout of 2 seconds
                        fallbackJob = lifecycleScope.launch {
                            delay(2000)
                            EventBus.getDefault().post(ShortcutEvent())
                        }
                    } else {
                        processNextShortcut()
                    }
                }
            }
        } else {
            dismissAllowingStateLoss()
            val activity = activity
            if (activity != null) {
                SetupSucceedDialogFragment().show(activity.supportFragmentManager, "success")
            }
        }
    }

    @Subscribe
    fun onShortcutInstalled(event: ShortcutEvent) {
        processNextShortcut()
    }

    private fun addShortcut(context: Context, item: ThemeIconItem, bmp: Bitmap) {
        val targetPkg = item.targetPackageName ?: return
        
        val proxyIntent = Intent(context, ChangeIconActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("target_package", targetPkg)
        }

        val roundedBmp = roundBitmap(bmp, 16f)

        val receiverIntent = Intent("com.app.personalization.SHORTCUT_INSTALLED").apply {
            setPackage(context.packageName)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val successCallback = PendingIntent.getBroadcast(
            context,
            item.hashCode(),
            receiverIntent,
            flags
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val shortcutInfo = android.content.pm.ShortcutInfo.Builder(context, targetPkg)
                    .setShortLabel(item.targetAppName ?: item.iconName)
                    .setIcon(android.graphics.drawable.Icon.createWithBitmap(roundedBmp))
                    .setIntent(proxyIntent)
                    .setActivity(android.content.ComponentName(context, ChangeIconActivity::class.java))
                    .build()

                shortcutManager.requestPinShortcut(shortcutInfo, successCallback.intentSender)
            } else {
                EventBus.getDefault().post(ShortcutEvent())
            }
        } else {
            val installIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, proxyIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, item.targetAppName ?: item.iconName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, roundedBmp)
            }
            context.sendBroadcast(installIntent)
            EventBus.getDefault().post(ShortcutEvent())
        }
    }

    private fun loadThemeIconBitmap(item: ThemeIconItem): Bitmap? {
        val context = requireContext()
        val themePath = if (item.assetPath.contains("theme_decorates/")) {
            item.assetPath.substringAfter("theme_decorates/").substringBefore("/key/")
        } else {
            item.assetPath.substringBefore("/key/")
        }
        val cdnUrl = com.app.personalization.data.ResourceConfig.getLauncherIconUrl(context, themePath, item.iconName)

        return try {
            if (theme.rawType == "widget_theme" || themePath.startsWith("theme_")) {
                Glide.with(context)
                    .asBitmap()
                    .load(cdnUrl)
                    .submit()
                    .get()
            } else {
                val inputStream: InputStream = context.assets.open(item.assetPath)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(cdnUrl)
                    .submit()
                    .get()
            } catch (e2: Exception) {
                val size = 96
                val fallback = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(fallback)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4F46E5")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 16f, 16f, paint)
                fallback
            }
        }
    }

    private fun roundBitmap(bitmap: Bitmap, cornerRadiusDp: Float): Bitmap {
        val density = resources.displayMetrics.density
        val pixels = (cornerRadiusDp * density).toInt()
        
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fallbackJob?.cancel()
        EventBus.getDefault().unregister(this)
        try {
            requireContext().unregisterReceiver(shortcutReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        _binding = null
    }

    private class SelectedIconsPreviewAdapter(
        private val list: List<ThemeIconItem>,
        private val themePath: String
    ) : RecyclerView.Adapter<SelectedIconsPreviewAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_theme_detail_icon, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val themeFolder = if (themePath.contains("/")) themePath.substringBefore("/") else themePath
            val cdnUrl = com.app.personalization.data.ResourceConfig.getLauncherIconUrl(context, themeFolder, item.iconName)
            val localKeyPath = "file:///android_asset/theme_decorates/$themeFolder/key/key.png"

            val glideRequest = if (cdnUrl.isNotEmpty()) {
                Glide.with(context).load(cdnUrl)
            } else {
                Glide.with(context).load(localKeyPath)
            }

            glideRequest
                .placeholder(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    Glide.with(context)
                        .load(localKeyPath)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(holder.ivIcon)
        }

        override fun getItemCount(): Int = list.size
    }
}
