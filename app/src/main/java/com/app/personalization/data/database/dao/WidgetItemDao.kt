package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.WidgetItem

@Dao
interface WidgetItemDao {
    @Query("SELECT * FROM widget_items")
    fun getAllWidgetItems(): List<WidgetItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidgetItems(items: List<WidgetItem>)
}
