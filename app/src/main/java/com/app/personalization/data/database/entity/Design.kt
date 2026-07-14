package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.Json
import java.io.Serializable
import java.util.UUID

/**
 * Thực thể Bản thiết kế gốc (Design) lưu trữ các thông tin chung của bản nháp DIY Wallpaper.
 */
@Entity(tableName = "diy_designs")
data class Design(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val snapshot: String, // Tên file preview PNG cục bộ đã xuất
    val isLiveWallpaper: Boolean,
    val pagesJson: String, // Lưu trữ danh sách DesignPage dưới dạng chuỗi JSON
    val templateId: UUID? = null
) : Serializable {

    /**
     * Helper tiện ích để lấy nhanh danh sách DesignPage từ pagesJson
     */
    fun getPages(): ArrayList<DesignPage> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val list = json.decodeFromString<List<DesignPage>>(pagesJson)
            ArrayList(list)
        } catch (e: Exception) {
            arrayListOf()
        }
    }

    companion object {
        /**
         * Helper để tạo đối tượng Design mới kèm danh sách trang được serialize
         */
        fun create(
            id: UUID = UUID.randomUUID(),
            name: String,
            snapshot: String,
            isLiveWallpaper: Boolean,
            pages: ArrayList<DesignPage>,
            templateId: UUID? = null
        ): Design {
            val pagesJsonStr = try {
                Json.encodeToString(kotlinx.serialization.serializer(), pages)
            } catch (e: Exception) {
                "[]"
            }
            return Design(id, name, snapshot, isLiveWallpaper, pagesJsonStr, templateId)
        }
    }
}
