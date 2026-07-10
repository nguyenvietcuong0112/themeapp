package com.app.personalization.data.database.dao

import androidx.room.*
import com.app.personalization.data.database.entity.WidgetThemeWallpaper

@Dao
interface WidgetThemeWallpaperDao {

    @Query("SELECT * FROM widget_theme_wallpapers ORDER BY `order` ASC")
    fun getAllWallpapers(): List<WidgetThemeWallpaper>

    @Query("SELECT * FROM widget_theme_wallpapers WHERE category = :category ORDER BY `order` ASC")
    fun getWallpapersByCategory(category: String): List<WidgetThemeWallpaper>

    @Query("SELECT * FROM widget_theme_wallpapers WHERE id = :id LIMIT 1")
    fun getWallpaperById(id: String): WidgetThemeWallpaper?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallpaper(wallpaper: WidgetThemeWallpaper)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallpapers(wallpapers: List<WidgetThemeWallpaper>)

    @Update
    fun updateWallpaper(wallpaper: WidgetThemeWallpaper)

    @Delete
    fun deleteWallpaper(wallpaper: WidgetThemeWallpaper)
}
