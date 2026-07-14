package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể đại diện cho Bộ Theme tổng hợp (WidgetTheme)
 */
@Entity(tableName = "widget_themes")
data class WidgetTheme(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val folder: String, // Tên thư mục chứa theme trên CDN (ví dụ: "theme_1")
    val order: Int,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false // Đánh dấu bộ theme tự phối (DIY Custom theme)
) : java.io.Serializable
