package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.WidgetTheme
import java.util.UUID

/**
 * Lớp truy vấn (DAO) cho các thao tác với thực thể WidgetTheme
 */
@Dao
interface ThemeDao {

    @Query("SELECT * FROM widget_themes ORDER BY `order` ASC")
    fun getAllThemes(): List<WidgetTheme>

    @Query("SELECT * FROM widget_themes WHERE id = :id LIMIT 1")
    fun getThemeById(id: UUID): WidgetTheme?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTheme(theme: WidgetTheme)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThemes(themes: List<WidgetTheme>)

    @Update
    fun updateTheme(theme: WidgetTheme)

    @Delete
    fun deleteTheme(theme: WidgetTheme)
}
