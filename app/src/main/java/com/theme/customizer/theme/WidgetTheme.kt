package com.theme.customizer.theme

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Thực thể lưu trữ cấu hình Bộ Theme tự phối (WidgetTheme)
 */
@Entity(tableName = "custom_themes")
data class WidgetTheme(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val folder: String, // Tên thư mục chứa theme (ví dụ: "theme_1")
    val order: Int,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = true
) : Serializable
