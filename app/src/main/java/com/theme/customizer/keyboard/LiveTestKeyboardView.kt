package com.theme.customizer.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

/**
 * Bàn phím dùng thử trực tiếp (LiveTestKeyboardView) bên trong ứng dụng.
 */
class LiveTestKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var themeConfig = ThemeConfig()
    var onKeyClickListener: ((String) -> Unit)? = null

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.parseColor("#12121A"))
        val density = resources.displayMetrics.density

        val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
        val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
        val row3 = listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Del")
        val row4 = listOf("Space", "Enter")

        createRowLayout(row1, density)
        createRowLayout(row2, density)
        createRowLayout(row3, density)
        createRowLayout(row4, density)
    }

    private fun createRowLayout(keys: List<String>, density: Float) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        for (key in keys) {
            val keyButton = Button(context).apply {
                text = key
                textSize = themeConfig.keyConfig.keyTextSizeSp
                setTextColor(Color.parseColor(themeConfig.keyConfig.keyTextColor))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)
                
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

                val widthWeight = when (key) {
                    "Shift", "Del" -> 1.5f
                    "Space" -> 4f
                    "Enter" -> 2f
                    else -> 1f
                }

                layoutParams = LayoutParams(
                    0,
                    (50 * density).toInt(),
                    widthWeight
                ).apply {
                    setMargins(6, 6, 6, 6)
                }

                setOnClickListener {
                    onKeyClickListener?.invoke(key)
                }
            }
            rowLayout.addView(keyButton)
        }
        addView(rowLayout)
    }

    fun updateTheme(config: ThemeConfig) {
        this.themeConfig = config
        removeAllViews()
        val density = resources.displayMetrics.density
        val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
        val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
        val row3 = listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Del")
        val row4 = listOf("Space", "Enter")

        createRowLayout(row1, density)
        createRowLayout(row2, density)
        createRowLayout(row3, density)
        createRowLayout(row4, density)
    }
}
