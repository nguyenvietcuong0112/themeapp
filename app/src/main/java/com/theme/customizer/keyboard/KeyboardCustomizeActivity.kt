package com.theme.customizer.keyboard

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Màn hình tùy chỉnh và dùng thử Bàn phím (KeyboardCustomizeActivity).
 * Tích hợp EditText kiểm thử nhập liệu và bảng tùy chọn giao diện thời gian thực.
 */
class KeyboardCustomizeActivity : AppCompatActivity() {

    private lateinit var etTestInput: EditText
    private lateinit var liveTestKeyboardView: LiveTestKeyboardView
    private var currentConfig = ThemeConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bố cục tổng quan dọc (vertical LinearLayout)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(16, 32, 16, 16)
        }

        // Ô kiểm thử nhập liệu
        etTestInput = EditText(this).apply {
            hint = "Chạm vào đây để nhập thử bàn phím..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.parseColor("#1E1E2E"))
            setPadding(24, 24, 24, 24)
            gravity = Gravity.START or Gravity.TOP
            minLines = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }
        root.addView(etTestInput)

        // Panel thay đổi màu sắc/bo phím
        val optionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }

        val btnChangeColor = Button(this).apply {
            text = "Màu Phím"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                // Đổi cấu hình màu sắc ngẫu nhiên
                val colors = listOf("#1E1E2E", "#2A2A3D", "#3F51B5", "#E91E63", "#009688")
                val randomColor = colors.random()
                currentConfig = currentConfig.copy(
                    keyConfig = currentConfig.keyConfig.copy(keyBgColor = randomColor)
                )
                liveTestKeyboardView.updateTheme(currentConfig)
                Toast.makeText(this@KeyboardCustomizeActivity, "Đã chọn màu: $randomColor", Toast.LENGTH_SHORT).show()
            }
        }

        val btnChangeShape = Button(this).apply {
            text = "Bo Góc"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                // Thay đổi góc bo phím từ 0dp đến 24dp
                val shapes = listOf(0f, 6f, 12f, 24f)
                val currentRadius = currentConfig.keyStyle.cornerRadiusDp
                val nextRadius = shapes.firstOrNull { it > currentRadius } ?: 0f
                currentConfig = currentConfig.copy(
                    keyStyle = currentConfig.keyStyle.copy(cornerRadiusDp = nextRadius)
                )
                liveTestKeyboardView.updateTheme(currentConfig)
                Toast.makeText(this@KeyboardCustomizeActivity, "Bo góc: ${nextRadius}dp", Toast.LENGTH_SHORT).show()
            }
        }

        optionsLayout.addView(btnChangeColor)
        optionsLayout.addView(btnChangeShape)
        root.addView(optionsLayout)

        // Keyboard preview nằm ở dưới cùng
        liveTestKeyboardView = LiveTestKeyboardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            onKeyClickListener = { key ->
                handleLiveInput(key)
            }
        }
        root.addView(liveTestKeyboardView)

        setContentView(root)
    }

    private fun handleLiveInput(key: String) {
        val text = etTestInput.text.toString()
        when (key) {
            "Del" -> {
                if (text.isNotEmpty()) {
                    etTestInput.setText(text.dropLast(1))
                    etTestInput.setSelection(etTestInput.text.length)
                }
            }
            "Space" -> {
                etTestInput.append(" ")
            }
            "Enter" -> {
                etTestInput.append("\n")
            }
            "Shift" -> {
                // Không thêm kí tự
            }
            else -> {
                etTestInput.append(key.lowercase())
            }
        }
    }
}
