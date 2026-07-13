package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateWidgetItemWeather {
    companion object {
        val INSTANCE = MigrateWidgetItemWeather()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        for (i in 1..161) {
            val free = if (i <= 5) 1 else 0
            // Seed Clock 2x2
            val sqlClock = "INSERT OR IGNORE INTO widget_items (id, themeFolder, name, widgetType, size, isFree, isFavorite) " +
                           "VALUES ('item_clock_$i', 'theme_$i', 'Clock 2x2', 'clock', 'small', $free, 0)"
            db.execSQL(sqlClock)

            // Seed Weather 4x2
            val sqlWeather = "INSERT OR IGNORE INTO widget_items (id, themeFolder, name, widgetType, size, isFree, isFavorite) " +
                             "VALUES ('item_weather_$i', 'theme_$i', 'Weather 4x2', 'weather', 'medium', $free, 0)"
            db.execSQL(sqlWeather)

            // Seed Calendar 4x4
            val sqlCalendar = "INSERT OR IGNORE INTO widget_items (id, themeFolder, name, widgetType, size, isFree, isFavorite) " +
                              "VALUES ('item_calendar_$i', 'theme_$i', 'Calendar 4x4', 'calendar', 'large', $free, 0)"
            db.execSQL(sqlCalendar)
        }
    }
}
