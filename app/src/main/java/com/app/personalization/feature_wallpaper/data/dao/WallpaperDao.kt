package com.app.personalization.feature_wallpaper.data.dao

import androidx.room.*
import com.app.personalization.feature_wallpaper.data.entity.ThemeWallpaper
import java.util.UUID

/**
 * Lớp truy vấn (DAO) cho các thao tác với thực thể ThemeWallpaper
 */
@Dao
interface WallpaperDao {

    @Query("SELECT * FROM theme_wallpapers")
    fun getAllWallpapers(): List<ThemeWallpaper>

    @Query("SELECT * FROM theme_wallpapers WHERE themeId = :themeId")
    fun getWallpapersByTheme(themeId: UUID): List<ThemeWallpaper>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallpaper(wallpaper: ThemeWallpaper)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallpapers(wallpapers: List<ThemeWallpaper>)

    @Delete
    fun deleteWallpaper(wallpaper: ThemeWallpaper)
}
