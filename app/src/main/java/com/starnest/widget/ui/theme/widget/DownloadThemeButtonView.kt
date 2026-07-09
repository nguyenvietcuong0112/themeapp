package com.starnest.widget.ui.theme.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.app.personalization.R

class DownloadThemeButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvDownload: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_download_theme_button_view, this, true)
        tvDownload = findViewById(R.id.tvDownload)
    }

    fun setText(text: String) {
        tvDownload.text = text
    }
}
