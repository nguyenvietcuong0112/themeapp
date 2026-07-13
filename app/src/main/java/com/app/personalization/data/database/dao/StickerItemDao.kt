package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.StickerItem

@Dao
interface StickerItemDao {
    @Query("SELECT * FROM sticker_items")
    fun getAllStickers(): List<StickerItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickers(stickers: List<StickerItem>)
}
