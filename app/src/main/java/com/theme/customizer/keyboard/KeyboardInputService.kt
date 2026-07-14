package com.theme.customizer.keyboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Nhân xử lý hệ thống nhập liệu bàn phím (KeyboardInputService).
 * Kế thừa InputMethodService để khởi dựng, nạp cấu hình và chuyển giao kí tự cho HĐH.
 */
class KeyboardInputService : InputMethodService() {

    private var themeConfig = ThemeConfig()

    override fun onCreateInputView(): View {
        val density = resources.displayMetrics.density

        // Layout tổng thể của bàn phím hệ thống
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 16)
        }

        // 3 hàng chữ cái QWERTY tiêu chuẩn
        val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
        val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
        val row3 = listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Del")
        val row4 = listOf("Space", "Enter")

        createRowLayout(root, row1, density)
        createRowLayout(root, row2, density)
        createRowLayout(root, row3, density)
        createRowLayout(root, row4, density)

        return root
    }

    private fun createRowLayout(parent: LinearLayout, keys: List<String>, density: Float) {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (key in keys) {
            val keyButton = Button(this).apply {
                text = key
                textSize = themeConfig.keyConfig.keyTextSizeSp
                setTextColor(Color.parseColor(themeConfig.keyConfig.keyTextColor))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)
                
                // Thiết lập trạng thái Drawable động (Normal & Pressed)
                val normalBg = GradientDrawable().apply {
                    setColor(Color.parseColor(themeConfig.keyConfig.keyBgColor))
                    cornerRadius = themeConfig.keyStyle.cornerRadiusDp * density
                }
                val pressedBg = GradientDrawable().apply {
                    setColor(Color.parseColor("#00E5FF"))
                    cornerRadius = themeConfig.keyStyle.cornerRadiusDp * density
                }

                background = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), pressedBg)
                    addState(intArrayOf(), normalBg)
                }

                // Gán kích thước linh động cho phím đặc biệt
                val widthWeight = when (key) {
                    "Shift", "Del" -> 1.5f
                    "Space" -> 4f
                    "Enter" -> 2f
                    else -> 1f
                }

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    (50 * density).toInt(),
                    widthWeight
                ).apply {
                    setMargins(6, 6, 6, 6)
                }

                setOnClickListener {
                    handleKeyPress(key)
                }
            }
            rowLayout.addView(keyButton)
        }

        parent.addView(rowLayout)
    }

    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return
        when (key) {
            "Del" -> {
                // Gửi tín hiệu phím xóa chuẩn của hệ thống
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }
            "Shift" -> {
                // Xử lý Shift (nếu cần đổi hoa/thường)
            }
            "Space" -> {
                ic.commitText(" ", 1)
            }
            "Enter" -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            }
            else -> {
                // Nhập kí tự thông thường
                ic.commitText(key.lowercase(), 1)
            }
        }
    }
}
