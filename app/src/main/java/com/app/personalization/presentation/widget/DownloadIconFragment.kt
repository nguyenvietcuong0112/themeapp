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
    private val installQueue = ArrayList<ThemeIconItem>()

    private val shortcutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.app.personalization.SHORTCUT_INSTALLED") {
                EventBus.getDefault().post(ShortcutEvent())
            }
        }
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val filter = IntentFilter("com.app.personalization.SHORTCUT_INSTALLED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(shortcutReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            requireContext().registerReceiver(shortcutReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        requireContext().unregisterReceiver(shortcutReceiver)
    }

    @Subscribe
    fun onShortcutInstalled(event: ShortcutEvent) {
        if (installQueue.isNotEmpty()) {
            val nextItem = installQueue.removeAt(0)
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = loadThemeIconBitmap(nextItem)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        addShortcut(requireContext(), nextItem, bitmap)
                    } else {
                        onShortcutInstalled(ShortcutEvent())
                    }
                }
            }
        } else {
            binding.pbCreate.visibility = View.GONE
            SetupSucceedDialogFragment().show(childFragmentManager, "success")
        }
    }

    private fun initPresetIcons() {
        val pm = requireContext().packageManager
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
        binding.actionView.llAction.visibility = View.GONE
        binding.actionView.llPlayVideo.root.visibility = View.GONE

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

    private fun addShortcut(context: Context, item: ThemeIconItem, bmp: Bitmap) {
        val targetPkg = item.targetPackageName ?: return
        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPkg) ?: return
        
        // Wrap intent to point to ChangeIconActivity proxy activity
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
                Toast.makeText(context, "Pinning shortcut not supported", Toast.LENGTH_SHORT).show()
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
            android.util.Log.w("DownloadIcon", "Asset open failed, trying CDN: $cdnUrl")
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(cdnUrl)
                    .submit()
                    .get()
            } catch (e2: Exception) {
                android.util.Log.w("DownloadIcon", "CDN load failed: $cdnUrl. Generating custom icon fallback.")
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
        _binding = null
    }
}
