package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.BackgroundItem

@Dao
interface BackgroundItemDao {
    @Query("SELECT * FROM background_items")
    fun getAllBackgrounds(): List<BackgroundItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackgrounds(backgrounds: List<BackgroundItem>)
}
