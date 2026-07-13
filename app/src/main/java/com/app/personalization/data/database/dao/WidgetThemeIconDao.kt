package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.WidgetThemeIcon

@Dao
interface WidgetThemeIconDao {
    @Query("SELECT * FROM widget_theme_icons")
    fun getAllIcons(): List<WidgetThemeIcon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIcons(icons: List<WidgetThemeIcon>)

    @Update
    fun updateIcon(icon: WidgetThemeIcon)
}
