package com.app.personalization.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.personalization.data.database.entity.WidgetConfig

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs WHERE widgetId = :widgetId LIMIT 1")
    fun getConfigForWidget(widgetId: Int): WidgetConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveConfig(config: WidgetConfig)

    @Query("DELETE FROM widget_configs WHERE widgetId = :widgetId")
    fun deleteConfig(widgetId: Int)
}
