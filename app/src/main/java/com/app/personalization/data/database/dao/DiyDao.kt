package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.DiyBackground
import com.app.personalization.data.database.entity.DiySticker

/**
 * Lớp truy vấn (DAO) cho các thao tác với tài nguyên thiết kế DIY (Backgrounds & Stickers)
 */
@Dao
interface DiyDao {

    @Query("SELECT * FROM diy_backgrounds WHERE categoryName = :categoryName")
    fun getBackgroundsByCategory(categoryName: String): List<DiyBackground>

    @Query("SELECT * FROM diy_backgrounds")
    fun getAllBackgrounds(): List<DiyBackground>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackgrounds(backgrounds: List<DiyBackground>)

    @Query("SELECT * FROM diy_stickers WHERE categoryName = :categoryName")
    fun getStickersByCategory(categoryName: String): List<DiySticker>

    @Query("SELECT * FROM diy_stickers")
    fun getAllStickers(): List<DiySticker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickers(stickers: List<DiySticker>)
}
