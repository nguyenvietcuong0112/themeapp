package com.app.personalization.feature_widget

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.personalization.R
import com.app.personalization.core.data.AppItemData
import com.app.personalization.databinding.FragmentDownloadIconBinding
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadIconFragment : Fragment() {

    private var _binding: FragmentDownloadIconBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: KeyboardTheme

    private val iconItems = mutableListOf<ThemeIconItem>()
    private lateinit var adapter: DownloadIconItemAdapter
    
    private var isAllSelected = true

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadIconFragment {
            return DownloadIconFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DownloadThemeViewModel::class.java]
        theme = arguments?.getSerializable("theme") as? KeyboardTheme
            ?: throw IllegalArgumentException("Theme required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadIconBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbCreate.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    initPresetIcons()
                }
                setupRecyclerView()
                setupHeader()
                setupActions()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error loading preset icons", Toast.LENGTH_SHORT).show()
            } finally {
                binding.pbCreate.visibility = View.GONE
            }
        }
    }

    private fun initPresetIcons() {
        val context = requireContext()
        val pm = context.packageManager
        iconItems.clear()

        val themePath = theme.path
            .removePrefix("file:///android_asset/")
            .removePrefix("file://android_asset/")
            .removePrefix("android_asset/")
            .removePrefix("/")

        val discoveredIconNames = mutableListOf<String>()

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
                val files = context.assets.list(cleanDir)
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

            // 1. Known package mappings
            val defaultApp = AppItemData.APPS.find { it.id.equals(normalizedName, ignoreCase = true) }
            if (defaultApp != null) {
                try {
                    val appInfo = pm.getApplicationInfo(defaultApp.packageName, 0)
                    targetPkg = defaultApp.packageName
                    targetAppName = pm.getApplicationLabel(appInfo).toString()
                    targetIcon = pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    // App not installed
                }
            }

            // 2. Implicit system intent mappings
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

            // 3. Fuzzy search in installed launcher apps
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
                    val stream = context.assets.open(candidate)
                    stream.close()
                    resolvedAssetPath = candidate
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }

            val assetPath = "file:///android_asset/$resolvedAssetPath"
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

        // Sorting: Icons that match installed device apps go to TOP of list
        iconItems.sortWith(
            compareByDescending<ThemeIconItem> { it.targetAppIcon != null }
                .thenBy { it.iconName }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DownloadIconItemAdapter(
            items = iconItems,
            onSelectToggle = { item ->
                if (item.targetPackageName.isNullOrEmpty()) {
                    Toast.makeText(context, "Please select an app to bind with this icon", Toast.LENGTH_SHORT).show()
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
        val pm = requireContext().packageManager
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
                
                val builder = AlertDialog.Builder(context)
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
            Toast.makeText(context, "Please select an app to bind with this icon", Toast.LENGTH_SHORT).show()
            showAppSelectionDialog(item)
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, listOf(item))
        sheet.show(childFragmentManager, "select_icons")
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected && !it.targetPackageName.isNullOrEmpty() }
        if (selected.isEmpty()) {
            Toast.makeText(context, "Please select at least one icon bound to an app", Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, selected)
        sheet.show(childFragmentManager, "select_icons")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
