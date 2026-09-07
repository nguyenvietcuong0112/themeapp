package com.themes.diy.widgets.keyboard.controlcenter.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.themes.diy.widgets.keyboard.controlcenter.R

class DownloadThemeButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvDownload: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_download_theme_button_view, this, true)
        tvDownload = findViewById(R.id.tvDownload)
        
        // Disable clickable on child ConstraintLayout so clicks propagate to parent FrameLayout
        findViewById<android.view.View>(R.id.innerDownloadContainer)?.let { inner ->
            inner.isClickable = false
            inner.isFocusable = false
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        val scale = if (pressed) 0.96f else 1.0f
        animate().scaleX(scale).scaleY(scale).setDuration(100).start()
    }

    fun setText(text: String) {
        tvDownload.text = text
    }
}

