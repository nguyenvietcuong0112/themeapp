package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể Nhãn dán trang trí dùng trong công cụ thiết kế DIY
 */
@Entity(tableName = "diy_stickers")
data class DiySticker(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val categoryName: String, // Ví dụ: "Wedding", "Birthday", "Flowers"
    val imageName: String, // Tên file biểu trưng (file SVG hoặc PNG)
    val folderName: String
) : java.io.Serializable
