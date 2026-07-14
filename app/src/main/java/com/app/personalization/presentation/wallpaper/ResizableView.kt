package com.app.personalization.presentation.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.app.personalization.R
import com.app.personalization.data.database.entity.PageComponent
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Khung điều khiển (ResizableView) bao bọc từng vật thể con trên canvas.
 * Hỗ trợ co giãn, xoay và xóa lớp thành phần. Sử dụng drawable nét đứt bg_canvas_border.xml.
 */
class ResizableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var pageComponent: PageComponent? = null
    var isSelectedComponent = false
        set(value) {
            field = value
            invalidate()
        }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
    }

    private val deletePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF819F")
        style = Paint.Style.FILL
    }

    private val handleRadius = 24f
    private var lastX = 0f
    private var lastY = 0f
    
    private enum class TouchMode { NONE, DRAG, ROTATE_SCALE }
    private var mode = TouchMode.NONE

    private var centerX = 0f
    private var centerY = 0f

    private var startAngle = 0f
    private var startDist = 0f
    private var startScaleX = 1f
    private var startScaleY = 1f
    private var startRotation = 0f

    var onComponentDeleted: (() -> Unit)? = null

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSelectedComponent) {
            // Vẽ viền nét đứt từ bg_canvas_border.xml
            val borderDrawable = context.getDrawable(R.drawable.bg_canvas_border)
            borderDrawable?.let {
                it.setBounds(
                    handleRadius.toInt(),
                    handleRadius.toInt(),
                    (width - handleRadius).toInt(),
                    (height - handleRadius).toInt()
                )
                it.draw(canvas)
            }

            // Vẽ nút neo xoay/co giãn (Góc dưới bên phải)
            canvas.drawCircle(
                width - handleRadius,
                height - handleRadius,
                handleRadius,
                handlePaint
            )

            // Vẽ nút neo xóa (Góc trên bên phải)
            canvas.drawCircle(
                width - handleRadius,
                handleRadius,
                handleRadius,
                deletePaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSelectedComponent) return false

        val x = event.rawX
        val y = event.rawY

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                
                val localX = event.x
                val localY = event.y

                // Bấm nút xóa (Góc trên bên phải)
                if (isInsideHandle(localX, localY, width - handleRadius, handleRadius)) {
                    onComponentDeleted?.invoke()
                    (parent as? ViewGroup)?.removeView(this)
                    return true
                }

                // Bấm nút xoay/thu phóng (Góc dưới bên phải)
                if (isInsideHandle(localX, localY, width - handleRadius, height - handleRadius)) {
                    mode = TouchMode.ROTATE_SCALE
                    calculateCenter()
                    startAngle = atan2(event.y - centerY, event.x - centerX)
                    startDist = calculateDistance(centerX, centerY, event.x, event.y)
                    startScaleX = scaleX
                    startScaleY = scaleY
                    startRotation = rotation
                } else {
                    mode = TouchMode.DRAG
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == TouchMode.DRAG) {
                    val dx = x - lastX
                    val dy = y - lastY
                    translationX += dx
                    translationY += dy
                    lastX = x
                    lastY = y
                } else if (mode == TouchMode.ROTATE_SCALE) {
                    val localX = event.x
                    val localY = event.y
                    
                    val currentAngle = atan2(localY - centerY, localX - centerX)
                    val angleDiff = Math.toDegrees((currentAngle - startAngle).toDouble()).toFloat()
                    rotation = startRotation + angleDiff

                    val currentDist = calculateDistance(centerX, centerY, localX, localY)
                    if (startDist > 10f) {
                        val scaleFactor = currentDist / startDist
                        scaleX = (startScaleX * scaleFactor).coerceIn(0.2f, 5.0f)
                        scaleY = (startScaleY * scaleFactor).coerceIn(0.2f, 5.0f)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mode = TouchMode.NONE
            }
        }
        return true
    }

    private fun isInsideHandle(x: Float, y: Float, hX: Float, hY: Float): Boolean {
        val dx = x - hX
        val dy = y - hY
        return sqrt((dx * dx + dy * dy).toDouble()) <= handleRadius * 2
    }

    private fun calculateCenter() {
        centerX = width / 2f
        centerY = height / 2f
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
