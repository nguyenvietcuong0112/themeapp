package com.theme.customizer.theme

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import com.app.personalization.data.database.ThemeDatabase
import com.app.personalization.data.database.entity.WidgetTheme
import com.app.personalization.data.database.entity.ThemeIconPack
import com.app.personalization.data.database.entity.ThemeWallpaper
import com.app.personalization.data.database.entity.ThemeWidget
import com.app.personalization.presentation.theme.CreateThemeViewModel
import com.app.personalization.presentation.theme.CreateThemeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CreateThemeActivity : AppCompatActivity() {

    private lateinit var viewModel: CreateThemeViewModel
    private lateinit var createThemeView: CreateThemeView
    private lateinit var ivClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CreateThemeViewModel::class.java]

        // Programmatically build UI matching activity_create_theme.xml
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // Header Toolbar
        val header = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.dp_56))
        }

        ivClose = ImageView(this).apply {
            setImageResource(R.drawable.ic_close)
            imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.dp_40), resources.getDimensionPixelSize(R.dimen.dp_40)).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                marginStart = 16
            }
            setOnClickListener {
                handleExit()
            }
        }

        val tvTitle = TextView(this).apply {
            text = "Customize Theme"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        }

        val ivInfo = ImageView(this).apply {
            setImageResource(R.drawable.ic_info)
            imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.dp_40), resources.getDimensionPixelSize(R.dimen.dp_40)).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = 16
            }
            setOnClickListener {
                showHelpDialog()
            }
        }

        header.addView(ivClose)
        header.addView(tvTitle)
        header.addView(ivInfo)

        // Workspace Container
        val previewContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                setMargins(24, 24, 24, 24)
            }
        }

        createThemeView = CreateThemeView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        previewContainer.addView(createThemeView)

        // Bottom panel
        val actionPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#1A1A24"))
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val btnWallpaper = Button(this).apply {
            text = "Wallpaper"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                val wallpaperDialog = com.app.personalization.presentation.theme.ChangeCreateThemeWallpaperDialog()
                wallpaperDialog.setOnChangeAppListener(object : com.app.personalization.presentation.theme.ChangeCreateThemeWallpaperDialog.OnChangeAppListener {
                    override fun onSelect(wallpaper: com.app.personalization.data.database.entity.WidgetThemeWallpaper) {
                        viewModel.loadWallpaper(wallpaper)
                    }
                })
                wallpaperDialog.show(supportFragmentManager, "wallpaper_picker")
            }
        }

        val btnIcon = Button(this).apply {
            text = "Icon"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                val iconDialog = com.app.personalization.presentation.theme.ChangeCreateThemeIconDialog()
                iconDialog.setOnChangeAppListener(object : com.app.personalization.presentation.theme.ChangeCreateThemeIconDialog.OnChangeAppListener {
                    override fun onSelect(icon: com.app.personalization.data.database.entity.WidgetThemeIcon) {
                        viewModel.loadIcons(icon)
                    }
                })
                iconDialog.show(supportFragmentManager, "icon_picker")
            }
        }

        val btnWidget = Button(this).apply {
            text = "Widget"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                val widgetDialog = com.app.personalization.presentation.theme.ChangeCreateThemeWidgetDialog()
                widgetDialog.setOnChangeAppListener(object : com.app.personalization.presentation.theme.ChangeCreateThemeWidgetDialog.OnChangeAppListener {
                    override fun onSelect(widget: com.app.personalization.data.database.entity.WidgetThemeWidget) {
                        viewModel.loadWidget(widget)
                    }
                })
                widgetDialog.show(supportFragmentManager, "widget_picker")
            }
        }

        val btnReset = Button(this).apply {
            text = "Reset"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF819F"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                viewModel.resetTheme()
                Toast.makeText(this@CreateThemeActivity, "Chủ đề đã được đặt lại về mặc định", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSave = Button(this).apply {
            text = "Lưu"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.GREEN)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                saveThemeSnapshot()
            }
        }

        actionPanel.addView(btnWallpaper)
        actionPanel.addView(btnIcon)
        actionPanel.addView(btnWidget)
        actionPanel.addView(btnReset)
        actionPanel.addView(btnSave)

        root.addView(header)
        root.addView(previewContainer)
        root.addView(actionPanel)

        setContentView(root)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.wallpaper.observe(this) { wp ->
            val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(this, wp.folder)
            val url = com.app.personalization.data.CdnPathResolver.getWallpaperFullUrl(resolvedFolder, wp.imageBg)
            createThemeView.setWallpaper(url)
        }

        viewModel.widget.observe(this) { wdg ->
            val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(this, wdg.folder)
            val type = when (wdg.category.lowercase()) {
                "calendar" -> "today"
                "weather" -> "weather"
                "image" -> "image"
                else -> "clocks"
            }
            val url = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$resolvedFolder/widgets/$type/bg_preview_medium.png"
            createThemeView.setWidget(url)
        }

        viewModel.icon.observe(this) { ic ->
            val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(this, ic.folder)
            val iconsList = listOf(
                "facebook", "instagram", "messenger", "tiktok",
                "chrome", "gmail", "camera", "settings"
            )
            val urls = iconsList.map { 
                com.app.personalization.data.CdnPathResolver.getSingleIconUrl(resolvedFolder, it) 
            }
            createThemeView.setIcons(urls)
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hướng dẫn tự phối Theme")
            .setMessage("Thay đổi ảnh nền, icon và widget bằng các nút ở thanh công cụ dưới. Sau khi hoàn thành, nhấn Lưu để tạo Theme đồng bộ lưu vào cơ sở dữ liệu.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveThemeSnapshot() {
        if (!viewModel.isChanged) {
            Toast.makeText(this, "Hãy thay đổi tối thiểu 1 thành phần trước khi lưu!", Toast.LENGTH_SHORT).show()
            return
        }

        if (createThemeView.width <= 0 || createThemeView.height <= 0) {
            Toast.makeText(this, "Vui lòng chờ bản vẽ dựng hình!", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(createThemeView.width, createThemeView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        createThemeView.draw(canvas)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val themeId = UUID.randomUUID()
                val filename = "theme_snap_${themeId}.png"
                val file = File(filesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                val wp = viewModel.wallpaper.value!!
                val ic = viewModel.icon.value!!
                val wdg = viewModel.widget.value!!

                // 1. Insert custom WidgetTheme (ThemeDatabase)
                val widgetTheme = WidgetTheme(
                    id = themeId,
                    name = "My Custom DIY Theme",
                    folder = file.absolutePath,
                    order = 100,
                    isCustom = true
                )
                ThemeDatabase.getDatabase(this@CreateThemeActivity).themeDao().insertTheme(widgetTheme)

                // 2. Insert child records into ThemeDatabase
                val themeWallpaper = ThemeWallpaper(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    folder = wp.folder,
                    imageName = wp.imageBg
                )
                ThemeDatabase.getDatabase(this@CreateThemeActivity).wallpaperDao().insertWallpaper(themeWallpaper)

                val themeIconPack = ThemeIconPack(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    folder = ic.folder,
                    name = ic.name
                )
                ThemeDatabase.getDatabase(this@CreateThemeActivity).iconDao().insertIconPack(themeIconPack)

                val themeWidget = ThemeWidget(
                    id = UUID.randomUUID(),
                    themeId = themeId,
                    templatePath = wdg.folder,
                    size = "MEDIUM",
                    type = wdg.category.uppercase()
                )
                ThemeDatabase.getDatabase(this@CreateThemeActivity).widgetDao().insertWidget(themeWidget)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Đã lưu bộ Theme vào CSDL thành công!", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(this@CreateThemeActivity, com.app.personalization.presentation.widget.DownloadThemeActivity::class.java).apply {
                        putExtra(com.app.personalization.cscthemeapp.widget.model.model.Constants.Intents.WIDGET_THEME, widgetTheme)
                        putExtra(com.app.personalization.cscthemeapp.widget.model.model.Constants.Intents.IS_CUSTOM, true)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Lỗi khi lưu Theme!", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
