package com.app.personalization.presentation.theme

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentDownloadIconBinding
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

        lifecycleScope.launch(Dispatchers.IO) {
            initPresetIcons()
            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                setupRecyclerView()
                setupHeader()
                setupActions()
            }
        }
    }

    private fun initPresetIcons() {
        val iconNames = listOf("facebook", "instagram", "tiktok", "youtube", "messenger", "gmail", "chrome", "phone", "camera", "settings")
        val packageMappings = mapOf(
            "facebook" to "com.facebook.katana",
            "instagram" to "com.instagram.android",
            "tiktok" to "com.zhiliaoapp.musically",
            "youtube" to "com.google.android.youtube",
            "messenger" to "com.facebook.orca",
            "gmail" to "com.google.android.gm",
            "chrome" to "com.android.chrome",
            "settings" to "com.android.settings"
        )

        val pm = requireContext().packageManager
        for (name in iconNames) {
            val targetPkg = packageMappings[name]
            var targetAppName: String? = null
            var targetIcon: Drawable? = null

            if (targetPkg != null) {
                try {
                    val appInfo = pm.getApplicationInfo(targetPkg, 0)
                    targetAppName = pm.getApplicationLabel(appInfo).toString()
                    targetIcon = pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    // App is not installed
                }
            }

            // Fallback for system apps like Phone & Camera
            if (targetAppName == null && (name == "phone" || name == "camera")) {
                val intent = Intent(if (name == "phone") Intent.ACTION_DIAL else android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolveInfo != null) {
                    targetAppName = resolveInfo.loadLabel(pm).toString()
                    targetIcon = resolveInfo.loadIcon(pm)
                }
            }

            iconItems.add(
                ThemeIconItem(
                    id = "${theme.id}_$name",
                    iconName = name,
                    assetPath = "theme_decorates/${theme.path}/key/preview.png", // fallback or default preview
                    targetPackageName = targetPkg,
                    targetAppName = targetAppName,
                    targetAppIcon = targetIcon,
                    isSelected = true
                )
            )
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        
        val isUnlocked = viewModel.isIconPackUnlocked(theme.id, theme.isPremium.not())
        adapter = DownloadIconItemAdapter(
            items = iconItems,
            isUnlocked = isUnlocked,
            onSelectToggle = { item ->
                item.isSelected = !item.isSelected
                adapter.notifyDataSetChanged()
                updateSelectAllUI()
            },
            onChangeApp = { item ->
                showAppSelectionDialog(item)
            },
            onUnlockClick = {
                unlockIconPack()
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
        // Ads logic disabled
        binding.actionView.llPlayVideo.root.visibility = View.GONE

        // Cost setup
        binding.actionView.tvCoin.text = "100"

        updateLockUnlockState()

        binding.actionView.llAddCoin.setOnClickListener {
            unlockIconPack()
        }

        binding.actionView.llGetAll.setOnClickListener {
            Toast.makeText(context, "Unlock premium from store to get all!", Toast.LENGTH_SHORT).show()
        }

        binding.actionView.clInstall.setOnClickListener {
            installSelectedIcons()
        }
    }

    private fun updateLockUnlockState() {
        val isUnlocked = viewModel.isIconPackUnlocked(theme.id, theme.isPremium.not())
        if (isUnlocked) {
            binding.actionView.llAction.visibility = View.GONE
            binding.actionView.clInstall.visibility = View.VISIBLE
        } else {
            binding.actionView.llAction.visibility = View.VISIBLE
            binding.actionView.clInstall.visibility = View.GONE
        }
    }

    private fun unlockIconPack() {
        if (viewModel.deductCoins(100)) {
            viewModel.unlockIconPack(theme.id)
            setupRecyclerView()
            updateLockUnlockState()
            Toast.makeText(context, "Icon Pack unlocked successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Not enough coins! Please earn or buy more.", Toast.LENGTH_SHORT).show()
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
        
        val context = requireContext()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent == null) {
            Toast.makeText(context, "Target app is not launchable", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val iconBitmap = loadThemeIconBitmap(item)
            withContext(Dispatchers.Main) {
                if (iconBitmap != null) {
                    val success = requestShortcutPin(item.targetAppName ?: item.iconName, iconBitmap, launchIntent)
                    if (success) {
                        showSuccessDialog(item.targetAppName ?: item.iconName)
                    }
                } else {
                    Toast.makeText(context, "Failed to load custom icon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun installSelectedIcons() {
        val selected = iconItems.filter { it.isSelected }
        if (selected.isEmpty()) {
            Toast.makeText(context, "Please select at least one icon", Toast.LENGTH_SHORT).show()
            return
        }

        var installedCount = 0
        lifecycleScope.launch(Dispatchers.IO) {
            val context = requireContext()
            for (item in selected) {
                val pkg = item.targetPackageName ?: continue
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg) ?: continue
                val iconBitmap = loadThemeIconBitmap(item) ?: continue

                withContext(Dispatchers.Main) {
                    val success = requestShortcutPin(item.targetAppName ?: item.iconName, iconBitmap, launchIntent)
                    if (success) installedCount++
                }
            }
            withContext(Dispatchers.Main) {
                if (installedCount > 0) {
                    showSuccessDialog("$installedCount icons")
                }
            }
        }
    }

    private fun requestShortcutPin(label: String, bmp: Bitmap, launchIntent: Intent): Boolean {
        val context = requireContext()
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Pinning shortcut not supported by launcher", Toast.LENGTH_SHORT).show()
            return false
        }

        val shortcutId = "theme_shortcut_${System.currentTimeMillis()}_${label.hashCode()}"
        val roundedBmp = getRoundedCornerBitmap(bmp, 32) // Styled rounded corner shortcut icon
        
        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(label)
            .setIcon(IconCompat.createWithBitmap(roundedBmp))
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun loadThemeIconBitmap(item: ThemeIconItem): Bitmap? {
        val context = requireContext()
        return try {
            val inputStream: InputStream = context.assets.open(item.assetPath)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback drawing if asset file not present
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

    private fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
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

    private fun showSuccessDialog(name: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Shortcut created")
        builder.setMessage("Shortcut for $name has been pinned to your Home screen successfully!")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadIconFragment {
            return DownloadIconFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }
}
