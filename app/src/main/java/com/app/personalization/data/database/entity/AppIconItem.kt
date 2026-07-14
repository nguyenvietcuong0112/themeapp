package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể biểu thị thông tin Biểu tượng lẻ của từng App
 */
@Entity(
    tableName = "app_icon_items",
    foreignKeys = [
        ForeignKey(
            entity = ThemeIconPack::class,
            parentColumns = ["id"],
            childColumns = ["iconPackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["iconPackId"])]
)
data class AppIconItem(
    @PrimaryKey val id: String, // Ví dụ: "facebook", "tiktok", "chrome"
    val iconPackId: UUID, // Khóa ngoại liên kết với ThemeIconPack
    val appName: String, // Tên hiển thị mặc định
    val packageId: String, // Package name của app (ví dụ: "com.facebook.katana")
    val customDisplayName: String? = null, // Tên tùy biến do người dùng chỉnh sửa
    val customPackageId: String? = null // Package tùy biến do người dùng sửa đổi
) : java.io.Serializable
