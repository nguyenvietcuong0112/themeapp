package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class VerticalSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = 0.5f // 0f to 1f
        set(value) {
            val clamped = value.coerceIn(0f, 1f)
            if (field != clamped) {
                field = clamped
                invalidate()
            }
        }

    private var bgBitmap: Bitmap? = null
    private var slideBitmap: Bitmap? = null
    private var iconBitmap: Bitmap? = null

    private var onProgressChangeListener: ((Float, Boolean) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D000000") // 30% black fallback
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val cornerRadius: Float
        get() = (width / 2f).coerceAtLeast(18f * resources.displayMetrics.density)

    private val clipPath = Path()
    private val viewRect = RectF()
    private val fillRect = RectF()
    private val iconRect = RectF()

    fun setAssets(bg: Bitmap?, slide: Bitmap?, icon: Bitmap?) {
        this.bgBitmap = bg
        this.slideBitmap = slide
        this.iconBitmap = icon
        invalidate()
    }

    fun setOnProgressChangeListener(listener: (Float, Boolean) -> Unit) {
        this.onProgressChangeListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val r = w / 2f
        viewRect.set(0f, 0f, w.toFloat(), h.toFloat())
        clipPath.reset()
        clipPath.addRoundRect(viewRect, r, r, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.clipPath(clipPath)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw Frosted Base Background
        canvas.drawRoundRect(viewRect, cornerRadius, cornerRadius, bgPaint)

        bgBitmap?.let { bg ->
            canvas.drawBitmap(bg, null, viewRect, iconPaint)
        }

        // 2. Draw Progress Fill (from bottom upwards)
        val fillHeight = h * progress
        val fillTop = h - fillHeight
        fillRect.set(0f, fillTop, w, h)

        slideBitmap?.let { slide ->
            canvas.save()
            canvas.clipRect(fillRect)
            canvas.drawBitmap(slide, null, viewRect, iconPaint)
            canvas.restore()
        } ?: run {
            canvas.drawRect(fillRect, fillPaint)
        }

        // 3. Draw Bottom Center Icon
        iconBitmap?.let { icon ->
            val iconSize = (w * 0.40f).coerceAtLeast(36f)
            val iconLeft = (w - iconSize) / 2f
            val iconBottom = h - (w * 0.30f)
            val iconTop = iconBottom - iconSize
            iconRect.set(iconLeft, iconTop, iconLeft + iconSize, iconBottom)
            canvas.drawBitmap(icon, null, iconRect, iconPaint)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val h = height.toFloat()
                if (h > 0) {
                    val y = event.y.coerceIn(0f, h)
                    val newProgress = (1f - (y / h)).coerceIn(0f, 1f)
                    progress = newProgress
                    onProgressChangeListener?.invoke(progress, true)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                onProgressChangeListener?.invoke(progress, false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
