package com.theme.customizer.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.app.personalization.data.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetPinnedSuccessEvent

/**
 * Nhận thông báo từ hệ điều hành Android khi một AppWidget được ghim thành công (AppWidget Pinning).
 */
class WidgetPinningReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
        )
        
        if (appWidgetId != android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Hiển thị thông báo toast thành công nhanh chóng
            Toast.makeText(context, "Đã ghim Widget ra màn hình chủ thành công!", Toast.LENGTH_SHORT).show()

            // Gửi sự kiện qua EventBus của ứng dụng
            EventBus.getDefault().post(WidgetPinnedSuccessEvent())
        }
    }
}
