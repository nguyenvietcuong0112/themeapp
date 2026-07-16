package com.app.personalization.presentation.wallpaper

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.json.JSONObject
import java.io.InputStream
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
    var activeLayer: CanvasLayer? = null

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initialDistance = 1f
    private var initialAngle = 0f
    private var initialScale = 1f
    private var initialRotation = 0f

    private var isDragging = false

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    var onFrameClickListener: ((CanvasLayer) -> Unit)? = null

    private var pendingTemplateFolder: String? = null

    // Undo / Redo History States
    private val undoList = ArrayList<HistoryState>()
    private val redoList = ArrayList<HistoryState>()

    fun saveToHistory() {
        val state = HistoryState(
            baseType = baseType,
            baseColor = baseColor,
            gradientStartColor = gradientStartColor,
            gradientEndColor = gradientEndColor,
            baseImageBitmap = baseImageBitmap,
            layers = layers.map { it.clone() }
        )
        undoList.add(state)
        if (undoList.size > 30) {
            undoList.removeAt(0)
        }
        redoList.clear()
    }

    fun undo() {
        if (undoList.isNotEmpty()) {
            val currentState = HistoryState(
                baseType = baseType,
                baseColor = baseColor,
                gradientStartColor = gradientStartColor,
                gradientEndColor = gradientEndColor,
                baseImageBitmap = baseImageBitmap,
                layers = layers.map { it.clone() }
            )
            redoList.add(currentState)

            val prevState = undoList.removeAt(undoList.size - 1)
            baseType = prevState.baseType
            baseColor = prevState.baseColor
            gradientStartColor = prevState.gradientStartColor
            gradientEndColor = prevState.gradientEndColor
            baseImageBitmap = prevState.baseImageBitmap

            layers.clear()
            layers.addAll(prevState.layers)
            activeLayer = null
            invalidate()
        }
    }

    fun redo() {
        if (redoList.isNotEmpty()) {
            val currentState = HistoryState(
                baseType = baseType,
                baseColor = baseColor,
                gradientStartColor = gradientStartColor,
                gradientEndColor = gradientEndColor,
                baseImageBitmap = baseImageBitmap,
                layers = layers.map { it.clone() }
            )
            undoList.add(currentState)

            val nextState = redoList.removeAt(redoList.size - 1)
            baseType = nextState.baseType
            baseColor = nextState.baseColor
            gradientStartColor = nextState.gradientStartColor
            gradientEndColor = nextState.gradientEndColor
            baseImageBitmap = nextState.baseImageBitmap

            layers.clear()
            layers.addAll(nextState.layers)
            activeLayer = null
            invalidate()
        }
    }

    fun setBackgroundSolid(color: Int) {
        saveToHistory()
        baseType = "solid"
        baseColor = color
        baseImageBitmap = null
        invalidate()
    }

    fun setBackgroundGradient(startColor: Int, endColor: Int) {
        saveToHistory()
        baseType = "gradient"
        gradientStartColor = startColor
        gradientEndColor = endColor
        baseImageBitmap = null
        invalidate()
    }

    fun setBackgroundImage(bitmap: Bitmap) {
        saveToHistory()
        baseType = "image"
        baseImageBitmap = bitmap
        invalidate()
    }

    fun addTextLayer(text: String, fontPath: String, color: Int, size: Float) {
        saveToHistory()
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
                y = height / 2f,
                width = width * 0.6f,
                height = size * 1.5f
            )
        )
        invalidate()
    }

    fun addStickerLayer(bitmap: Bitmap, resId: Int = 0) {
        saveToHistory()
        layers.add(
            CanvasLayer(
                type = "sticker",
                stickerBitmap = bitmap,
                stickerResId = resId,
                x = width / 2f,
                y = height / 2f,
                width = bitmap.width.toFloat(),
                height = bitmap.height.toFloat()
            )
        )
        invalidate()
    }

    fun getActiveTextLayer(): CanvasLayer? {
        return activeLayer?.takeIf { it.type == "text" }
    }

    fun loadTemplate(templateFolder: String) {
        if (width == 0 || height == 0) {
            pendingTemplateFolder = templateFolder
            return
        }
        pendingTemplateFolder = null
        saveToHistory()
        layers.clear()
        activeLayer = null

        try {
            val jsonString = context.assets.open("templates/$templateFolder/config.json").use { input ->
                input.bufferedReader().use { it.readText() }
            }

            val root = JSONObject(jsonString)
            val templatePages = root.getJSONArray("templatePages")
            if (templatePages.length() > 0) {
                val pageObj = templatePages.getJSONObject(0)
                val pagesArr = pageObj.getJSONArray("pages")
                if (pagesArr.length() > 0) {
                    val subPage = pagesArr.getJSONObject(0)

                    // Read base template coordinate size
                    var templateWidth = 1130f
                    var templateHeight = 2388f
                    if (subPage.has("size")) {
                        val sizeStr = subPage.getString("size")
                        val parts = sizeStr.split(",")
                        if (parts.size == 2) {
                            templateWidth = parts[0].trim().toFloat()
                            templateHeight = parts[1].trim().toFloat()
                        }
                    }

                    // Scaling factors to fit current screen/canvas size
                    val scaleFactorX = width.toFloat() / templateWidth
                    val scaleFactorY = height.toFloat() / templateHeight

                    // Read background color or image
                    if (subPage.has("colorPage")) {
                        val colorStr = subPage.getString("colorPage")
                        setBackgroundSolid(Color.parseColor(colorStr))
                    }

                    if (subPage.has("bgImage")) {
                        val bgImgPath = subPage.getString("bgImage")
                        val bgUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/$bgImgPath"
                        loadBackgroundImageFromUrl(bgUrl)
                    }

                    val components = subPage.getJSONArray("components")
                    for (i in 0 until components.length()) {
                        val comp = components.getJSONObject(i)
                        val type = comp.getString("type")

                        val pos = comp.getJSONObject("position")
                        val left = pos.optDouble("left", 0.0).toFloat()
                        val top = pos.optDouble("top", 0.0).toFloat()

                        var w = 200f
                        var h = 200f
                        if (comp.has("size")) {
                            val sizeStr = comp.getString("size")
                            val parts = sizeStr.split(",")
                            if (parts.size == 2) {
                                w = parts[0].trim().toFloat()
                                h = parts[1].trim().toFloat()
                            }
                        }

                        val angle = comp.optDouble("angle", 0.0).toFloat()
                        val rotation = comp.optDouble("rotation", 0.0).toFloat()
                        val finalRotation = if (angle != 0f) angle else rotation

                        val finalW = w * scaleFactorX
                        val finalH = h * scaleFactorY
                        val finalX = (left + w / 2f) * scaleFactorX
                        val finalY = (top + h / 2f) * scaleFactorY

                        val layer = CanvasLayer(type = type).apply {
                            this.x = finalX
                            this.y = finalY
                            this.width = finalW
                            this.height = finalH
                            this.rotation = finalRotation
                        }

                        when (type) {
                            "text" -> {
                                layer.text = comp.optString("text", "Text")
                                if (comp.has("fontStyle")) {
                                    val fontStyle = comp.getJSONObject("fontStyle")
                                    if (fontStyle.has("color")) {
                                        layer.textColor = Color.parseColor(fontStyle.getString("color"))
                                    }
                                    if (fontStyle.has("size")) {
                                        layer.textSize = fontStyle.optDouble("size", 40.0).toFloat() * scaleFactorY
                                    }
                                    layer.fontName = fontStyle.optString("font", "normal")
                                    if (layer.fontName != "normal" && layer.fontName.isNotEmpty()) {
                                        layer.typeface = try {
                                            Typeface.createFromAsset(context.assets, "fonts/${layer.fontName}")
                                        } catch (e: Exception) {
                                            Typeface.DEFAULT
                                        }
                                    }
                                }
                            }
                            "sticker" -> {
                                val imgUrl = comp.getString("imageUrl")
                                val cleanUrl = if (imgUrl.contains("template")) "designs/$imgUrl" else imgUrl
                                layer.stickerUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/$cleanUrl"
                                loadStickerBitmap(layer)
                            }
                            "image_template" -> {
                                layer.bgMaskImageUrl = comp.getString("bgMaskImageUrl")
                                val sampleImg = comp.getString("sampleImageUrl")
                                layer.sampleImageUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/templates/designs/$sampleImg"
                                updateMaskedBitmap(layer)
                                loadFrameSampleBitmap(layer)
                            }
                        }
                        layers.add(layer)
                    }
                }
            }
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBackgroundImageFromUrl(url: String) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .listener(object : com.bumptech.glide.request.RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }
                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    setBackgroundImage(resource)
                }
                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    private fun loadStickerBitmap(layer: CanvasLayer) {
        val url = layer.stickerUrl ?: return
        Glide.with(context)
            .asBitmap()
            .load(url)
            .listener(object : com.bumptech.glide.request.RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }
                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    layer.stickerBitmap = resource
                    invalidate()
                }
                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    private fun loadFrameSampleBitmap(layer: CanvasLayer) {
        val url = layer.sampleImageUrl ?: return
        Glide.with(context)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : com.bumptech.glide.request.RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }
                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Bitmap>,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    layer.sampleBitmap = resource
                    updateMaskedBitmap(layer)
                    invalidate()
                }
                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    fun updateMaskedBitmap(layer: CanvasLayer) {
        val w = layer.width.toInt().coerceAtLeast(1)
        val h = layer.height.toInt().coerceAtLeast(1)

        try {
            val maskBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(maskBmp)

            layer.bgMaskImageUrl?.let { svgPath ->
                try {
                    val svg = com.caverock.androidsvg.SVG.getFromAsset(context.assets, svgPath)
                    val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
                    svg.renderToCanvas(maskCanvas, rect)
                } catch (e: Exception) {
                    val paint = Paint().apply { color = Color.WHITE }
                    maskCanvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
                }
            }

            val resultBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val resultCanvas = Canvas(resultBmp)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            resultCanvas.drawBitmap(maskBmp, 0f, 0f, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val bmp = layer.userImageBitmap ?: layer.sampleBitmap
            if (bmp != null) {
                val srcRect = Rect(0, 0, bmp.width, bmp.height)
                val destRect = Rect(0, 0, w, h)
                resultCanvas.drawBitmap(bmp, srcRect, destRect, paint)
            } else {
                val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E5E7EB")
                    style = Paint.Style.FILL
                }
                resultCanvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), colorPaint)
            }
            paint.xfermode = null

            layer.maskedBitmap?.recycle()
            layer.maskedBitmap = resultBmp

            maskBmp.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserImageForActiveFrame(uri: Uri, bitmap: Bitmap) {
        activeLayer?.let { layer ->
            if (layer.type == "image_template") {
                saveToHistory()
                layer.userImageUri = uri.toString()
                layer.userImageBitmap = bitmap
                updateMaskedBitmap(layer)
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pendingTemplateFolder?.let {
            loadTemplate(it)
        }
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
                    val srcRect = Rect(0, 0, bmp.width, bmp.height)
                    val destRect = Rect(0, 0, width, height)
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
            } else if (layer.type == "image_template") {
                layer.maskedBitmap?.let { bmp ->
                    canvas.drawBitmap(bmp, -layer.width / 2f, -layer.height / 2f, stickerPaint)
                }

                // If user hasn't selected their own photo yet, draw a '+' indicator in the center
                if (layer.userImageUri == null) {
                    val radius = 40f
                    val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#80FFFFFF") // 50% translucent white
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(0f, 0f, radius, indicatorPaint)

                    val plusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        strokeWidth = 6f
                        style = Paint.Style.STROKE
                    }
                    canvas.drawLine(-15f, 0f, 15f, 0f, plusPaint)
                    canvas.drawLine(0f, -15f, 0f, 15f, plusPaint)
                }
            }

            // Draw border if active/selected layer
            if (layer === activeLayer) {
                val halfW = layer.width / 2f
                val halfH = layer.height / 2f
                canvas.drawRect(-halfW - 8f, -halfH - 8f, halfW + 8f, halfH + 8f, borderPaint)
            }

            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val touchedLayer = findLayerAt(x, y)
                activeLayer = touchedLayer
                lastTouchX = x
                lastTouchY = y
                isDragging = false
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
                    if (!isDragging) {
                        saveToHistory()
                    }
                    isDragging = true
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
                        layer.scale = (initialScale * scaleFactor).coerceIn(0.2f, 5.0f)

                        val angleDiff = currentAngle - initialAngle
                        layer.rotation = initialRotation + angleDiff
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                activeLayer?.let { layer ->
                    val dist = srcDiff(x - lastTouchX, y - lastTouchY)
                    if (dist < 15f && layer.type == "image_template") {
                        onFrameClickListener?.invoke(layer)
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {}
        }
        return true
    }

    private fun findLayerAt(x: Float, y: Float): CanvasLayer? {
        for (i in layers.size - 1 downTo 0) {
            val layer = layers[i]
            val widthHalf = (layer.width * layer.scale) / 2f
            val heightHalf = (layer.height * layer.scale) / 2f

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
    var stickerUrl: String? = null,
    var stickerBitmap: Bitmap? = null,
    var stickerResId: Int = 0,
    var sampleImageUrl: String? = null,
    var sampleBitmap: Bitmap? = null,
    var bgMaskImageUrl: String? = null,
    var userImageUri: String? = null,
    var userImageBitmap: Bitmap? = null,
    var maskedBitmap: Bitmap? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var scale: Float = 1.0f,
    var rotation: Float = 0f
) {
    fun clone(): CanvasLayer {
        return CanvasLayer(
            type = type,
            text = text,
            textColor = textColor,
            textSize = textSize,
            fontName = fontName,
            typeface = typeface,
            stickerUrl = stickerUrl,
            stickerBitmap = stickerBitmap,
            stickerResId = stickerResId,
            sampleImageUrl = sampleImageUrl,
            sampleBitmap = sampleBitmap,
            bgMaskImageUrl = bgMaskImageUrl,
            userImageUri = userImageUri,
            userImageBitmap = userImageBitmap,
            maskedBitmap = maskedBitmap,
            x = x,
            y = y,
            width = width,
            height = height,
            scale = scale,
            rotation = rotation
        )
    }
}

data class HistoryState(
    val baseType: String,
    val baseColor: Int,
    val gradientStartColor: Int,
    val gradientEndColor: Int,
    val baseImageBitmap: Bitmap?,
    val layers: List<CanvasLayer>
)
