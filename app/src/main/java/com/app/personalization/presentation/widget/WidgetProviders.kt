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
import android.view.View
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.data.database.entity.WidgetItem
import com.app.personalization.data.database.entity.WidgetSize
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
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val widgetType = prefs.getString("widget_type_$widgetId", "clock") ?: "clock"
        val themeFolder = prefs.getString("theme_folder_$widgetId", "theme_1") ?: "theme_1"
        val themeId = prefs.getString("theme_id_$widgetId", "1") ?: "1"

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "2x2",
            isFree = true,
            isFavorite = false
        )

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_2x2,
            widgetSize = WidgetSize.SMALL,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidget, bitmap)
        }
        appWidgetManager.updateAppWidget(widgetId, views)
    }
}

class Widget4x2Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val widgetType = prefs.getString("widget_type_$widgetId", "weather") ?: "weather"
        val themeFolder = prefs.getString("theme_folder_$widgetId", "theme_1") ?: "theme_1"
        val themeId = prefs.getString("theme_id_$widgetId", "1") ?: "1"

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "4x2",
            isFree = true,
            isFavorite = false
        )

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_4x2,
            widgetSize = WidgetSize.MEDIUM,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidget, bitmap)
        }
        appWidgetManager.updateAppWidget(widgetId, views)
    }
}

class Widget4x4Provider : BaseWidgetProvider() {
    override fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val widgetType = prefs.getString("widget_type_$widgetId", "calendar") ?: "calendar"
        val themeFolder = prefs.getString("theme_folder_$widgetId", "theme_1") ?: "theme_1"
        val themeId = prefs.getString("theme_id_$widgetId", "1") ?: "1"

        val widgetItem = WidgetItem(
            id = themeId,
            themeFolder = themeFolder,
            name = "Widget",
            widgetType = widgetType,
            size = "4x4",
            isFree = true,
            isFavorite = false
        )

        val bitmap = WidgetRenderHelper.getSnapshotImage(
            context = context,
            layoutId = R.layout.widget_layout_4x4,
            widgetSize = WidgetSize.LARGE,
            widgetItem = widgetItem,
            widgetId = widgetId
        )

        val views = RemoteViews(context.packageName, R.layout.widget_container)
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidget, bitmap)
        }
        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
