package com.theme.customizer.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import com.app.personalization.R

/**
 * Hỗ trợ vẽ Custom Font trực tiếp lên RemoteViews bằng cơ chế Canvas/Bitmap đệm.
 * Giúp vượt qua giới hạn của Android OS khi không cho phép nạp Custom Typeface trực tiếp vào RemoteViews.
 */
object WidgetFontDrawer {

    /**
     * Dựng chữ bằng font tùy biến lên một Bitmap và gán vào ImageView tương ứng trên RemoteViews.
     */
    fun drawCustomFontText(
        context: Context,
        remoteViews: RemoteViews,
        imageViewId: Int, // Ví dụ: R.id.ivWidget hoặc R.id.img_text
        textString: String,
        fontPathInAssets: String,
        textSizeSp: Float,
        textColorHex: String
    ) {
        try {
            // Tải Typeface từ Assets của ứng dụng
            val customTypeface = Typeface.createFromAsset(context.assets, fontPathInAssets)
            
            // Cấu hình Paint để đo kích thước vẽ
            val density = context.resources.displayMetrics.density
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = customTypeface
                color = Color.parseColor(textColorHex)
                textSize = textSizeSp * density
            }

            // Đo kích thước chữ cần vẽ
            val textWidth = paint.measureText(textString).coerceAtLeast(10f)
            val fontMetrics = paint.fontMetrics
            val textHeight = (fontMetrics.bottom - fontMetrics.top).coerceAtLeast(10f)

            // Tạo Bitmap trống và Canvas đệm
            val bitmap = Bitmap.createBitmap(textWidth.toInt() + 10, textHeight.toInt() + 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Vẽ chữ lên Canvas đệm
            val x = 5f
            val y = -fontMetrics.top + 5f
            canvas.drawText(textString, x, y, paint)

            // Gán bitmap đã vẽ font chữ vào RemoteViews thông qua ImageView
            remoteViews.setImageViewBitmap(imageViewId, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
