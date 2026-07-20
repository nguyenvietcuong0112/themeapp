package com.app.personalization.presentation.theme

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.domain.model.KeyDef

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var theme: KeyboardTheme? = null
    private val keyViews = ArrayList<TextView>()

    private var previewPopup: PopupWindow? = null
    private var previewTextView: TextView? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.BOTTOM
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val pad = dpToPx(6)
        setPadding(pad, pad, pad, dpToPx(36))
    }

    fun initKeyboard(rows: List<List<KeyDef>>, activeTheme: KeyboardTheme?) {
        this.theme = activeTheme
        this.removeAllViews()
        keyViews.clear()

        com.app.personalization.data.DefaultColors.setBackground(this, activeTheme)

        for (row in rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(4)
                    bottomMargin = dpToPx(4)
                }
            }

            for (key in row) {
                val keyView = createKeyView(key)
                rowLayout.addView(keyView)
                keyViews.add(keyView)
            }
            addView(rowLayout)
        }
    }

    private fun createKeyView(key: KeyDef): TextView {
        val keyView = TextView(context).apply {
            tag = key
            text = getDisplayLabel(key)
            gravity = Gravity.CENTER
            textSize = 20f

            val textColor = if (theme != null) {
                theme!!.keyTextColor(context)
            } else {
                0xFF00E5FF.toInt()
            }
            setTextColor(textColor)

            val tf = if (theme != null) {
                try {
                    theme!!.keyFont(context)
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            } else {
                Typeface.DEFAULT
            }
            typeface = tf

            val weight = if (key.functionalType == "space") 4f else if (key.isFunctional) 1.5f else 1.0f
            layoutParams = LayoutParams(0, dpToPx(46), weight).apply {
                leftMargin = dpToPx(3)
                rightMargin = dpToPx(3)
            }

            val code = if (key.code != 0) key.code else when (key.functionalType) {
                "shift" -> -1
                "delete" -> -5
                "symbols_switch" -> -2
                "emoji_switch" -> 12289
                "space" -> 32
                "enter" -> 10
                else -> key.label.firstOrNull()?.code ?: 0
            }

            val keyBg = if (theme != null) {
                theme!!.getKeyBackground(context, code)
            } else {
                null
            }

            background = keyBg ?: GradientDrawable().apply {
                val customStyle = theme?.getConfig(context)?.key?.customStyle
                if (customStyle != null) {
                    val blur = customStyle.blur
                    val bgColor = try { Color.parseColor(customStyle.backgroundColor) } catch(e: Exception) { 0xFF1E1E2E.toInt() }
                    val alpha = Math.round(Color.alpha(bgColor) * blur)
                    setColor(Color.argb(alpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor)))
                    cornerRadius = dpToPx(customStyle.cornerRadius.toInt()).toFloat()
                    if (customStyle.borderWidth > 0 && customStyle.borderColor.isNotEmpty()) {
                        try {
                            setStroke(dpToPx(customStyle.borderWidth.toInt()), Color.parseColor(customStyle.borderColor))
                        } catch(e: Exception) {}
                    }
                } else {
                    setColor(0xFF1E1E2E.toInt())
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), 0x3300E5FF)
                }
            }

            setOnTouchListener { view, event ->
                handleTouch(view as TextView, key, event)
                true
            }
        }
        return keyView
    }

    var onKeyClickListener: ((KeyDef) -> Unit)? = null
    var isShiftedState = false
        set(value) {
            field = value
            toggleShift(value)
        }

    private fun handleTouch(view: TextView, key: KeyDef, event: MotionEvent) {
        val ime = context as? CustomKeyboardIME
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.isPressed = true
                if (!key.isFunctional) {
                    showPreview(view, getDisplayLabel(key))
                }
                ime?.playClick(key)
                ime?.vibrate()
            }
            MotionEvent.ACTION_UP -> {
                view.isPressed = false
                dismissPreview()
                onKeyClickListener?.invoke(key)
                ime?.handleKeyPress(key)
            }
            MotionEvent.ACTION_CANCEL -> {
                view.isPressed = false
                dismissPreview()
            }
        }
    }

    private fun showPreview(anchor: TextView, text: String) {
        dismissPreview()

        val density = resources.displayMetrics.density
        val width = (60 * density).toInt()
        val height = (80 * density).toInt()

        if (previewTextView == null) {
            previewTextView = TextView(context).apply {
                gravity = Gravity.CENTER
                textSize = 28f
                
                val pTextColor = if (theme != null) {
                    theme!!.previewTextColor(context)
                } else {
                    0xFF00E5FF.toInt()
                }
                setTextColor(pTextColor)

                val popupBgDrawable = if (theme != null) {
                    var drawable: Drawable? = null
                    if (theme!!.path.isNotEmpty()) {
                        val localPopupFile = java.io.File(context.filesDir, "keyboard_themes/${theme!!.path}/popup_background.png")
                        if (localPopupFile.exists()) {
                            try {
                                drawable = Drawable.createFromPath(localPopupFile.absolutePath)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (drawable == null && theme!!.rawType == "default") {
                        try {
                            context.assets.open("${theme!!.getPrefix()}/popup_background.png").use {
                                drawable = Drawable.createFromStream(it, null)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    drawable
                } else {
                    null
                }

                background = popupBgDrawable ?: GradientDrawable().apply {
                    setColor(0xFF1E1E2E.toInt())
                    cornerRadius = (16 * density)
                    setStroke((2 * density).toInt(), 0xFF00E5FF.toInt())
                }
            }
        }

        previewTextView?.text = text
        previewTextView?.typeface = anchor.typeface

        previewPopup = PopupWindow(previewTextView, width, height, false).apply {
            elevation = 15f
            val location = IntArray(2)
            anchor.getLocationInWindow(location)
            val x = location[0] + (anchor.width - width) / 2
            val y = location[1] - height - (8 * density).toInt()
            showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
        }
    }

    private fun dismissPreview() {
        previewPopup?.dismiss()
        previewPopup = null
    }

    private fun getDisplayLabel(key: KeyDef): String {
        val label = key.label
        val ime = context as? CustomKeyboardIME
        val isShifted = if (ime != null) ime.isShifted else isShiftedState
        return if (isShifted && label.length == 1 && label[0].isLetter()) {
            label.uppercase()
        } else {
            label
        }
    }

    fun toggleShift(shifted: Boolean) {
        isShiftedState = shifted
        for (i in 0 until keyViews.size) {
            val keyView = keyViews[i]
            val tag = keyView.tag as? KeyDef ?: continue
            if (!tag.isFunctional && tag.label.length == 1 && tag.label[0].isLetter()) {
                keyView.text = if (shifted) tag.label.uppercase() else tag.label
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
