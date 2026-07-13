package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateWidgetThemeWidget {
    companion object {
        val INSTANCE = MigrateWidgetThemeWidget()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val categories = listOf("aesthetic", "cute", "hot", "anime", "simple")
        for (i in 1..161) {
            val cat = categories[i % categories.size]
            val free = if (i <= 5) 1 else 0
            val sql = "INSERT OR IGNORE INTO widget_theme_widgets (id, name, folder, category, isFree, isFavorite) VALUES ('widget_theme_$i', 'Theme $i', 'theme_$i', '$cat', $free, 0)"
            db.execSQL(sql)
        }
    }
}
