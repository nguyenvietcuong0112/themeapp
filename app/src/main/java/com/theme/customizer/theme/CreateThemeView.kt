package com.theme.customizer.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Khung mô phỏng màn hình chủ (CreateThemeView) hiển thị trực quan các thành phần phối ghép.
 */
class CreateThemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val ivBackground = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    val ivWidget = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    val recyclerView = RecyclerView(context).apply {
        layoutManager = GridLayoutManager(context, 4)
    }

    init {
        setBackgroundColor(Color.BLACK)
        
        // Thêm hình nền preview
        addView(ivBackground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        
        // Thêm widget trung tâm giả lập
        val widgetParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (160 * resources.displayMetrics.density).toInt()
        ).apply {
            topMargin = (48 * resources.displayMetrics.density).toInt()
            leftMargin = (24 * resources.displayMetrics.density).toInt()
            rightMargin = (24 * resources.displayMetrics.density).toInt()
        }
        addView(ivWidget, widgetParams)

        // Thêm mạng lưới icons 4 cột
        val recyclerParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ).apply {
            topMargin = (220 * resources.displayMetrics.density).toInt()
            bottomMargin = (16 * resources.displayMetrics.density).toInt()
            leftMargin = (16 * resources.displayMetrics.density).toInt()
            rightMargin = (16 * resources.displayMetrics.density).toInt()
        }
        addView(recyclerView, recyclerParams)
    }

    fun setWallpaper(url: String) {
        Glide.with(context).load(url).into(ivBackground)
    }

    fun setWidget(url: String) {
        Glide.with(context).load(url).into(ivWidget)
    }

    fun setIcons(list: List<String>) {
        recyclerView.adapter = CreateThemeIconAdapter(list)
    }
}
