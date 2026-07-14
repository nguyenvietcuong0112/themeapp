package com.app.personalization.data.database.converter

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Bộ chuyển đổi kiểu dữ liệu (Converters) hỗ trợ lưu trữ kiểu UUID cho database mới.
 */
class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidStr: String?): UUID? {
        return uuidStr?.let { UUID.fromString(it) }
    }
}
