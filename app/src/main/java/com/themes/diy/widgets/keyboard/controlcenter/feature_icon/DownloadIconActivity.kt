package com.themes.diy.widgets.keyboard.controlcenter.feature_icon

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.core.data.AppItemData
import com.themes.diy.widgets.keyboard.controlcenter.databinding.ActivityDownloadIconBinding
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.DownloadIconItemAdapter
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.SelectIconBottomSheet
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.ThemeIconItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadIconActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadIconBinding
    private lateinit var theme: KeyboardTheme

    private val iconItems = mutableListOf<ThemeIconItem>()
    private lateinit var adapter: DownloadIconItemAdapter
    
    private var isAllSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadIconBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeId = intent.getStringExtra("theme_id") ?: ""
        val themeName = intent.getStringExtra("theme_name") ?: "Default Theme"
        val themePath = intent.getStringExtra("theme_path") ?: ""
        val themeType = intent.getStringExtra("theme_type") ?: "default"
        theme = KeyboardTheme(id = themeId, name = themeName, path = themePath, rawType = themeType)

        initToolbar()
        
        binding.pbCreate.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    initPresetIcons()
                }
                setupRecyclerView()
                setupHeader()
                setupActions()
                binding.llHeader.visibility = View.VISIBLE
                binding.actionView.root.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DownloadIconActivity, "Error loading preset icons", Toast.LENGTH_SHORT).show()
            } finally {
                binding.pbCreate.visibility = View.GONE
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.titleTextView.text = "Icon Setup"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initPresetIcons() {
        val pm = packageManager
        iconItems.clear()

        val themePath = theme.path
            .removePrefix("file:///android_asset/")
            .removePrefix("file://android_asset/")
            .removePrefix("android_asset/")
            .removePrefix("/")

        val discoveredIconNames = mutableListOf<String>()

        // Check asset directories for icons
        val candidateDirs = listOf(
            "assets_theme/$themePath/icons",
            "assets_theme/category/$themePath/icons",
            "assets_collection/theme/$themePath/icons",
            "assets_collection/icons/$themePath",
            "$themePath/icons",
            themePath
        )

        var matchedDir: String? = null
        for (dir in candidateDirs) {
            try {
                val cleanDir = dir.removePrefix("file:///android_asset/").removePrefix("android_asset/").removePrefix("/")
                val files = assets.list(cleanDir)
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (file.endsWith(".png") || file.endsWith(".webp")) {
                            val iconName = file.substringBeforeLast(".")
                            if (!discoveredIconNames.contains(iconName)) {
                                discoveredIconNames.add(iconName)
                            }
                        }
                    }
                    if (discoveredIconNames.isNotEmpty()) {
                        matchedDir = cleanDir
                        break
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Fallback default list if no asset folder found
        if (discoveredIconNames.isEmpty()) {
            discoveredIconNames.addAll(
                listOf(
                    "ic_calculator", "ic_calendar", "ic_camera", "ic_chrome",
                    "ic_facebook", "ic_gallery", "ic_instagram", "ic_phone",
                    "ic_phonebook", "ic_setting", "ic_tiktok", "ic_weather",
                    "ic_youtube", "ic_gmail", "ic_maps", "ic_spotify",
                    "ic_messenger", "ic_telegram", "ic_twitter", "ic_snapchat",
                    "ic_healthy", "ic_record", "ic_binance", "ic_twitch"
                )
            )
        }

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps = pm.queryIntentActivities(mainIntent, 0)

        for (rawIconName in discoveredIconNames) {
            val normalizedName = rawIconName.removePrefix("ic_").lowercase()
            var targetPkg: String? = null
            var targetAppName: String? = null
            var targetIcon: android.graphics.drawable.Drawable? = null

            // 1. Match known package mappings from AppItemData
            val defaultApp = AppItemData.APPS.find { it.id.equals(normalizedName, ignoreCase = true) }
            if (defaultApp != null) {
                try {
                    val appInfo = pm.getApplicationInfo(defaultApp.packageName, 0)
                    targetPkg = defaultApp.packageName
                    targetAppName = pm.getApplicationLabel(appInfo).toString()
                    targetIcon = pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    // App not installed with this package
                }
            }

            // 2. Match standard system intents (Camera, Phone, Gallery, Settings, Calculator)
            if (targetPkg == null) {
                when (normalizedName) {
                    "phone", "call", "dialer" -> {
                        val intent = Intent(Intent.ACTION_DIAL)
                        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (resolveInfo != null && resolveInfo.activityInfo.packageName != "android") {
                            targetAppName = resolveInfo.loadLabel(pm).toString()
                            targetIcon = resolveInfo.loadIcon(pm)
                            targetPkg = resolveInfo.activityInfo.packageName
                        }
                    }
                    "camera" -> {
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (resolveInfo != null && resolveInfo.activityInfo.packageName != "android") {
                            targetAppName = resolveInfo.loadLabel(pm).toString()
                            targetIcon = resolveInfo.loadIcon(pm)
                            targetPkg = resolveInfo.activityInfo.packageName
                        }
                    }
                    "gallery", "photo", "photos" -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
                        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (resolveInfo != null && resolveInfo.activityInfo.packageName != "android") {
                            targetAppName = resolveInfo.loadLabel(pm).toString()
                            targetIcon = resolveInfo.loadIcon(pm)
                            targetPkg = resolveInfo.activityInfo.packageName
                        }
                    }
                    "setting", "settings" -> {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (resolveInfo != null) {
                            targetAppName = resolveInfo.loadLabel(pm).toString()
                            targetIcon = resolveInfo.loadIcon(pm)
                            targetPkg = resolveInfo.activityInfo.packageName
                        }
                    }
                    "calculator" -> {
                        val calcPkgs = listOf(
                            "com.google.android.calculator", "com.sec.android.app.popupcalculator",
                            "com.miui.calculator", "com.android.calculator2", "com.coloros.calculator",
                            "com.oneplus.calculator", "com.asus.calculator"
                        )
                        for (pkg in calcPkgs) {
                            try {
                                val appInfo = pm.getApplicationInfo(pkg, 0)
                                targetPkg = pkg
                                targetAppName = pm.getApplicationLabel(appInfo).toString()
                                targetIcon = pm.getApplicationIcon(appInfo)
                                break
                            } catch (e: Exception) {
                                // continue
                            }
                        }
                    }
                }
            }

            // 3. Fuzzy match against installed launcher apps
            if (targetPkg == null) {
                val matched = installedApps.firstOrNull { app ->
                    val label = app.loadLabel(pm).toString().lowercase()
                    label.contains(normalizedName) || normalizedName.contains(label)
                }
                if (matched != null) {
                    targetPkg = matched.activityInfo.packageName
                    targetAppName = matched.loadLabel(pm).toString()
                    targetIcon = matched.loadIcon(pm)
                }
            }

            val cleanName = rawIconName.removePrefix("ic_")
            val candidateFilePaths = mutableListOf<String>()
            if (matchedDir != null) {
                candidateFilePaths.add("$matchedDir/$rawIconName.png")
                candidateFilePaths.add("$matchedDir/ic_$cleanName.png")
                candidateFilePaths.add("$matchedDir/$cleanName.png")
                candidateFilePaths.add("$matchedDir/bg_icon.png")
            }
            candidateFilePaths.add("assets_theme/category/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_theme/category/$themePath/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_theme/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_theme/$themePath/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_collection/theme/$themePath/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_collection/theme/$themePath/icons/$rawIconName.png")
            candidateFilePaths.add("assets_collection/icons/$themePath/bg_icon.png")
            // Fallbacks
            candidateFilePaths.add("assets_theme/category/Trending/theme_1/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_theme/category/Animal/theme_1/icons/ic_$cleanName.png")
            candidateFilePaths.add("assets_collection/theme/theme_1/icons/ic_$cleanName.png")

            var resolvedAssetPath = "assets_theme/category/Trending/theme_1/icons/ic_$cleanName.png"
            for (candidate in candidateFilePaths) {
                try {
                    val stream = assets.open(candidate)
                    stream.close()
                    resolvedAssetPath = candidate
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }

            val assetPath = "${com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.ASSET_BASE_URL}/$resolvedAssetPath"
            val isMatched = !targetPkg.isNullOrEmpty()
            val displayName = targetAppName ?: cleanName

            iconItems.add(
                ThemeIconItem(
                    id = "${theme.id}_$normalizedName",
                    iconName = rawIconName,
                    assetPath = assetPath,
                    targetPackageName = targetPkg,
                    targetAppName = if (isMatched) displayName else null,
                    targetAppIcon = targetIcon,
                    isSelected = isMatched, // Only select if app is bound
                    isUnlocked = false
                )
            )
        }

        // Requirement: Sort icons that match installed device apps to the TOP of the list
        iconItems.sortWith(
            compareByDescending<ThemeIconItem> { it.targetAppIcon != null }
                .thenBy { it.iconName }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DownloadIconItemAdapter(
            items = iconItems,
            onSelectToggle = { item ->
                if (item.targetPackageName.isNullOrEmpty()) {
                    Toast.makeText(this, "Please select an app to bind with this icon", Toast.LENGTH_SHORT).show()
                    showAppSelectionDialog(item)
                } else {
                    item.isSelected = !item.isSelected
                    adapter.notifyDataSetChanged()
                    updateSelectAllUI()
                }
            },
            onChangeApp = { item ->
                showAppSelectionDialog(item)
            },
            onInstallClick = { item ->
                installSingleIcon(item)
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun setupHeader() {
        updateSelectAllUI()
        
        binding.ivSelectAll.setOnClickListener {
            val bindableItems = iconItems.filter { !it.targetPackageName.isNullOrEmpty() }
            val hasUnselected = bindableItems.any { !it.isSelected }
            
            for (item in iconItems) {
                if (!item.targetPackageName.isNullOrEmpty()) {
                    item.isSelected = hasUnselected
                } else {
                    item.isSelected = false
                }
            }
            adapter.notifyDataSetChanged()
            updateSelectAllUI()
        }

        binding.llUnlockAll.visibility = View.GONE
    }

    private fun updateSelectAllUI() {
        val bindableItems = iconItems.filter { !it.targetPackageName.isNullOrEmpty() }
        val allSelected = bindableItems.isNotEmpty() && bindableItems.all { it.isSelected }
        isAllSelected = allSelected
        if (allSelected) {
            binding.ivSelectAll.setImageResource(R.drawable.ic_radio_checked)
            binding.ivSelectAll.imageTintList = null
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.bg_circle)
            binding.ivSelectAll.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
        }
    }

    private fun setupActions() {
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = "Take All"

        binding.actionView.clInstall.setOnClickListener {
            installSelectedIcons()
        }
    }

    private fun showAppSelectionDialog(item: ThemeIconItem) {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        binding.pbCreate.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val sortedApps = resolveInfos.sortedWith { a, b ->
                a.loadLabel(pm).toString().compareTo(b.loadLabel(pm).toString(), ignoreCase = true)
            }

            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                
                val builder = AlertDialog.Builder(this@DownloadIconActivity)
                builder.setTitle("Choose target app")
                
                val names = sortedApps.map { it.loadLabel(pm).toString() }.toTypedArray()
                builder.setItems(names) { dialog, which ->
                    val app = sortedApps[which]
                    item.targetPackageName = app.activityInfo.packageName
                    item.targetAppName = app.loadLabel(pm).toString()
                    item.targetAppIcon = app.loadIcon(pm)
                    item.isSelected = true // Automatically select when newly bound
                    adapter.notifyDataSetChanged()
                    updateSelectAllUI()
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                builder.show()
            }
        }
    }

    private fun installSingleIcon(item: ThemeIconItem) {
        if (item.targetPackageName.isNullOrEmpty()) {
            Toast.makeText(this, "Please select an app to bind with this icon", Toast.LENGTH_SHORT).show()
            showAppSelectionDialog(item)
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, listOf(item))
        sheet.show(supportFragmentManager, "select_icons")
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected && !it.targetPackageName.isNullOrEmpty() }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one icon bound to an app", Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, selected)
        sheet.show(supportFragmentManager, "select_icons")
    }
}
