package com.app.personalization.presentation.wallpaper

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.app.personalization.data.database.entity.PageComponent
import java.util.UUID

/**
 * Khung vẽ trung tâm (PageWrapperView) quản lý và dựng lại các PageComponent lên tọa độ tương ứng.
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
    var backgroundUri: String = ""

    init {
        addView(ivBackground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

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

    fun setWallpaper(url: String) {
        this.backgroundUri = url
        Glide.with(context)
            .load(url)
            .into(ivBackground)
    }

    /**
     * Dựng lại toàn bộ các PageComponent nhận được từ DesignPage lên bản vẽ.
     */
    fun loadPageComponents(components: List<PageComponent>) {
        // Xóa sạch các view cũ (trừ background)
        for (i in childCount - 1 downTo 1) {
            removeViewAt(i)
        }
        activeSelection = null

        val density = resources.displayMetrics.density
        val defaultSize = (140 * density).toInt()

        for (comp in components) {
            val resizableView = ResizableView(context).apply {
                this.pageComponent = comp
                translationX = comp.x
                translationY = comp.y
                scaleX = comp.width
                scaleY = comp.height
                rotation = comp.rotationAngle
                alpha = comp.alpha
                layoutParams = LayoutParams(defaultSize, defaultSize)
            }

            when (comp.componentType) {
                "STICKER" -> {
                    val imageView = ImageView(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                    Glide.with(context).load(comp.stickerPath).into(imageView)
                    resizableView.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                }
                "TEXT" -> {
                    val textView = TextView(context).apply {
                        text = comp.text
                        setTextColor(comp.textColor ?: android.graphics.Color.WHITE)
                        textSize = 20f
                        gravity = android.view.Gravity.CENTER
                    }
                    comp.fontPath?.let { font ->
                        try {
                            if (font.isNotEmpty()) {
                                textView.typeface = android.graphics.Typeface.createFromAsset(context.assets, "fonts/$font")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    resizableView.addView(textView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                }
                "SHAPE" -> {
                    val shapeView = View(context).apply {
                        setBackgroundColor(comp.strokeColor ?: android.graphics.Color.RED)
                    }
                    resizableView.addView(shapeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                }
            }

            setupSelectionListener(resizableView)
            addView(resizableView)
        }
    }

    fun addSticker(url: String) {
        val size = (140 * resources.displayMetrics.density).toInt()
        val pageComp = PageComponent(
            id = UUID.randomUUID().toString(),
            componentType = "STICKER",
            x = 0f,
            y = 0f,
            width = 1f,
            height = 1f,
            rotationAngle = 0f,
            zIndex = childCount,
            alpha = 1f,
            stickerPath = url
        )

        val resizableView = ResizableView(context).apply {
            this.pageComponent = pageComp
            layoutParams = LayoutParams(size, size)
        }

        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        Glide.with(context).load(url).into(imageView)
        resizableView.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        setupSelectionListener(resizableView)
        addView(resizableView)
        selectComponent(resizableView)
    }

    fun addText(text: String, color: Int, font: String? = null) {
        val widthVal = (200 * resources.displayMetrics.density).toInt()
        val heightVal = (80 * resources.displayMetrics.density).toInt()
        
        val pageComp = PageComponent(
            id = UUID.randomUUID().toString(),
            componentType = "TEXT",
            x = 0f,
            y = 0f,
            width = 1f,
            height = 1f,
            rotationAngle = 0f,
            zIndex = childCount,
            alpha = 1f,
            text = text,
            textColor = color,
            fontPath = font
        )

        val resizableView = ResizableView(context).apply {
            this.pageComponent = pageComp
            layoutParams = LayoutParams(widthVal, heightVal)
        }

        val textView = TextView(context).apply {
            this.text = text
            this.setTextColor(color)
            this.textSize = 20f
            this.gravity = android.view.Gravity.CENTER
            font?.let {
                try {
                    if (it.isNotEmpty()) {
                        typeface = android.graphics.Typeface.createFromAsset(context.assets, "fonts/$it")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
        resizableView?.bringToFront()
    }

    fun clearSelections() {
        activeSelection?.isSelectedComponent = false
        activeSelection = null
    }

    /**
     * Thu thập danh sách PageComponent hiện tại để lưu trạng thái chỉnh sửa.
     */
    fun getPageComponents(): ArrayList<PageComponent> {
        val list = arrayListOf<PageComponent>()
        for (i in 1 until childCount) {
            val child = getChildAt(i)
            if (child is ResizableView) {
                val comp = child.pageComponent ?: continue
                val updated = comp.copy(
                    x = child.translationX,
                    y = child.translationY,
                    width = child.scaleX,
                    height = child.scaleY,
                    rotationAngle = child.rotation,
                    alpha = child.alpha,
                    zIndex = i
                )
                list.add(updated)
            }
        }
        return list
    }
}
