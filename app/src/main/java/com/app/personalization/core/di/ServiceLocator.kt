package com.app.personalization.core.di

import android.content.Context
import com.app.personalization.core.data.database.AppDatabase
import com.app.personalization.feature_keyboard.data.dao.KeyboardThemeDao
import com.app.personalization.feature_widget.data.dao.WidgetConfigDao

import com.app.personalization.feature_widget.data.dao.WidgetThemeWallpaperDao

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
 
    fun getIconPackDao(context: Context): com.app.personalization.feature_widget.data.dao.WidgetThemeIconDao {
        return getDatabase(context).iconPackDao()
    }
 
    fun getChargingDao(context: Context): com.app.personalization.feature_charging.data.dao.ChargingAnimationDao {
        return getDatabase(context).chargingDao()
    }
 
    fun getWidgetThemeDao(context: Context): com.app.personalization.feature_widget.data.dao.WidgetThemeWidgetDao {
        return getDatabase(context).widgetThemeDao()
    }
 
    fun getWidgetItemDao(context: Context): com.app.personalization.feature_widget.data.dao.WidgetItemDao {
        return getDatabase(context).widgetItemDao()
    }
 
    fun getStickerDao(context: Context): com.app.personalization.feature_wallpaper.data.dao.StickerItemDao {
        return getDatabase(context).stickerDao()
    }
 
    fun getBackgroundDao(context: Context): com.app.personalization.feature_wallpaper.data.dao.BackgroundItemDao {
        return getDatabase(context).backgroundDao()
    }
 
    fun getTemplateDao(context: Context): com.app.personalization.feature_wallpaper.data.dao.TemplateDao {
        return getDatabase(context).templateDao()
    }

    @Volatile
    private var themeRepository: com.app.personalization.core.data.repository.ThemeRepository? = null

    fun getThemeRepository(context: Context): com.app.personalization.core.data.repository.ThemeRepository {
        return themeRepository ?: synchronized(this) {
            val repo = com.app.personalization.core.data.repository.ThemeRepositoryImpl(
                assetDataSource = com.app.personalization.core.data.source.AssetThemeDataSource(context.applicationContext),
                apiDataSource = null, // Can be injected when remote API is ready
                themeDao = getThemeDao(context.applicationContext)
            )
            themeRepository = repo
            repo
        }
    }
}
