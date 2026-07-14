package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.ThemeWidget
import java.util.UUID

/**
 * Lớp truy vấn (DAO) cho các thao tác với thực thể ThemeWidget
 */
@Dao
interface WidgetDao {

    @Query("SELECT * FROM theme_widgets")
    fun getAllWidgets(): List<ThemeWidget>

    @Query("SELECT * FROM theme_widgets WHERE themeId = :themeId")
    fun getWidgetsByTheme(themeId: UUID): List<ThemeWidget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidget(widget: ThemeWidget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidgets(widgets: List<ThemeWidget>)

    @Delete
    fun deleteWidget(widget: ThemeWidget)
}
