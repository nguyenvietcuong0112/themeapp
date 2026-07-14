package com.app.personalization.presentation.theme

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.app.personalization.databinding.ItemCreateThemeViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class CreateThemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding: ItemCreateThemeViewBinding = ItemCreateThemeViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        // Setup initial recycler settings
        binding.recyclerView.layoutManager = GridLayoutManager(context, 4)
        binding.recyclerView.adapter = CreateThemeIconAdapter(emptyList())
    }

    fun setWallpaper(url: String) {
        if (url.isEmpty()) {
            binding.ivBackground.setImageDrawable(null)
            return
        }
        binding.pbLoading.visibility = View.VISIBLE
        Glide.with(context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pbLoading.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pbLoading.visibility = View.GONE
                    return false
                }
            })
            .into(binding.ivBackground)
    }

    fun setWidget(url: String) {
        if (url.isEmpty()) {
            binding.ivWidget.setImageDrawable(null)
            return
        }
        Glide.with(context)
            .load(url)
            .into(binding.ivWidget)
    }

    fun setIcons(list: List<String>) {
        val adapter = CreateThemeIconAdapter(list)
        binding.recyclerView.adapter = adapter
    }
}
