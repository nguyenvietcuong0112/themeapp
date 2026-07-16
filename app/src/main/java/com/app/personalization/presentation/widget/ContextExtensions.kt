package com.app.personalization.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.app.personalization.data.database.entity.WidgetItem
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
        val intent = Intent(this, WidgetReceiver::class.java).apply {
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
