package com.app.personalization.presentation.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.sqrt

class DIYWallpaperCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var baseType = "solid"
    var baseColor = 0xFF12121A.toInt()
    var gradientStartColor = 0xFF00E5FF.toInt()
    var gradientEndColor = 0xFF7C4DFF.toInt()
    var baseImageBitmap: Bitmap? = null

    val layers = ArrayList<CanvasLayer>()
    private var activeLayer: CanvasLayer? = null

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initialDistance = 1f
    private var initialAngle = 0f
    private var initialScale = 1f
    private var initialRotation = 0f

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setBackgroundSolid(color: Int) {
        baseType = "solid"
        baseColor = color
        baseImageBitmap = null
        invalidate()
    }

    fun setBackgroundGradient(startColor: Int, endColor: Int) {
        baseType = "gradient"
        gradientStartColor = startColor
        gradientEndColor = endColor
        baseImageBitmap = null
        invalidate()
    }

    fun setBackgroundImage(bitmap: Bitmap) {
        baseType = "image"
        baseImageBitmap = bitmap
        invalidate()
    }

    fun addTextLayer(text: String, fontPath: String, color: Int, size: Float) {
        val tf = if (fontPath != "normal" && fontPath.isNotEmpty()) {
            try {
                Typeface.createFromAsset(context.assets, "fonts/$fontPath")
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
        } else {
            Typeface.DEFAULT
        }

        layers.add(
            CanvasLayer(
                type = "text",
                text = text,
                textColor = color,
                textSize = size,
                typeface = tf,
                fontName = fontPath,
                x = width / 2f,
                y = height / 2f
            )
        )
        invalidate()
    }

    fun addStickerLayer(bitmap: Bitmap, resId: Int = 0) {
        layers.add(
            CanvasLayer(
                type = "sticker",
                stickerBitmap = bitmap,
                stickerResId = resId,
                x = width / 2f,
                y = height / 2f
            )
        )
        invalidate()
    }

    fun getActiveTextLayer(): CanvasLayer? {
        return activeLayer?.takeIf { it.type == "text" }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (baseType) {
            "solid" -> {
                canvas.drawColor(baseColor)
            }
            "gradient" -> {
                val shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    gradientStartColor, gradientEndColor,
                    Shader.TileMode.CLAMP
                )
                val paint = Paint().apply { this.shader = shader }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            "image" -> {
                baseImageBitmap?.let { bmp ->
                    val srcRect = android.graphics.Rect(0, 0, bmp.width, bmp.height)
                    val destRect = android.graphics.Rect(0, 0, width, height)
                    canvas.drawBitmap(bmp, srcRect, destRect, null)
                } ?: canvas.drawColor(Color.BLACK)
            }
        }

        for (layer in layers) {
            canvas.save()
            canvas.translate(layer.x, layer.y)
            canvas.rotate(layer.rotation)
            canvas.scale(layer.scale, layer.scale)

            if (layer.type == "text") {
                textPaint.color = layer.textColor
                textPaint.textSize = layer.textSize
                textPaint.typeface = layer.typeface
                
                val fontMetrics = textPaint.fontMetrics
                val baseline = - (fontMetrics.ascent + fontMetrics.descent) / 2
                canvas.drawText(layer.text, 0f, baseline, textPaint)
            } else if (layer.type == "sticker") {
                layer.stickerBitmap?.let { bmp ->
                    canvas.drawBitmap(bmp, -bmp.width / 2f, -bmp.height / 2f, stickerPaint)
                }
            }

            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeLayer = findLayerAt(x, y)
                lastTouchX = x
                lastTouchY = y
                invalidate()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2 && activeLayer != null) {
                    initialDistance = getDistance(event)
                    initialAngle = getAngle(event)
                    initialScale = activeLayer!!.scale
                    initialRotation = activeLayer!!.rotation
                }
            }
            MotionEvent.ACTION_MOVE -> {
                activeLayer?.let { layer ->
                    if (event.pointerCount == 1) {
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY
                        layer.x += dx
                        layer.y += dy
                        lastTouchX = x
                        lastTouchY = y
                    } else if (event.pointerCount == 2) {
                        val currentDistance = getDistance(event)
                        val currentAngle = getAngle(event)
                        
                        val scaleFactor = currentDistance / initialDistance
                        layer.scale = initialScale * scaleFactor
                        
                        val angleDiff = currentAngle - initialAngle
                        layer.rotation = initialRotation + angleDiff
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {}
        }
        return true
    }

    private fun findLayerAt(x: Float, y: Float): CanvasLayer? {
        for (i in layers.size - 1 downTo 0) {
            val layer = layers[i]
            val widthHalf = if (layer.type == "text") layer.textSize * layer.text.length / 3f else (layer.stickerBitmap?.width ?: 100) / 2f
            val heightHalf = if (layer.type == "text") layer.textSize / 2f else (layer.stickerBitmap?.height ?: 100) / 2f
            
            if (x >= layer.x - widthHalf && x <= layer.x + widthHalf &&
                y >= layer.y - heightHalf && y <= layer.y + heightHalf) {
                return layer
            }
        }
        return null
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return srcDiff(dx, dy)
    }

    private fun srcDiff(dx: Float, dy: Float): Float {
        return sqrt(dx * dx + dy * dy)
    }

    private fun getAngle(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        val radians = atan2(dy.toDouble(), dx.toDouble())
        return Math.toDegrees(radians).toFloat()
    }
}

data class CanvasLayer(
    var type: String,
    var text: String = "",
    var textColor: Int = Color.WHITE,
    var textSize: Float = 40f,
    var fontName: String = "normal",
    var typeface: Typeface = Typeface.DEFAULT,
    var stickerBitmap: Bitmap? = null,
    var stickerResId: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var scale: Float = 1.0f,
    var rotation: Float = 0f
)
