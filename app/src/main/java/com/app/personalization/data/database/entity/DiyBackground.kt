package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Thực thể Ảnh nền dùng cho thiết kế DIY tự chọn
 */
@Entity(tableName = "diy_backgrounds")
data class DiyBackground(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val categoryName: String, // Ví dụ: "Aesthetic", "Cute", "Texture"
    val imageName: String,
    val folderName: String
) : java.io.Serializable
