package com.theme.customizer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import com.app.personalization.data.EventBus
import com.app.personalization.presentation.widget.Widget2x2Provider
import com.app.personalization.presentation.widget.Widget4x2Provider
import com.app.personalization.presentation.widget.Widget4x4Provider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Màn hình quản lý và áp dụng Widget hệ thống (WidgetManagerActivity)
 * Thiết lập Programmatic UI và tích hợp chức năng AppWidgetManager.requestPinAppWidget.
 */
class WidgetManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bố cục tổng thể (vertical LinearLayout)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#12121A"))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 64, 32, 32)
        }

        val tvTitle = TextView(this).apply {
            text = "Danh Sách Mẫu Widget"
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        root.addView(tvTitle)

        // Thêm các nút ghim mẫu Widget
        addWidgetPinRow(root, "Widget Đồng Hồ Tròn (Small 2x2)", Widget2x2Provider::class.java)
        addWidgetPinRow(root, "Widget Thời Tiết & Lịch (Medium 4x2)", Widget4x2Provider::class.java)
        addWidgetPinRow(root, "Widget Lịch Tháng Chi Tiết (Large 4x4)", Widget4x4Provider::class.java)

        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @com.app.personalization.data.Subscribe
    fun onWidgetPinned(event: WidgetPinnedSuccessEvent) {
        Toast.makeText(this, "Nhận phản hồi: Ghim thành công!", Toast.LENGTH_LONG).show()
    }

    private fun addWidgetPinRow(container: LinearLayout, label: String, providerClass: Class<*>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E2E"))
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
        }

        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        val btnPin = Button(this).apply {
            text = "Ghim lên màn hình chính"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setOnClickListener {
                pinWidgetToHomeScreen(providerClass)
            }
        }

        row.addView(tvLabel)
        row.addView(btnPin)
        container.addView(row)
    }

    private fun pinWidgetToHomeScreen(providerClass: Class<*>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val myProvider = ComponentName(this, providerClass)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                // Tạo intent trỏ tới WidgetPinningReceiver
                val pinnedWidgetCallbackIntent = Intent(this, WidgetPinningReceiver::class.java)
                
                val successCallback = PendingIntent.getBroadcast(
                    this,
                    0,
                    pinnedWidgetCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                Toast.makeText(this, "Hãy đồng ý yêu cầu ghim trên hộp thoại hệ thống", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Thiết bị của bạn không hỗ trợ ghim Widget tự động!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tính năng ghim Widget yêu cầu Android 8.0 trở lên!", Toast.LENGTH_SHORT).show()
        }
    }
}
