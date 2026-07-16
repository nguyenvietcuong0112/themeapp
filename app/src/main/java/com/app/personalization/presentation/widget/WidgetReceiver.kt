package com.app.personalization.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.app.personalization.data.EventBus
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.data.database.entity.WidgetItem
import com.app.personalization.di.ServiceLocator
import com.app.personalization.presentation.widget.event.WidgetAddSucceedEvent
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
                if (item != null) {
                    val themeId = item.id
                    val widgetType = item.widgetType
                    val size = item.size
                    
                    val fileName = "widget_bg_${themeId}_${widgetType}_$size.png"
                    val file = ctx.getFileStreamPath(fileName)

                    if (file.exists()) {
                        val config = WidgetConfig(
                            widgetId = appWidgetId,
                            bgType = "IMAGE",
                            solidColor = 0,
                            imageUri = Uri.fromFile(file).toString(),
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
                                    .putString("theme_folder_$appWidgetId", item.themeFolder)
                                    .putString("theme_id_$appWidgetId", themeId)
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
                }
            }

            EventBus.getDefault().post(WidgetAddSucceedEvent(appWidgetId))
        }
    }
}
