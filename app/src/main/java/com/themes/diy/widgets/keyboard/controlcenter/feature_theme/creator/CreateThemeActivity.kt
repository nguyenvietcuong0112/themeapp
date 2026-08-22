package com.themes.diy.widgets.keyboard.controlcenter.feature_theme.creator

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme
import com.themes.diy.widgets.keyboard.controlcenter.feature_icon.data.entity.ThemeIconPack
import com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.entity.ThemeWallpaper
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.ThemeWidget
import com.themes.diy.widgets.keyboard.controlcenter.databinding.ActivityCreateThemeBinding
import com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeWallpaper
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeIcon
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeWidget

class CreateThemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateThemeBinding
    private lateinit var viewModel: CreateThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CreateThemeViewModel::class.java]

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        binding.ivClose.setOnClickListener {
            handleExit()
        }

        binding.ivInfo.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun setupListeners() {
        // Option tabs clicks
        binding.actionView.llWallpaper.setOnClickListener {
            val wallpaperDialog = ChangeCreateThemeWallpaperDialog()
            wallpaperDialog.setOnChangeAppListener(object : ChangeCreateThemeWallpaperDialog.OnChangeAppListener {
                override fun onSelect(wallpaper: WidgetThemeWallpaper) {
                    viewModel.loadWallpaper(wallpaper)
                }
            })
            wallpaperDialog.show(supportFragmentManager, "wallpaper_picker")
        }

        binding.actionView.llIcon.setOnClickListener {
            val iconDialog = ChangeCreateThemeIconDialog()
            iconDialog.setOnChangeAppListener(object : ChangeCreateThemeIconDialog.OnChangeAppListener {
                override fun onSelect(icon: WidgetThemeIcon) {
                    viewModel.loadIcons(icon)
                }
            })
            iconDialog.show(supportFragmentManager, "icon_picker")
        }

        binding.actionView.llWidget.setOnClickListener {
            val widgetDialog = ChangeCreateThemeWidgetDialog()
            widgetDialog.setOnChangeAppListener(object : ChangeCreateThemeWidgetDialog.OnChangeAppListener {
                override fun onSelect(widget: WidgetThemeWidget) {
                    viewModel.loadWidget(widget)
                }
            })
            widgetDialog.show(supportFragmentManager, "widget_picker")
        }

        binding.actionView.ivReset.setOnClickListener {
            viewModel.resetTheme()
            Toast.makeText(this, "Chủ đề đã được đặt lại về mặc định", Toast.LENGTH_SHORT).show()
        }

        binding.actionView.tvSave.setOnClickListener {
            saveTheme()
        }
    }

    private fun observeViewModel() {
        viewModel.wallpaper.observe(this) { wp ->
            val resolvedFolder = ResourceConfig.getThemeFolderByPath(this, wp.folder)
            val url = ResourceConfig.getWallpaperFullUrl(resolvedFolder, wp.imageBg)
            binding.themeView.setWallpaper(url)
        }

        viewModel.widget.observe(this) { wdg ->
            val resolvedFolder = ResourceConfig.getThemeFolderByPath(this, wdg.folder)
            val type = when (wdg.category.lowercase()) {
                "calendar" -> "today"
                "weather" -> "weather"
                "image" -> "image"
                else -> "clocks"
            }
            val url = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$resolvedFolder/widgets/$type/bg_preview_medium.png"
            binding.themeView.setWidget(url)
        }

        viewModel.icon.observe(this) { ic ->
            val resolvedFolder = ResourceConfig.getThemeFolderByPath(this, ic.folder)
            val iconsList = listOf(
                "facebook", "instagram", "messenger", "tiktok",
                "chrome", "gmail", "camera", "settings"
            )
            val urls = iconsList.map { 
                ResourceConfig.getSingleIconUrl(resolvedFolder, it) 
            }
            binding.themeView.setIcons(urls)
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hướng dẫn")
            .setMessage("Tùy biến màn hình chính bằng cách lựa chọn hình nền, bộ biểu tượng và widget. Nhấn Lưu để tạo chủ đề tùy chỉnh của bạn.")
            .setPositiveButton("Đồng ý") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveTheme() {
        if (!viewModel.isChanged) {
            Toast.makeText(this, "Hãy thay đổi tối thiểu 1 thành phần trước khi lưu!", Toast.LENGTH_SHORT).show()
            return
        }

        val targetView = binding.themeView.binding.clView
        if (targetView.width == 0 || targetView.height == 0) {
            Toast.makeText(this, "Giao diện xem trước chưa sẵn sàng, hãy thử lại...", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = viewToBitmap(targetView)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Save preview screenshot locally
                val themeId = UUID.randomUUID()
                val filename = "theme_preview_${themeId}.png"
                val file = File(filesDir, filename)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                val wp = viewModel.wallpaper.value!!
                val ic = viewModel.icon.value!!
                val wdg = viewModel.widget.value!!

                // 1. Insert custom KeyboardTheme
                val customTheme = KeyboardTheme(
                    id = themeId.toString(),
                    name = "My Custom Theme ${themeId.toString().take(4)}",
                    path = file.absolutePath,
                    rawType = "diy",
                    backgroundPath = file.absolutePath,
                    previewPath = file.absolutePath
                )
                ServiceLocator.getThemeDao(this@CreateThemeActivity).insertTheme(customTheme)

                // 2. Insert custom WidgetTheme (ThemeDatabase)
                val newWidgetTheme = com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetTheme(
                    id = themeId,
                    name = "My Custom Theme ${themeId.toString().take(4)}",
                    folder = file.absolutePath,
                    order = 999,
                    isCustom = true
                )
                com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.ThemeDatabase.getDatabase(this@CreateThemeActivity).themeDao().insertTheme(newWidgetTheme)

                // 3. Insert child records into ThemeDatabase to associate custom selections
                val themeWallpaper = ThemeWallpaper(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    folder = wp.folder,
                    imageName = wp.imageBg
                )
                com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.ThemeDatabase.getDatabase(this@CreateThemeActivity).wallpaperDao().insertWallpaper(themeWallpaper)

                val themeIconPack = ThemeIconPack(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    folder = ic.folder,
                    name = ic.name
                )
                com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.ThemeDatabase.getDatabase(this@CreateThemeActivity).iconDao().insertIconPack(themeIconPack)

                val themeWidget = ThemeWidget(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    templatePath = wdg.folder,
                    size = "MEDIUM",
                    type = wdg.category.uppercase()
                )
                com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.ThemeDatabase.getDatabase(this@CreateThemeActivity).widgetDao().insertWidget(themeWidget)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Đã lưu chủ đề tùy chỉnh thành công!", Toast.LENGTH_SHORT).show()
                    
                    val intent = android.content.Intent(this@CreateThemeActivity, com.themes.diy.widgets.keyboard.controlcenter.feature_widget.DownloadThemeActivity::class.java).apply {
                        putExtra("widget_theme", newWidgetTheme)
                        putExtra("is_custom", true)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Lỗi lưu chủ đề!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun handleExit() {
        if (viewModel.isChanged) {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận thoát")
                .setMessage("Bạn có thay đổi chưa lưu, bạn có chắc chắn muốn thoát?")
                .setPositiveButton("Có") { _, _ ->
                    finish()
                }
                .setNegativeButton("Không") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        handleExit()
    }
}
