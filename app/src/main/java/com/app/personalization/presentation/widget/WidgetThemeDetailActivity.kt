package com.app.personalization.presentation.widget

import android.app.AlertDialog
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.presentation.widget.WidgetTheme
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.databinding.ActivityWidgetThemeDetailBinding
import com.app.personalization.presentation.widget.ThemeIconItem
import com.app.personalization.presentation.widget.Widget2x2Provider
import com.app.personalization.presentation.widget.Widget4x2Provider
import com.app.personalization.presentation.widget.Widget4x4Provider
import com.app.personalization.presentation.customviews.RemoteImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetThemeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetThemeDetailBinding
    private lateinit var theme: WidgetTheme
    private val iconIds = listOf(
        "phone", "phonebook", "calendar", "weather", "camera", "calculator",
        "setting", "gallery", "chrome", "facebook", "tiktok", "instagram",
        "twitch", "binance", "healthy", "record"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetThemeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        theme = intent.getSerializableExtra("widget_theme") as? WidgetTheme
            ?: WidgetTheme("widget_theme_category_Aesthetic_theme_1", "Theme 1", "category/Aesthetic/theme_1")

        initViews()
    }

    private fun initViews() {
        binding.tvTitle.text = theme.name
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 1. Load Background Theme Preview (Composite Mockup)
        val previewUrl = com.app.personalization.data.CdnPathResolver.getThemePreviewUrl(theme.folder)
        Glide.with(this)
            .load(previewUrl)
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivWallpaper)

        val wallpaperUrl = "${ResourceConfig.S3_URL}/themes/${theme.folder}/wallpapers/bg_wallpaper.png"

        // 2. Load Widget Preview Card
        val widgetBgUrl = "${ResourceConfig.S3_URL}/themes/${theme.folder}/widgets/bg_medium.png"
        Glide.with(this)
            .load(widgetBgUrl)
            .placeholder(R.drawable.bg_default_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivWidgetBg)

        // 3. Load Horizontal Icon Preview List
        binding.rvIcons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvIcons.adapter = HorizontalIconsAdapter(this, theme.folder, iconIds)

        // Action click handlers
        binding.btnWallpaper.setOnClickListener {
            showWallpaperChooserDialog(wallpaperUrl)
        }

        binding.btnIcons.setOnClickListener {
            showIconsSelectionDialog()
        }

        binding.btnWidget.setOnClickListener {
            showWidgetChooserDialog()
        }

        binding.cardWidget.setOnClickListener {
            showWidgetChooserDialog()
        }
    }

    // --- Wallpaper Flow ---
    private fun showWallpaperChooserDialog(url: String) {
        val options = arrayOf("Home Screen", "Lock Screen", "Both Screens")
        AlertDialog.Builder(this)
            .setTitle("Apply Wallpaper")
            .setItems(options) { _, which ->
                applyWallpaper(url, which)
            }
            .show()
    }

    private fun applyWallpaper(url: String, type: Int) {
        Toast.makeText(this, "Downloading wallpaper...", Toast.LENGTH_SHORT).show()
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val wm = WallpaperManager.getInstance(this@WidgetThemeDetailActivity)
                            when (type) {
                                0 -> wm.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                                1 -> wm.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                                2 -> {
                                    wm.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                                    wm.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WidgetThemeDetailActivity, "Wallpaper applied successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WidgetThemeDetailActivity, "Failed to apply wallpaper.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    // --- Icon Pack Selection Dialog ---
    private fun showIconsSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_selection, null)
        val rvItems = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        rvItems.layoutManager = LinearLayoutManager(this)

        val appMappings = getAppMappings()
        val adapter = IconSelectionAdapter(this, theme.folder, appMappings)
        rvItems.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Select Icons to Install")
            .setView(dialogView)
            .setPositiveButton("Install Pinned Shortcuts") { _, _ ->
                installSelectedShortcuts(adapter.getSelectedItems())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun installSelectedShortcuts(items: List<IconSelectionItem>) {
        if (items.isEmpty()) {
            Toast.makeText(this, "No icons selected", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Adding shortcuts to Home screen...", Toast.LENGTH_SHORT).show()
        for (item in items) {
            Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        try {
                            val pm = packageManager
                            val intent = pm.getLaunchIntentForPackage(item.packageName) ?: Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                setPackage(item.packageName)
                            }
                            
                            val shortcut = ShortcutInfoCompat.Builder(this@WidgetThemeDetailActivity, item.id)
                                .setShortLabel(item.customName)
                                .setIcon(IconCompat.createWithBitmap(resource))
                                .setIntent(intent)
                                .build()

                            ShortcutManagerCompat.requestPinShortcut(this@WidgetThemeDetailActivity, shortcut, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    // --- Widget Flow ---
    private fun showWidgetChooserDialog() {
        val options = arrayOf("Clock Widget 2x2", "Weather Widget 4x2", "Calendar Widget 4x4")
        AlertDialog.Builder(this)
            .setTitle("Add Widget to Home Screen")
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> "clock"
                    1 -> "weather"
                    else -> "calendar"
                }
                val size = when (which) {
                    0 -> "2x2"
                    1 -> "4x2"
                    else -> "4x4"
                }
                
                val typeFolder = when (type) {
                    "clock" -> "clocks"
                    "calendar" -> "today"
                    "weather" -> "weather"
                    else -> type
                }
                val isMedium = size == "4x2"
                val previewFileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
                val previewUrl = "${ResourceConfig.S3_URL}/themes/${theme.folder}/widgets/$typeFolder/$previewFileName"

                val keyboardTheme = com.app.personalization.data.database.entity.KeyboardTheme(
                    id = theme.id,
                    name = theme.name,
                    path = theme.folder
                )

                val sheet = SelectWidgetBottomSheet()
                sheet.setParams(
                    theme = keyboardTheme,
                    widgetType = type,
                    size = size,
                    previewUrl = previewUrl
                )
                sheet.show(supportFragmentManager, "select_widget")
            }
            .show()
    }

    private fun getAppMappings(): List<IconSelectionItem> {
        val mappings = listOf(
            Pair("phone", "com.android.server.telecom"),
            Pair("phonebook", "com.android.contacts"),
            Pair("calendar", "com.android.calendar"),
            Pair("weather", "com.sec.android.app.popupcalculator"), // Calculator or fallback
            Pair("camera", "com.android.camera"),
            Pair("calculator", "com.google.android.calculator"),
            Pair("setting", "com.android.settings"),
            Pair("gallery", "com.android.gallery3d"),
            Pair("chrome", "com.android.chrome"),
            Pair("facebook", "com.facebook.katana"),
            Pair("tiktok", "com.zhiliaoapp.musically"),
            Pair("instagram", "com.instagram.android"),
            Pair("twitch", "tv.twitch.android.app"),
            Pair("binance", "com.binance.dev"),
            Pair("healthy", "com.google.android.apps.fitness"),
            Pair("record", "com.android.soundrecorder")
        )

        return mappings.map { (id, pkg) ->
            val imageUrl = "${ResourceConfig.S3_URL}/${theme.folder}/icons/ic_$id.png"
            val appName = id.replaceFirstChar { it.uppercase() }
            IconSelectionItem(id, appName, appName, pkg, imageUrl, true)
        }
    }

    // --- Inner Horizontal Icons Adapter ---
    class HorizontalIconsAdapter(
        private val context: Context,
        private val folder: String,
        private val iconIds: List<String>
    ) : RecyclerView.Adapter<HorizontalIconsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_widget_theme_detail_icon, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iconId = iconIds[position]
            val iconUrl = "${ResourceConfig.S3_URL}/$folder/icons/ic_$iconId.png"
            Glide.with(context)
                .load(iconUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivIcon)
        }

        override fun getItemCount(): Int = iconIds.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: RemoteImageView = view.findViewById(R.id.ivIcon)
        }
    }

    // --- Inner Selection Dialog Classes ---
    data class IconSelectionItem(
        val id: String,
        val defaultName: String,
        var customName: String,
        val packageName: String,
        val imageUrl: String,
        var isSelected: Boolean
    )

    class IconSelectionAdapter(
        private val context: Context,
        private val folder: String,
        private val list: List<IconSelectionItem>
    ) : RecyclerView.Adapter<IconSelectionAdapter.ViewHolder>() {

        fun getSelectedItems() = list.filter { it.isSelected }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.item_dialog_icon_select, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.cbSelect.isChecked = item.isSelected
            holder.etName.setText(item.customName)

            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivIcon)

            holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
            }

            holder.etName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    item.customName = holder.etName.text.toString()
                }
            }
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
            val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
            val etName: EditText = view.findViewById(R.id.etName)
        }
    }
}
