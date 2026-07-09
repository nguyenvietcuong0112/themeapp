package com.app.personalization.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.personalization.data.database.entity.KeyboardTheme

@Dao
interface KeyboardThemeDao {
    @Query("SELECT * FROM keyboard_themes")
    fun getAllThemes(): List<KeyboardTheme>

    @Query("SELECT * FROM keyboard_themes WHERE id = :id LIMIT 1")
    fun getThemeById(id: String): KeyboardTheme?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTheme(theme: KeyboardTheme): Long

    @Delete
    fun deleteTheme(theme: KeyboardTheme)

    @Query("DELETE FROM keyboard_themes WHERE id = :id")
    fun deleteThemeById(id: String)
}
