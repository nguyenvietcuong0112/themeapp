package com.app.personalization.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.app.personalization.R

class GemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvGem: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_gem_view, this, true)
        tvGem = findViewById(R.id.tvGem)
    }

    fun setCoins(amount: Int) {
        tvGem.text = amount.toString()
    }
}
