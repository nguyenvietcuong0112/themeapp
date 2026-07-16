package com.app.personalization.presentation.widget

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.personalization.R
import com.app.personalization.data.EventBus
import com.app.personalization.data.Subscribe
import com.app.personalization.data.ShortcutEvent
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentDownloadIconBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

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
        viewModel = ViewModelProvider(requireActivity()).get(DownloadThemeViewModel::class.java)
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
        val pm = requireContext().packageManager
        val iconFolder = try {
            val uuid = java.util.UUID.fromString(theme.id)
            val diyIcon = com.app.personalization.data.database.ThemeDatabase.getDatabase(requireContext()).iconDao().getIconPackByTheme(uuid)
            diyIcon?.folder ?: theme.path
        } catch (e: Exception) {
            theme.path
        }

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
                    assetPath = "theme_decorates/$iconFolder/key/preview.png",
                    targetPackageName = targetPkg,
                    targetAppName = targetAppName ?: app.name,
                    targetAppIcon = targetIcon,
                    isSelected = true
                )
            )
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
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
        // Hide unlock actions

        // Make install button always visible and active
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = "Install Icons"

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
            Toast.makeText(context, "Please bind a target app first!", Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = SelectIconBottomSheet()
        sheet.setParams(theme, listOf(item))
        sheet.show(childFragmentManager, "select_icons")
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected }
        if (selected.isEmpty()) {
            Toast.makeText(context, "Please select at least one icon", Toast.LENGTH_SHORT).show()
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
