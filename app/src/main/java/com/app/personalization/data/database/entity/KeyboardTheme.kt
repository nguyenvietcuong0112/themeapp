package com.app.personalization.data.database.entity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.personalization.data.ThemeConfig
import com.app.personalization.data.ResourceConfig
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "keyboard_themes")
data class KeyboardTheme(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val name: String,
    val path: String = "",
    val rawType: String = "diy", // "default", "custom", "diy"
    var themeConfig: ThemeConfig? = null,
    val backgroundPath: String? = null,
    val popupKeyBackgroundPath: String? = null,
    val previewPath: String? = null,
    val isPremium: Boolean = false
) : java.io.Serializable {

    fun getPrefix(): String {
        return "theme_decorates/$path"
    }

    fun getBackground(): String? {
        if (rawType == "diy" || path.isEmpty()) {
            return null
        }
        if (backgroundPath != null && backgroundPath.startsWith("custom_image_")) {
            return null
        }
        return ResourceConfig.getKeyboardBackgroundUrl(path)
    }

    private fun loadJsonFromAsset(context: Context, path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun getConfig(context: Context): ThemeConfig? {
        val currentConfig = themeConfig
        if (currentConfig != null) {
            return currentConfig
        }
        if (rawType == "default" && path.isNotEmpty()) {
            return try {
                val jsonStr = loadJsonFromAsset(context, "${getPrefix()}/config.json")
                val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(jsonStr)
                themeConfig = decoded
                decoded
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun getKeyBackground(context: Context, keyCode: Int): Drawable? {
        if (rawType == "diy") {
            val config = getConfig(context) ?: themeConfig
            return config?.getKeyShapeDrawable()
        }
        val specialKeyName = when (keyCode) {
            10, 66 -> "return"
            12289 -> "emoji"
            -5 -> "backspace"
            -1 -> "shift"
            -2 -> "modeChange"
            32 -> "space"
            else -> "key"
        }
        return icon(context, specialKeyName)
    }

    private fun icon(context: Context, name: String): Drawable? {
        val assetPath = "${getPrefix()}/key/$name.png"
        return try {
            context.assets.open(assetPath).use {
                Drawable.createFromStream(it, null)
            }
        } catch (e: Exception) {
            // Fallback to general key if special key isn't present
            if (name != "key") {
                icon(context, "key")
            } else {
                null
            }
        }
    }

    fun keyFont(context: Context): Typeface {
        val config = getConfig(context)
        if (config != null) {
            val tf = config.font.getTypeface(context)
            if (tf != null) return tf
        }
        return Typeface.DEFAULT
    }

    fun keyTextColor(context: Context): Int {
        val config = getConfig(context) ?: return Color.BLACK
        return config.font.fontColor
    }

    fun tintColor(context: Context): Int {
        val config = getConfig(context) ?: return Color.WHITE
        return try {
            if (config.tintColor.isNotEmpty()) Color.parseColor(config.tintColor) else Color.WHITE
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    fun decorateKeyColor(context: Context): Int {
        val config = getConfig(context) ?: return Color.BLACK
        return try {
            if (config.decorateKeyColor.isNotEmpty()) Color.parseColor(config.decorateKeyColor) else keyTextColor(context)
        } catch (e: Exception) {
            keyTextColor(context)
        }
    }

    fun previewKeyFont(context: Context): Typeface {
        return keyFont(context)
    }

    fun previewTextColor(context: Context): Int {
        val config = getConfig(context) ?: return Color.BLACK
        return config.popup.textColor
    }

    fun generateLocalThemePreview(context: Context): Drawable {
        val width = 360
        val height = 240
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw solid background color
        val bgColor = tintColor(context)
        canvas.drawColor(bgColor)
        
        val assetManager = context.assets
        val prefix = getPrefix()
        
        // 2. Load key.png and space.png
        val keyBitmap = try {
            assetManager.open("$prefix/key/key.png").use { 
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
        
        val spaceBitmap = try {
            assetManager.open("$prefix/key/space.png").use { 
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // 3. Draw a mock key layout
        if (keyBitmap != null) {
            val keyW = 60
            val keyH = 50
            val startY = 40
            val gap = 15
            
            // Row 1: 3 keys
            val startX1 = (width - (3 * keyW + 2 * gap)) / 2
            for (i in 0 until 3) {
                val left = startX1 + i * (keyW + gap)
                val rect = Rect(left, startY, left + keyW, startY + keyH)
                canvas.drawBitmap(keyBitmap, null, rect, paint)
            }
            
            // Row 2: 2 keys
            val startX2 = (width - (2 * keyW + gap)) / 2
            val row2Y = startY + keyH + gap
            for (i in 0 until 2) {
                val left = startX2 + i * (keyW + gap)
                val rect = Rect(left, row2Y, left + keyW, row2Y + keyH)
                canvas.drawBitmap(keyBitmap, null, rect, paint)
            }
        }
        
        // 4. Draw a spacebar
        if (spaceBitmap != null) {
            val spaceW = 180
            val spaceH = 40
            val spaceX = (width - spaceW) / 2
            val spaceY = height - spaceH - 30
            val rect = Rect(spaceX, spaceY, spaceX + spaceW, spaceY + spaceH)
            canvas.drawBitmap(spaceBitmap, null, rect, paint)
        } else if (keyBitmap != null) {
            val spaceW = 180
            val spaceH = 40
            val spaceX = (width - spaceW) / 2
            val spaceY = height - spaceH - 30
            val rect = Rect(spaceX, spaceY, spaceX + spaceW, spaceY + spaceH)
            canvas.drawBitmap(keyBitmap, null, rect, paint)
        }
        
        return BitmapDrawable(context.resources, bitmap)
    }
}
