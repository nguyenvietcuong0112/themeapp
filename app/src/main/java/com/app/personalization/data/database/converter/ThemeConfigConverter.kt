package com.app.personalization.data.database.converter

import androidx.room.TypeConverter
import com.app.personalization.data.ThemeConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ThemeConfigConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromThemeConfig(config: ThemeConfig?): String? {
        if (config == null) return null
        return try {
            json.encodeToString(config)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun toThemeConfig(jsonStr: String?): ThemeConfig? {
        if (jsonStr.isNullOrEmpty()) return null
        return try {
            json.decodeFromString<ThemeConfig>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }
}
