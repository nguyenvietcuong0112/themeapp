package com.app.personalization.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.data.database.entity.WidgetItem
import com.app.personalization.data.database.entity.WidgetSize
import com.app.personalization.di.ServiceLocator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WidgetRenderHelper {

    fun getTargetPixelSize(widgetSize: WidgetSize): android.util.Size {
        return when (widgetSize) {
            WidgetSize.SMALL -> android.util.Size(500, 500)      // 2x2
            WidgetSize.MEDIUM -> android.util.Size(1000, 500)    // 4x2
            WidgetSize.LARGE -> android.util.Size(1000, 1000)    // 4x4
        }
    }

    fun getSnapshotImage(
        context: Context,
        layoutId: Int,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        preloadedBackground: Bitmap? = null
    ): Bitmap? {
        val view = LayoutInflater.from(context).inflate(layoutId, null, false) ?: return null
        val targetSize = getTargetPixelSize(widgetSize)

        bindDataToView(context, view, widgetSize, widgetItem, widgetId, preloadedBackground)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(targetSize.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(targetSize.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, targetSize.width, targetSize.height)

        val bitmap = Bitmap.createBitmap(targetSize.width, targetSize.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val cornerRadius = targetSize.height * 0.16f
        return roundBitmapCorners(bitmap, cornerRadius)
    }

    private fun bindDataToView(
        context: Context,
        view: View,
        widgetSize: WidgetSize,
        widgetItem: WidgetItem,
        widgetId: Int,
        preloadedBackground: Bitmap? = null
    ) {
        val config = if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)
        } else {
            null
        }

        // 1. Populate Background
        val ivBackground = view.findViewById<ImageView>(R.id.ivBackground)
        if (ivBackground != null) {
            var bgBmp: Bitmap? = preloadedBackground
            if (bgBmp == null) {
                if (config != null && config.bgType == "IMAGE" && config.imageUri != null) {
                    try {
                        val file = File(Uri.parse(config.imageUri).path ?: "")
                        if (file.exists()) {
                            bgBmp = BitmapFactory.decodeFile(file.absolutePath)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (bgBmp == null) {
                try {
                    val sizeStr = when (widgetSize) {
                        WidgetSize.SMALL -> "2x2"
                        WidgetSize.MEDIUM -> "4x2"
                        WidgetSize.LARGE -> "4x4"
                    }
                    val fileName = "widget_bg_${widgetItem.id}_${widgetItem.widgetType}_$sizeStr.png"
                    val file = context.getFileStreamPath(fileName)
                    if (file.exists()) {
                        bgBmp = BitmapFactory.decodeFile(file.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (bgBmp != null) {
                ivBackground.setImageBitmap(bgBmp)
            } else {
                ivBackground.setBackgroundColor(0xFF1E1E2E.toInt())
            }
        }

        // 2. Populate components based on size
        when (widgetSize) {
            WidgetSize.SMALL -> {
                val ivClock = view.findViewById<ImageView>(R.id.ivClock)
                if (ivClock != null) {
                    if (widgetItem.widgetType.lowercase() == "clock") {
                        ivClock.visibility = View.VISIBLE
                        val dialSize = 512
                        val clockBmp = Bitmap.createBitmap(dialSize, dialSize, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(clockBmp)

                        val themeFolder = widgetItem.themeFolder
                        val styleId = themeFolder.filter { it.isDigit() }.toIntOrNull()?.let {
                            (it % 29).let { if (it < 0) -it else it } + 1
                        } ?: 1

                        val dialResId = context.resources.getIdentifier("widget_clock_${styleId}_dial", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_dial else it }
                        val hourResId = context.resources.getIdentifier("widget_clock_${styleId}_hours", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_hours else it }
                        val minResId = context.resources.getIdentifier("widget_clock_${styleId}_minutes", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_minutes else it }
                        val secResId = context.resources.getIdentifier("widget_clock_${styleId}_seconds", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_seconds else it }

                        val dialBmp = BitmapFactory.decodeResource(context.resources, dialResId)
                        val hourBmp = BitmapFactory.decodeResource(context.resources, hourResId)
                        val minBmp = BitmapFactory.decodeResource(context.resources, minResId)
                        val secBmp = BitmapFactory.decodeResource(context.resources, secResId)

                        val destRect = Rect(0, 0, dialSize, dialSize)
                        if (dialBmp != null) {
                            canvas.drawBitmap(dialBmp, null, destRect, null)
                        } else {
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.WHITE
                                style = Paint.Style.STROKE
                                strokeWidth = 8f
                            }
                            canvas.drawCircle(dialSize / 2f, dialSize / 2f, dialSize / 2f - 16f, paint)
                        }

                        val cal = Calendar.getInstance()
                        val hour = cal.get(Calendar.HOUR)
                        val minute = cal.get(Calendar.MINUTE)
                        val second = cal.get(Calendar.SECOND)

                        val hrAngle = (hour % 12) * 30f + minute * 0.5f
                        val minAngle = minute * 6f + second * 0.1f
                        val secAngle = second * 6f

                        drawRotatedHand(canvas, hourBmp, hrAngle, dialSize)
                        drawRotatedHand(canvas, minBmp, minAngle, dialSize)
                        drawRotatedHand(canvas, secBmp, secAngle, dialSize)

                        ivClock.setImageBitmap(clockBmp)
                    } else {
                        ivClock.visibility = View.GONE
                    }
                }
            }
            WidgetSize.MEDIUM -> {
                val tvTime = view.findViewById<TextView>(R.id.tvTime)
                val tvDate = view.findViewById<TextView>(R.id.tvDate)
                val ivWeatherIcon = view.findViewById<ImageView>(R.id.ivWeatherIcon)
                val tvWeatherTemp = view.findViewById<TextView>(R.id.tvWeatherTemp)

                if (widgetItem.widgetType.lowercase() == "calendar") {
                    tvTime?.visibility = View.GONE
                    tvDate?.visibility = View.GONE
                    ivWeatherIcon?.visibility = View.GONE
                    tvWeatherTemp?.visibility = View.GONE
                } else {
                    tvTime?.visibility = View.VISIBLE
                    tvDate?.visibility = View.VISIBLE
                    ivWeatherIcon?.visibility = View.VISIBLE
                    tvWeatherTemp?.visibility = View.VISIBLE

                    val textColor = config?.textColor ?: Color.WHITE
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                    val now = Date()

                    tvTime?.text = timeFormat.format(now)
                    tvTime?.setTextColor(textColor)

                    tvDate?.text = dateFormat.format(now)
                    tvDate?.setTextColor(textColor)

                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    val weatherText = prefs.getString("weather_temp", "26°C Sunny") ?: "26°C Sunny"
                    tvWeatherTemp?.text = weatherText
                    tvWeatherTemp?.setTextColor(textColor)

                    val iconCode = when {
                        weatherText.contains("sunny", ignoreCase = true) -> "sunny"
                        weatherText.contains("cloud", ignoreCase = true) -> "cloudy"
                        weatherText.contains("rain", ignoreCase = true) -> "rainy"
                        else -> "sunny"
                    }
                    val weatherIconId = context.resources.getIdentifier("ic_weather_${iconCode}", "drawable", context.packageName).let { if (it == 0) R.drawable.ic_style_weather else it }
                    ivWeatherIcon?.setImageResource(weatherIconId)
                }
            }
            WidgetSize.LARGE -> {
                val ivCalendar = view.findViewById<ImageView>(R.id.ivCalendar)
                if (ivCalendar != null) {
                    val calSize = 1000
                    val calBmp = Bitmap.createBitmap(calSize, calSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(calBmp)

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val monthMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

                    val textColor = config?.textColor ?: Color.WHITE

                    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColor
                        textSize = 64f
                        textAlign = Paint.Align.CENTER
                        style = Paint.Style.FILL
                    }
                    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    canvas.drawText(monthFormat.format(Date()), calSize / 2f, 150f, titlePaint)

                    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColor
                        textSize = 40f
                        textAlign = Paint.Align.CENTER
                        alpha = 180
                    }
                    val days = arrayOf("S", "M", "T", "W", "T", "F", "S")
                    val cellWidth = calSize / 7f
                    val gridTop = 260f
                    val cellHeight = (calSize - gridTop) / 7f

                    for (i in days.indices) {
                        val x = i * cellWidth + cellWidth / 2f
                        canvas.drawText(days[i], x, gridTop, headerPaint)
                    }

                    val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = 44f
                        textAlign = Paint.Align.CENTER
                    }

                    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    var row = 1
                    for (day in 1..monthMax) {
                        val col = (startDayOfWeek + day - 1) % 7
                        val x = col * cellWidth + cellWidth / 2f
                        val y = gridTop + row * cellHeight

                        if (day == today) {
                            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textColor
                                style = Paint.Style.STROKE
                                strokeWidth = 6f
                            }
                            canvas.drawCircle(x, y - 15f, 35f, circlePaint)
                            dayPaint.color = textColor
                            dayPaint.isFakeBoldText = true
                        } else {
                            dayPaint.color = textColor
                            dayPaint.isFakeBoldText = false
                        }

                        canvas.drawText(day.toString(), x, y, dayPaint)

                        if (col == 6) {
                            row++
                        }
                    }

                    ivCalendar.setImageBitmap(calBmp)
                }
            }
        }
    }

    private fun drawRotatedHand(canvas: Canvas, handBmp: Bitmap?, angle: Float, size: Int) {
        if (handBmp == null) return
        val scale = size.toFloat() / handBmp.height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val dx = (size / 2f) - (handBmp.width / 2f * scale)
        val dy = (size / 2f) - (handBmp.height / 2f * scale)
        matrix.postTranslate(dx, dy)
        matrix.postRotate(angle, size / 2f, size / 2f)
        canvas.drawBitmap(handBmp, matrix, null)
    }

    private fun roundBitmapCorners(src: Bitmap, radius: Float): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
            shader = android.graphics.BitmapShader(
                src, 
                android.graphics.Shader.TileMode.CLAMP, 
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        
        // Use a tiny 0.5px inset to make sure anti-aliasing edges are extremely smooth
        val rectF = RectF(0.5f, 0.5f, src.width.toFloat() - 0.5f, src.height.toFloat() - 0.5f)
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return output
    }
}
