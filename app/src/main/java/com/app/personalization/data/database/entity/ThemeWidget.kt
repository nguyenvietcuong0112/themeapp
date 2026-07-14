package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể biểu diễn Tiện ích (Widget) đi kèm trong bộ Theme
 */
@Entity(
    tableName = "theme_widgets",
    foreignKeys = [
        ForeignKey(
            entity = WidgetTheme::class,
            parentColumns = ["id"],
            childColumns = ["themeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["themeId"])]
)
data class ThemeWidget(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val themeId: UUID, // Khóa ngoại liên kết với WidgetTheme
    val templatePath: String, // Đường dẫn thư mục chứa template trên CDN (ví dụ: "templates/template1")
    val size: String, // Kích thước: SMALL / MEDIUM / LARGE
    val type: String // Loại widget: CLOCK / CALENDAR / WEATHER / IMAGE
) : java.io.Serializable
