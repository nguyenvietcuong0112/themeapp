package com.app.personalization.di

import android.content.Context
import com.app.personalization.data.database.AppDatabase
import com.app.personalization.data.database.dao.KeyboardThemeDao
import com.app.personalization.data.database.dao.WidgetConfigDao

import com.app.personalization.data.database.dao.WidgetThemeWallpaperDao

object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            database = instance
            instance
        }
    }

    fun getThemeDao(context: Context): KeyboardThemeDao {
        return getDatabase(context).themeDao()
    }

    fun getWidgetConfigDao(context: Context): WidgetConfigDao {
        return getDatabase(context).widgetConfigDao()
    }

    fun getWallpaperDao(context: Context): WidgetThemeWallpaperDao {
        return getDatabase(context).wallpaperDao()
    }
}
