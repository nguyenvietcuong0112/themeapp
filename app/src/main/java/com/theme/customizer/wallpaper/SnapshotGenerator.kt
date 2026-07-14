package com.theme.customizer.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thuật toán xuất ảnh chất lượng cao (SnapshotGenerator)
 * Khởi tạo một View vẽ ẩn ngoài luồng (off-screen layout) để render độ phân giải thực của thiết bị.
 */
object SnapshotGenerator {

    /**
     * Chuyển đổi một View thành đối tượng Bitmap chất lượng cao bằng Canvas gốc.
     */
    suspend fun generate(view: View, targetWidth: Int, targetHeight: Int): Bitmap {
        return withContext(Dispatchers.Default) {
            // Thiết lập kích thước đo đạc thực tế cho View để vẽ chính xác
            view.measure(
                View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(targetHeight, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, targetWidth, targetHeight)

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            withContext(Dispatchers.Main) {
                view.draw(canvas)
            }
            bitmap
        }
    }
}
