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
 
    fun getIconPackDao(context: Context): com.app.personalization.data.database.dao.WidgetThemeIconDao {
        return getDatabase(context).iconPackDao()
    }
 
    fun getChargingDao(context: Context): com.app.personalization.data.database.dao.ChargingAnimationDao {
        return getDatabase(context).chargingDao()
    }
 
    fun getWidgetThemeDao(context: Context): com.app.personalization.data.database.dao.WidgetThemeWidgetDao {
        return getDatabase(context).widgetThemeDao()
    }
 
    fun getWidgetItemDao(context: Context): com.app.personalization.data.database.dao.WidgetItemDao {
        return getDatabase(context).widgetItemDao()
    }
 
    fun getStickerDao(context: Context): com.app.personalization.data.database.dao.StickerItemDao {
        return getDatabase(context).stickerDao()
    }
 
    fun getBackgroundDao(context: Context): com.app.personalization.data.database.dao.BackgroundItemDao {
        return getDatabase(context).backgroundDao()
    }
 
    fun getTemplateDao(context: Context): com.app.personalization.data.database.dao.TemplateDao {
        return getDatabase(context).templateDao()
    }
}
