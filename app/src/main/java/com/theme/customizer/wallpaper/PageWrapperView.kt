package com.theme.customizer.wallpaper

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

/**
 * Khung vẽ chính giả lập tỉ lệ màn hình thiết bị (Aspect Ratio 9:16 trên điện thoại).
 * Cho phép quản lý và phối ghép các thành phần hình nền, sticker, khung ảnh và chữ.
 */
class PageWrapperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivBackground = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private var activeSelection: ResizableView? = null

    init {
        // Thêm background view vào dưới cùng của canvas
        addView(ivBackground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * Khóa tỉ lệ màn hình 9:16 trong chế độ đo đạc nếu chiều cao được định vị.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (height > 0) {
            val desiredWidth = (height * 9) / 16
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * Đặt ảnh nền cho canvas.
     */
    fun setWallpaper(url: String) {
        Glide.with(context)
            .load(url)
            .into(ivBackground)
    }

    /**
     * Thêm sticker vào canvas dưới dạng ResizableView.
     */
    fun addSticker(url: String) {
        val size = (150 * resources.displayMetrics.density).toInt()
        
        val resizableView = ResizableView(context).apply {
            layoutParams = LayoutParams(size, size).apply {
                leftMargin = (this@PageWrapperView.width - size) / 2
                topMargin = (this@PageWrapperView.height - size) / 2
            }
        }

        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        Glide.with(context).load(url).into(imageView)

        // Bọc ImageView bằng ResizableView
        resizableView.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        setupSelectionListener(resizableView)

        addView(resizableView)
        selectComponent(resizableView)
    }

    /**
     * Thêm chữ nghệ thuật vào canvas.
     */
    fun addText(text: String, color: Int) {
        val widthVal = (200 * resources.displayMetrics.density).toInt()
        val heightVal = (80 * resources.displayMetrics.density).toInt()

        val resizableView = ResizableView(context).apply {
            layoutParams = LayoutParams(widthVal, heightVal).apply {
                leftMargin = (this@PageWrapperView.width - widthVal) / 2
                topMargin = (this@PageWrapperView.height - heightVal) / 2
            }
        }

        val textView = TextView(context).apply {
            this.text = text
            this.setTextColor(color)
            this.textSize = 20f
            this.gravity = android.view.Gravity.CENTER
        }

        resizableView.addView(textView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        setupSelectionListener(resizableView)

        addView(resizableView)
        selectComponent(resizableView)
    }

    private fun setupSelectionListener(resizableView: ResizableView) {
        resizableView.setOnClickListener {
            selectComponent(resizableView)
        }
    }

    fun selectComponent(resizableView: ResizableView?) {
        activeSelection?.isSelectedComponent = false
        activeSelection = resizableView
        activeSelection?.isSelectedComponent = true
        // Bring to front
        resizableView?.bringToFront()
    }

    fun clearSelections() {
        activeSelection?.isSelectedComponent = false
        activeSelection = null
    }
}
