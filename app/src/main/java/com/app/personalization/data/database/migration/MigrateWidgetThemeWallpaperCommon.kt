package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateWidgetThemeWallpaperCommon {
    companion object {
        val INSTANCE = MigrateWidgetThemeWallpaperCommon()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val wallpapers = listOf(
            "('wp_aes_1', 'aesthetic', 'Blue Sky', 1, 'Aesthetic', 'blue-sky', 1, 0, 0, 'aesthetic')",
            "('wp_aes_2', 'aesthetic', 'Purple Galaxy', 2, 'Aesthetic', 'purple-galaxy', 1, 0, 0, 'aesthetic')",
            "('wp_cute_1', 'cute', 'Pink Rose', 3, 'Romantic', 'pink-rose', 1, 0, 0, 'cute')",
            "('wp_cute_2', 'cute', 'Love Letters', 4, 'Romantic', 'love and letters', 1, 0, 0, 'cute')",
            "('wp_hot_1', 'hot', 'Butterflies', 5, 'Trending', 'butterflies', 1, 0, 0, 'hot')",
            "('wp_hot_2', 'hot', 'Neon Galaxy', 6, 'Trending', 'galaxy', 1, 0, 0, 'hot')",
            "('wp_anime_1', 'anime', 'Go Green', 7, 'Simple', 'go-green', 1, 0, 0, 'anime')",
            "('wp_anime_2', 'anime', 'Bubble Soap', 8, 'Simple', 'bubble-soap', 1, 0, 0, 'anime')"
        )
        for (wp in wallpapers) {
            db.execSQL("INSERT OR IGNORE INTO widget_theme_wallpapers (id, themeId, name, `order`, folder, imageBg, isFree, isFavorite, isNew, category) VALUES $wp")
        }
        val categories = listOf("aesthetic", "cute", "hot", "anime", "simple")
        for (i in 1..161) {
            val cat = categories[i % categories.size]
            val free = if (i <= 5) 1 else 0
            val sql = "INSERT OR IGNORE INTO widget_theme_wallpapers (id, themeId, name, `order`, folder, imageBg, isFree, isFavorite, isNew, category) " +
                      "VALUES ('wp_theme_$i', 'theme_$i', 'Wallpaper $i', $i, 'theme_$i', 'bg_wallpaper', $free, 0, 0, '$cat')"
            db.execSQL(sql)
        }
    }
}
