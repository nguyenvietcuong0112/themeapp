package com.app.personalization.feature_wallpaper.data.dao

import androidx.room.*
import com.app.personalization.feature_wallpaper.data.entity.StickerItem

@Dao
interface StickerItemDao {
    @Query("SELECT * FROM sticker_items")
    fun getAllStickers(): List<StickerItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickers(stickers: List<StickerItem>)
}
