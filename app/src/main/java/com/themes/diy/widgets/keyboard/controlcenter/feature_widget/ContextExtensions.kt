package com.themes.diy.widgets.keyboard.controlcenter.feature_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetItem
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetSize
import kotlin.reflect.KClass

fun <V : AppWidgetProvider> Context.addWidget(
    providerClass: KClass<V>, 
    widgetItem: WidgetItem, 
    isMineOrCustom: Boolean = false
) {
    val appWidgetManager = getSystemService(AppWidgetManager::class.java) ?: return
    val componentName = ComponentName(this, providerClass.java)
    val bundle = Bundle()
    
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        val widgetSize = when (widgetItem.size.lowercase()) {
            "2x2", "small" -> WidgetSize.SMALL
            "4x2", "medium" -> WidgetSize.MEDIUM
            "4x4", "large" -> WidgetSize.LARGE
            else -> WidgetSize.SMALL
        }
        val layoutRes = when (widgetSize) {
            WidgetSize.SMALL -> R.layout.widget_layout_2x2
            WidgetSize.MEDIUM -> R.layout.widget_layout_4x2
            WidgetSize.LARGE -> R.layout.widget_layout_4x4
        }
        
        try {
            val previewBitmap = WidgetRenderHelper.getSnapshotImage(
                context = this,
                layoutId = layoutRes,
                widgetSize = widgetSize,
                widgetItem = widgetItem
            )
            if (previewBitmap != null) {
                val remoteViews = RemoteViews(packageName, R.layout.widget_container)
                remoteViews.setImageViewBitmap(R.id.ivWidget, previewBitmap)
                bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(this, WidgetReceiver::class.java).apply {
            putExtra("theme_id", widgetItem.id)
            putExtra("theme_folder", widgetItem.themeFolder)
            putExtra("widget_type", widgetItem.widgetType)
            putExtra("size", widgetItem.size)
            WidgetReceiver.widgetItem = widgetItem
            WidgetReceiver.isMineOrCustom = isMineOrCustom
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        
        appWidgetManager.requestPinAppWidget(componentName, bundle, pendingIntent)
    }
}
