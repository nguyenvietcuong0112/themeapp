package com.app.personalization.data.database.entity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

import com.app.personalization.data.ResourceConfig

@Entity(tableName = "widget_theme_wallpapers")
data class WidgetThemeWallpaper(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val themeId: String,
    val name: String,
    val order: Int,
    val folder: String,
    val imageBg: String,
    val isFree: Boolean = true,
    var isFavorite: Boolean = false,
    val isNew: Boolean = false,
    val category: String = "" // Custom field for easy category horizontal filter
) : java.io.Serializable {

    fun getFile(context: Context): File {
        val config = context.resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600
        val suffix = if (isTablet) "_tablet" else ""
        val fileName = "${imageBg}${suffix}"
        return File(context.filesDir, "images/$folder/wallpapers/$fileName.png")
    }

    fun getPreviewUri(): Uri {
        return Uri.parse("file:///android_asset/theme_decorates/$folder/$imageBg/popup_background.png")
    }

    fun getImageUri(): Uri {
        return Uri.parse("file:///android_asset/theme_decorates/$folder/$imageBg/popup_background.png")
    }

    fun getOnlinePreviewUri(context: Context): Uri {
        val themePath = if (folder.startsWith("theme_") || folder.contains("/")) folder else "$folder/$imageBg"
        val themeFolder = ResourceConfig.getThemeFolderByPath(context, themePath)
        return Uri.parse(ResourceConfig.getWallpaperThumbnailUrl(themeFolder, "bg_wallpaper"))
    }

    fun getOnlineImageUri(context: Context): Uri {
        val themePath = if (folder.startsWith("theme_") || folder.contains("/")) folder else "$folder/$imageBg"
        val themeFolder = ResourceConfig.getThemeFolderByPath(context, themePath)
        return Uri.parse(ResourceConfig.getWallpaperFullUrl(themeFolder, "bg_wallpaper"))
    }

    private fun getLocalTintColor(context: Context): Int {
        val assetManager = context.assets
        val prefix = "theme_decorates/$folder/$imageBg"
        return try {
            assetManager.open("$prefix/config.json").use { input ->
                val text = input.bufferedReader().use { it.readText() }
                val match = Regex("\"tintColor\"\\s*:\\s*\"([^\"]+)\"").find(text)
                val colorStr = match?.groupValues?.get(1) ?: "#12121A"
                Color.parseColor(colorStr)
            }
        } catch (e: Exception) {
            Color.parseColor("#12121A")
        }
    }

    fun generateLocalThemePreview(context: Context): Drawable {
        val width = 240
        val height = 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw solid background
        val bgColor = getLocalTintColor(context)
        canvas.drawColor(bgColor)
        
        // 2. Load decorative asset
        val assetManager = context.assets
        val prefix = "theme_decorates/$folder/$imageBg"
        val decorBitmap = try {
            assetManager.open("$prefix/popup_background.png").use { 
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            try {
                assetManager.open("$prefix/key/space.png").use { 
                    BitmapFactory.decodeStream(it)
                }
            } catch (e2: Exception) {
                null
            }
        }
        
        if (decorBitmap != null) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val destW = width * 2 / 3
            val destH = (destW * (decorBitmap.height.toFloat() / decorBitmap.width)).toInt()
            val left = (width - destW) / 2
            val top = (height - destH) / 2
            val rect = Rect(left, top, left + destW, top + destH)
            canvas.drawBitmap(decorBitmap, null, rect, paint)
        }
        
        return BitmapDrawable(context.resources, bitmap)
    }

    suspend fun getImageBg(context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val metrics = context.resources.displayMetrics
                val width = metrics.widthPixels.coerceAtLeast(1080)
                val height = metrics.heightPixels.coerceAtLeast(1920)
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                
                // 1. Draw solid background color
                val bgColor = getLocalTintColor(context)
                canvas.drawColor(bgColor)
                
                // 2. Load decorative asset
                val assetManager = context.assets
                val prefix = "theme_decorates/$folder/$imageBg"
                val decorBitmap = try {
                    assetManager.open("$prefix/popup_background.png").use { 
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    try {
                        assetManager.open("$prefix/key/space.png").use { 
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (e2: Exception) {
                        null
                    }
                }
                
                if (decorBitmap != null) {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    val destW = width / 2
                    val destH = (destW * (decorBitmap.height.toFloat() / decorBitmap.width)).toInt()
                    val left = (width - destW) / 2
                    val top = (height - destH) / 2
                    val rect = Rect(left, top, left + destW, top + destH)
                    canvas.drawBitmap(decorBitmap, null, rect, paint)
                }
                
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
