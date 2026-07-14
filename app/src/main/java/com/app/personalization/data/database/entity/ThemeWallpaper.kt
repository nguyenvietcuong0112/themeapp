package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể Hình nền đi kèm trong mỗi bộ Theme
 */
@Entity(
    tableName = "theme_wallpapers",
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
data class ThemeWallpaper(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val themeId: UUID, // Khóa ngoại liên kết với WidgetTheme
    val folder: String,
    val imageName: String,
    val isFavorite: Boolean = false
) : java.io.Serializable
