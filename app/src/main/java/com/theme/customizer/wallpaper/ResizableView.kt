package com.theme.customizer.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Một wrapper View tùy biến bao bọc các thành phần con để hỗ trợ:
 * di chuyển (Drag), co giãn (Scale) và xoay (Rotate) thông qua các cử chỉ và điểm neo.
 */
class ResizableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Trạng thái được chọn
    var isSelectedComponent = false
        set(value) {
            field = value
            invalidate()
        }

    // Các thành phần vẽ khung điều khiển
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val handlePaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
    }

    // Vị trí và kích thước chạm điều khiển
    private val handleRadius = 24f
    private var lastX = 0f
    private var lastY = 0f
    
    // Các chế độ tương tác chạm
    private enum class TouchMode { NONE, DRAG, ROTATE_SCALE }
    private var mode = TouchMode.NONE

    // Tọa độ tâm của view
    private var centerX = 0f
    private var centerY = 0f

    // Góc và độ dài chạm ban đầu
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
            // Vẽ đường viền nét đứt bao quanh thành phần
            canvas.drawRect(
                handleRadius,
                handleRadius,
                width.toFloat() - handleRadius,
                height.toFloat() - handleRadius,
                borderPaint
            )

            // Vẽ điểm neo điều khiển Xoay & Co giãn ở góc dưới bên phải
            canvas.drawCircle(
                width.toFloat() - handleRadius,
                height.toFloat() - handleRadius,
                handleRadius,
                handlePaint
            )

            // Vẽ điểm neo Xóa ở góc trên bên phải (Màu đỏ)
            val deletePaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            canvas.drawCircle(
                width.toFloat() - handleRadius,
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
                
                // Xác định vùng chạm điều khiển
                val localX = event.x
                val localY = event.y

                // Kiểm tra chạm nút xóa (Góc trên bên phải)
                if (isInsideHandle(localX, localY, width - handleRadius, handleRadius)) {
                    onComponentDeleted?.invoke()
                    (parent as? ViewGroup)?.removeView(this)
                    return true
                }

                // Kiểm tra chạm nút xoay/thu phóng (Góc dưới bên phải)
                if (isInsideHandle(localX, localY, width - handleRadius, height - handleRadius)) {
                    mode = TouchMode.ROTATE_SCALE
                    calculateCenter()
                    startAngle = atan2(event.y - centerY, event.x - centerX)
                    startDist = calculateDistance(centerX, centerY, event.x, event.y)
                    startScaleX = scaleX
                    startScaleY = scaleY
                    startRotation = rotation
                } else {
                    // Mặc định là di chuyển layer
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
                    
                    // Tính toán xoay góc
                    val currentAngle = atan2(localY - centerY, localX - centerX)
                    val angleDiff = Math.toDegrees((currentAngle - startAngle).toDouble()).toFloat()
                    rotation = startRotation + angleDiff

                    // Tính toán tỉ lệ co giãn (Scale)
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
