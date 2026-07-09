package com.app.personalization.data.database.entity

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.personalization.data.ThemeConfig
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID

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
) {

    fun getPrefix(): String {
        return "theme_decorates/$path"
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
}
