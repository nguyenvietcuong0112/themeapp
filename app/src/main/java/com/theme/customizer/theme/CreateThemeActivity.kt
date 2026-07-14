package com.theme.customizer.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import com.app.personalization.data.database.ThemeDatabase
import com.app.personalization.data.database.entity.WidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Màn hình Tự phối bộ Theme (CreateThemeActivity) sử dụng giao diện Programmatic UI
 * tích hợp logic MVVM lưu trữ SQLite hoàn chỉnh và chụp Canvas preview.
 */
class CreateThemeActivity : AppCompatActivity() {

    private lateinit var createThemeView: CreateThemeView
    private lateinit var viewModel: CreateThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CreateThemeViewModel::class.java]

        // Giao diện LinearLayout chính
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Header chứa nút đóng và hướng dẫn
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val btnClose = Button(this).apply {
            text = "Đóng"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        val btnInfo = Button(this).apply {
            text = "Hướng dẫn"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                showHelpDialog()
            }
        }

        header.addView(btnClose)
        header.addView(spacer)
        header.addView(btnInfo)

        // Khung simulated phone view
        val previewContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        createThemeView = CreateThemeView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                (280 * resources.displayMetrics.density).toInt(),
                (500 * resources.displayMetrics.density).toInt()
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        previewContainer.addView(createThemeView)

        // Bảng điều khiển chọn đổi tài nguyên
        val actionPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 16, 8, 16)
            setBackgroundColor(Color.parseColor("#1E1E2E"))
        }

        val btnWallpaper = Button(this).apply {
            text = "Wallpaper"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                viewModel.selectWallpaper("https://csc-themeapp-widget.pages.dev/theme_2/wallpapers/bg_wallpaper.png")
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
                val newIcons = (1..24).map {
                    "https://csc-themeapp-widget.pages.dev/theme_2/icons/ic_facebook.png"
                }
                viewModel.selectIconPack(newIcons)
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
                viewModel.selectWidget("https://csc-themeapp-widget.pages.dev/theme_2/bg_preview.png")
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
        viewModel.wallpaperState.observe(this) { url ->
            createThemeView.setWallpaper(url)
        }

        viewModel.widgetState.observe(this) { url ->
            createThemeView.setWidget(url)
        }

        viewModel.iconPackState.observe(this) { icons ->
            createThemeView.setIcons(icons)
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

                // Ghi đè cấu hình vào Database sử dụng ThemeDatabase mới di chuyển
                val widgetTheme = WidgetTheme(
                    id = themeId,
                    name = "My Custom DIY Theme",
                    folder = file.absolutePath,
                    order = 100,
                    isCustom = true
                )
                ThemeDatabase.getDatabase(this@CreateThemeActivity).themeDao().insertTheme(widgetTheme)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateThemeActivity, "Đã lưu bộ Theme vào CSDL thành công!", Toast.LENGTH_SHORT).show()
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
}
