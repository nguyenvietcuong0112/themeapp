package com.themes.diy.widgets.keyboard.controlcenter.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.themes.diy.widgets.keyboard.controlcenter.R

class RemoteImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    enum class LoadingSize {
        SMALL, MEDIUM, LARGE
    }

    fun loadImage(uri: Any?, size: LoadingSize, callback: Any? = null) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.bg_default_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }
}
