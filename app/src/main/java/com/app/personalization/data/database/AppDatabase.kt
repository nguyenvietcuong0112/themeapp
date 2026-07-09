package com.app.personalization.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.personalization.data.database.converter.ThemeConfigConverter
import com.app.personalization.data.database.dao.KeyboardThemeDao
import com.app.personalization.data.database.dao.WidgetConfigDao
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.database.entity.WidgetConfig

@Database(entities = [KeyboardTheme::class, WidgetConfig::class], version = 1, exportSchema = false)
@TypeConverters(ThemeConfigConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themeDao(): KeyboardThemeDao
    abstract fun widgetConfigDao(): WidgetConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personalization_db"
                )
                .fallbackToDestructiveMigration() // Prevent crashes due to schema changes
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
