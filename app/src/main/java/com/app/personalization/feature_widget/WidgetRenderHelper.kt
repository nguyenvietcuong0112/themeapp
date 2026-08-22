package com.app.personalization.feature_widget

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
import com.app.personalization.feature_widget.data.entity.WidgetConfig
import com.app.personalization.feature_widget.data.entity.WidgetItem
import com.app.personalization.feature_widget.data.entity.WidgetSize
import com.app.personalization.core.di.ServiceLocator
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
                    val cleanId = widgetItem.id.replace('/', '_').replace('\\', '_')
                    val fileName = "widget_bg_${cleanId}_${widgetItem.widgetType}_$sizeStr.png"
                    val file = context.getFileStreamPath(fileName)
                    if (file.exists()) {
                        bgBmp = BitmapFactory.decodeFile(file.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (bgBmp == null) {
                try {
                    val folder = widgetItem.themeFolder
                        .removePrefix("file:///android_asset/")
                        .removePrefix("file://android_asset/")
                        .removePrefix("android_asset/")
                        .removePrefix("/")
                    val typeFolder = when (widgetItem.widgetType.lowercase()) {
                        "clock", "clocks" -> "clocks"
                        "calendar", "today", "date" -> "today"
                        "weather" -> "weather"
                        "image", "photo" -> "image"
                        else -> widgetItem.widgetType.lowercase()
                    }
                    val isMedium = widgetSize == WidgetSize.MEDIUM
                    val previewFileName = if (isMedium) "bg_preview_medium.png" else "bg_preview_large.png"
                    val bgFileName = if (isMedium) "bg_medium.png" else "bg_large.png"
                    
                    val candidatePaths = listOf(
                        "assets_theme/$folder/widgets/$typeFolder/$previewFileName",
                        "assets_theme/category/$folder/widgets/$typeFolder/$previewFileName",
                        "assets_collection/theme/$folder/widgets/$typeFolder/$previewFileName",
                        "assets_collection/$folder/widgets/$typeFolder/$previewFileName",
                        "$folder/widgets/$typeFolder/$previewFileName",
                        "assets_theme/$folder/widgets/$bgFileName",
                        "assets_theme/category/$folder/widgets/$bgFileName",
                        "assets_theme/$folder/widgets/bg_medium.png",
                        "assets_theme/$folder/widgets/bg_large.png"
                    )
                    for (cand in candidatePaths) {
                        try {
                            context.assets.open(cand).use { stream ->
                                bgBmp = BitmapFactory.decodeStream(stream)
                            }
                            if (bgBmp != null) break
                        } catch (e: Exception) {
                            // Try next
                        }
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
                ivClock?.visibility = View.GONE
            }
            WidgetSize.MEDIUM -> {
                val tvTime = view.findViewById<TextView>(R.id.tvTime)
                val tvDate = view.findViewById<TextView>(R.id.tvDate)
                val ivWeatherIcon = view.findViewById<ImageView>(R.id.ivWeatherIcon)
                val tvWeatherTemp = view.findViewById<TextView>(R.id.tvWeatherTemp)

                tvTime?.visibility = View.GONE
                tvDate?.visibility = View.GONE
                ivWeatherIcon?.visibility = View.GONE
                tvWeatherTemp?.visibility = View.GONE
            }
            WidgetSize.LARGE -> {
                val ivCalendar = view.findViewById<ImageView>(R.id.ivCalendar)
                ivCalendar?.visibility = View.GONE
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
