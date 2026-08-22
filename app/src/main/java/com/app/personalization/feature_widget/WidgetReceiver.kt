package com.app.personalization.feature_widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.app.personalization.core.data.EventBus
import com.app.personalization.feature_widget.data.entity.WidgetConfig
import com.app.personalization.feature_widget.data.entity.WidgetItem
import com.app.personalization.core.di.ServiceLocator
import com.app.personalization.feature_widget.event.WidgetAddSucceedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetReceiver : BroadcastReceiver() {

    companion object {
        var widgetItem: WidgetItem? = null
        var isMineOrCustom: Boolean = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras ?: return
        val appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        
        if (appWidgetId != 0) {
            context?.let { ctx ->
                Toast.makeText(ctx, "Widget added", Toast.LENGTH_SHORT).show()

                val item = widgetItem
                val themeId = extras.getString("theme_id") ?: item?.id ?: "1"
                val themeFolder = extras.getString("theme_folder") ?: item?.themeFolder ?: "theme_1"
                val widgetType = extras.getString("widget_type") ?: item?.widgetType ?: "weather"
                val size = extras.getString("size") ?: item?.size ?: "4x2"
                
                val cleanId = themeId.replace('/', '_').replace('\\', '_')
                val fileName = "widget_bg_${cleanId}_${widgetType}_$size.png"
                val file = ctx.getFileStreamPath(fileName)

                val imageUriStr = if (file.exists()) Uri.fromFile(file).toString() else null

                val config = WidgetConfig(
                    widgetId = appWidgetId,
                    bgType = if (imageUriStr != null) "IMAGE" else "COLOR",
                    solidColor = 0xFF1E1E2E.toInt(),
                    imageUri = imageUriStr,
                    textColor = android.graphics.Color.WHITE,
                    fontStyle = "normal",
                    gradientStartColor = 0,
                    gradientEndColor = 0
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        ctx.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("widget_type_$appWidgetId", widgetType)
                            .putString("theme_folder_$appWidgetId", themeFolder)
                            .putString("theme_id_$appWidgetId", themeId)
                            .putString("widget_size_$appWidgetId", size)
                            .apply()
                        ServiceLocator.getWidgetConfigDao(ctx).saveConfig(config)
                        val appWidgetManager = AppWidgetManager.getInstance(ctx)
                        Widget2x2Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                        Widget4x2Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                        Widget4x4Provider().updateWidget(ctx, appWidgetManager, appWidgetId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            EventBus.getDefault().post(WidgetAddSucceedEvent(appWidgetId))
        }
    }
}
