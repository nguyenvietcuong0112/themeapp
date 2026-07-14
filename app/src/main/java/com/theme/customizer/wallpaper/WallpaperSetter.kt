package com.theme.customizer.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hỗ trợ cài đặt hình nền hệ thống khóa thu phóng (OS Zoom Locking).
 * Cấu hình kích thước gợi ý chính xác theo pixel vật lý của màn hình để triệt tiêu hiệu ứng Parallax của Launcher.
 */
object WallpaperSetter {

    /**
     * Cài đặt Bitmap làm hình nền hệ thống/màn hình khóa và vô hiệu hóa hiện tượng tự động Zoom.
     */
    suspend fun setWallpaper(context: Context, bitmap: Bitmap, wallpaperType: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                val metrics = context.resources.displayMetrics
                val screenWidth = metrics.widthPixels
                val screenHeight = metrics.heightPixels

                // Khóa kích thước mong muốn trùng khớp tuyệt đối với độ phân giải vật lý của màn hình
                wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION.SDK_INT) {
                    wallpaperManager.setBitmap(bitmap, null, true, wallpaperType)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
