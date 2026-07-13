package com.app.personalization.presentation.widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.personalization.data.EventBus
import com.app.personalization.presentation.widget.event.WidgetEvent

class WidgetReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_WIDGET_PINNED = "com.app.personalization.WIDGET_PINNED"

        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetReceiver::class.java).apply {
                action = ACTION_WIDGET_PINNED
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, 200, intent, flags)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_WIDGET_PINNED) {
            EventBus.getDefault().post(WidgetEvent())
        }
    }
}
