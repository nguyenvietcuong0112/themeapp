package com.theme.customizer.wallpaper

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Màn hình Biên tập/Tự thiết kế hình nền DIY (DIYWallpaperActivity)
 * Thiết lập giao diện hoàn toàn bằng mã nguồn chương trình (Programmatic UI) để hoạt động độc lập 100%.
 */
class DIYWallpaperActivity : AppCompatActivity() {

    private lateinit var pageWrapperView: PageWrapperView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Giao diện gốc dọc (LinearLayout)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Khung chứa canvas vẽ hình nền
        val canvasContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        pageWrapperView = PageWrapperView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }
        canvasContainer.addView(pageWrapperView)

        // Bảng các nút bấm biên tập
        val actionPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#1E1E2E"))
        }

        val btnAddBg = Button(this).apply {
            text = "Background"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener {
                pageWrapperView.setWallpaper("https://csc-themeapp-widget.pages.dev/themes/category/Aesthetic/theme_1/wallpapers/bg_wallpaper.png")
            }
        }

        val btnAddSticker = Button(this).apply {
            text = "Sticker"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener {
                pageWrapperView.addSticker("https://csc-themeapp-widget.pages.dev/themes/category/Aesthetic/theme_1/icons/ic_facebook.png")
            }
        }

        val btnAddText = Button(this).apply {
            text = "Text"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener {
                showAddTextDialog()
            }
        }

        val btnSave = Button(this).apply {
            text = "Save"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF819F"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener {
                saveAndApplyWallpaper()
            }
        }

        actionPanel.addView(btnAddBg)
        actionPanel.addView(btnAddSticker)
        actionPanel.addView(btnAddText)
        actionPanel.addView(btnSave)

        root.addView(canvasContainer)
        root.addView(actionPanel)

        setContentView(root)
    }

    private fun showAddTextDialog() {
        val editText = EditText(this).apply {
            hint = "Nhập văn bản..."
        }
        AlertDialog.Builder(this)
            .setTitle("Thêm chữ nghệ thuật")
            .setView(editText)
            .setPositiveButton("Thêm") { dialog, _ ->
                val text = editText.text.toString()
                if (text.isNotEmpty()) {
                    pageWrapperView.addText(text, Color.WHITE)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveAndApplyWallpaper() {
        pageWrapperView.clearSelections()
        
        val width = pageWrapperView.width
        val height = pageWrapperView.height
        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "Bản vẽ chưa sẵn sàng!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val bitmap = SnapshotGenerator.generate(pageWrapperView, width, height)
            val success = WallpaperSetter.setWallpaper(this@DIYWallpaperActivity, bitmap, 1)
            if (success) {
                Toast.makeText(this@DIYWallpaperActivity, "Đã lưu và cài hình nền thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@DIYWallpaperActivity, "Không thể thiết lập hình nền!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
