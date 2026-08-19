package com.app.personalization.feature_wallpaper.data.dao

import androidx.room.*
import com.app.personalization.feature_wallpaper.data.entity.BackgroundItem

@Dao
interface BackgroundItemDao {
    @Query("SELECT * FROM background_items")
    fun getAllBackgrounds(): List<BackgroundItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackgrounds(backgrounds: List<BackgroundItem>)
}
