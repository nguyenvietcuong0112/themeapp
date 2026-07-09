package com.app.personalization.presentation.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.database.entity.WidgetConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

open class BaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    open fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        // Subclasses implement this
    }

    protected fun applyWidgetConfig(context: Context, views: RemoteViews, config: WidgetConfig?, defaultBg: Int, widgetId: Int) {
        if (config == null) {
            views.setInt(R.id.ivBackground, "setBackgroundColor", defaultBg)
            return
        }

        when (config.bgType) {
            "COLOR" -> {
                views.setImageViewBitmap(R.id.ivBackground, null)
                views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
            }
            "GRADIENT" -> {
                try {
                    val width = 400
                    val height = 400
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    val gd = GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(config.gradientStartColor, config.gradientEndColor)
                    )
                    gd.setBounds(0, 0, width, height)
                    gd.cornerRadius = 24f
                    gd.draw(canvas)
                    views.setImageViewBitmap(R.id.ivBackground, bmp)
                } catch (e: Exception) {
                    views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                }
            }
            "IMAGE" -> {
                if (config.imageUri != null) {
                    thread {
                        try {
                            val bmp = Glide.with(context.applicationContext)
                                .asBitmap()
                                .load(Uri.parse(config.imageUri))
                                .submit()
                                .get()
                            views.setImageViewBitmap(R.id.ivBackground, bmp)
                            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
                        }
                    }
                } else {
                    views.setInt(R.id.ivBackground, "setBackgroundColor", config.solidColor)
                }
            }
        }
    }

    protected fun getTypeface(context: Context, fontStyle: String?): Typeface {
        if (fontStyle.isNullOrEmpty() || fontStyle == "normal") return Typeface.DEFAULT
        return try {
            Typeface.createFromAsset(context.assets, "fonts/$fontStyle")
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
    }
}

class Widget2x2Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_2x2)
        val config = ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)

        applyWidgetConfig(context, views, config, 0xFF1E1E2E.toInt(), widgetId)

        val size = 512
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val styleId = (widgetId % 29).let { if (it < 0) -it else it } + 1
        val dialResId = context.resources.getIdentifier("widget_clock_${styleId}_dial", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_dial else it }
        val hourResId = context.resources.getIdentifier("widget_clock_${styleId}_hours", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_hours else it }
        val minResId = context.resources.getIdentifier("widget_clock_${styleId}_minutes", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_minutes else it }
        val secResId = context.resources.getIdentifier("widget_clock_${styleId}_seconds", "drawable", context.packageName).let { if (it == 0) R.drawable.widget_clock_1_seconds else it }

        val dialBmp = BitmapFactory.decodeResource(context.resources, dialResId)
        val hourBmp = BitmapFactory.decodeResource(context.resources, hourResId)
        val minBmp = BitmapFactory.decodeResource(context.resources, minResId)
        val secBmp = BitmapFactory.decodeResource(context.resources, secResId)

        val destRect = android.graphics.Rect(0, 0, size, size)

        if (dialBmp != null) {
            canvas.drawBitmap(dialBmp, null, destRect, null)
        } else {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config?.textColor ?: Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 16f, paint)
        }

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        val hrAngle = (hour % 12) * 30f + minute * 0.5f
        val minAngle = minute * 6f + second * 0.1f
        val secAngle = second * 6f

        drawRotatedHand(canvas, hourBmp, hrAngle, size)
        drawRotatedHand(canvas, minBmp, minAngle, size)
        drawRotatedHand(canvas, secBmp, secAngle, size)

        views.setImageViewBitmap(R.id.ivClock, bmp)
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun drawRotatedHand(canvas: Canvas, handBmp: Bitmap?, angle: Float, size: Int) {
        if (handBmp == null) return
        val matrix = Matrix()
        val scale = size.toFloat() / handBmp.width.toFloat()
        matrix.postScale(scale, scale)
        matrix.postRotate(angle, size / 2f, size / 2f)
        canvas.drawBitmap(handBmp, matrix, null)
    }
}

class Widget4x2Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
        val config = ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)

        applyWidgetConfig(context, views, config, 0xFF1A1A24.toInt(), widgetId)
        scheduleWeatherWork(context)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val now = Date()

        views.setTextViewText(R.id.tvTime, timeFormat.format(now))
        views.setTextViewText(R.id.tvDate, dateFormat.format(now))

        val textColor = config?.textColor ?: Color.WHITE
        views.setTextColor(R.id.tvTime, textColor)
        views.setTextColor(R.id.tvDate, textColor)

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val weatherText = prefs.getString("weather_temp", "26°C Sunny") ?: "26°C Sunny"
        
        val iconCode = when {
            weatherText.contains("sunny", ignoreCase = true) -> "sunny"
            weatherText.contains("cloud", ignoreCase = true) -> "cloudy"
            weatherText.contains("rain", ignoreCase = true) -> "rainy"
            else -> "sunny"
        }
        val weatherIconId = context.resources.getIdentifier("ic_weather_${iconCode}", "drawable", context.packageName).let { if (it == 0) R.drawable.ic_style_weather else it }
        views.setImageViewResource(R.id.ivWeatherIcon, weatherIconId)
        views.setTextViewText(R.id.tvWeatherTemp, weatherText)
        views.setTextColor(R.id.tvWeatherTemp, textColor)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun scheduleWeatherWork(context: Context) {
        try {
            val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                "WeatherUpdateWork",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class Widget4x4Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_4x4)
        val config = ServiceLocator.getWidgetConfigDao(context).getConfigForWidget(widgetId)

        applyWidgetConfig(context, views, config, 0xFF12121A.toInt(), widgetId)

        val size = 512
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val textColor = config?.textColor ?: Color.WHITE
        val accentColor = 0xFF00E5FF.toInt()
        val font = getTypeface(context, config?.fontStyle)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = font
        }
        val cal = Calendar.getInstance()
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = cal.get(Calendar.YEAR)
        canvas.drawText("$monthName $year", size / 2f, 50f, headerPaint)

        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 180
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = font
        }
        val colWidth = size / 7f
        for (i in 0 until 7) {
            canvas.drawText(weekdays[i], colWidth * i + colWidth / 2f, 110f, weekdayPaint)
        }

        val today = cal.get(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = font
        }

        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        var row = 0
        for (day in 1..maxDay) {
            val col = (firstDayOfWeek + day - 1) % 7
            val x = colWidth * col + colWidth / 2f
            val y = 180f + row * 60f

            if (day == today) {
                canvas.drawCircle(x, y - 8f, 26f, highlightPaint)
                dayPaint.color = accentColor
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

        views.setImageViewBitmap(R.id.ivCalendar, bmp)
        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
