package com.app.personalization.presentation.icon

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
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.ActivityDownloadIconBinding
import com.app.personalization.presentation.widget.ThemeIconItem
import com.app.personalization.presentation.widget.SelectIconBottomSheet
import com.app.personalization.presentation.widget.DownloadIconItemAdapter
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
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DownloadIconActivity, "Error loading preset icons", Toast.LENGTH_SHORT).show()
            } finally {
                binding.pbCreate.visibility = View.GONE
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.titleTextView.text = theme.name
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initPresetIcons() {
        val pm = packageManager
        for (app in com.app.personalization.data.AppItemData.APPS) {
            val name = app.id
            var targetPkg = app.packageName
            var targetAppName: String? = null
            var targetIcon: android.graphics.drawable.Drawable? = null

            try {
                val appInfo = pm.getApplicationInfo(targetPkg, 0)
                targetAppName = pm.getApplicationLabel(appInfo).toString()
                targetIcon = pm.getApplicationIcon(appInfo)
            } catch (e: Exception) {
                // App not installed
            }

            if (targetAppName == null && (name == "phone" || name == "camera")) {
                val intent = Intent(if (name == "phone") Intent.ACTION_DIAL else android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolveInfo != null) {
                    targetAppName = resolveInfo.loadLabel(pm).toString()
                    targetIcon = resolveInfo.loadIcon(pm)
                    targetPkg = resolveInfo.activityInfo.packageName
                }
            }

            iconItems.add(
                ThemeIconItem(
                    id = "${theme.id}_$name",
                    iconName = name,
                    assetPath = "theme_decorates/${theme.path}/key/preview.png",
                    targetPackageName = targetPkg,
                    targetAppName = targetAppName ?: app.name,
                    targetAppIcon = targetIcon,
                    isSelected = true
                )
            )
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DownloadIconItemAdapter(
            items = iconItems,
            onSelectToggle = { item ->
                item.isSelected = !item.isSelected
                adapter.notifyDataSetChanged()
                updateSelectAllUI()
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
            isAllSelected = !isAllSelected
            for (item in iconItems) {
                item.isSelected = isAllSelected
            }
            adapter.notifyDataSetChanged()
            updateSelectAllUI()
        }

        binding.llUnlockAll.visibility = View.GONE
    }

    private fun updateSelectAllUI() {
        val allSelected = iconItems.all { it.isSelected }
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
        binding.actionView.tvInstall.text = "Install Icons"

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
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                builder.show()
            }
        }
    }

    private fun installSingleIcon(item: ThemeIconItem) {
        val pkg = item.targetPackageName
        if (pkg.isNullOrEmpty()) {
            Toast.makeText(this, "Please bind a target app first!", Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, listOf(item))
        sheet.show(supportFragmentManager, "select_icons")
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one icon", Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, selected)
        sheet.show(supportFragmentManager, "select_icons")
    }
}
