package com.themes.diy.widgets.keyboard.controlcenter.core.di

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.core.data.database.AppDatabase
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.dao.KeyboardThemeDao
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.dao.WidgetConfigDao

import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.dao.WidgetThemeWallpaperDao

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
 
    fun getIconPackDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.dao.WidgetThemeIconDao {
        return getDatabase(context).iconPackDao()
    }
 
    fun getChargingDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_charging.data.dao.ChargingAnimationDao {
        return getDatabase(context).chargingDao()
    }
 
    fun getWidgetThemeDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.dao.WidgetThemeWidgetDao {
        return getDatabase(context).widgetThemeDao()
    }
 
    fun getWidgetItemDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.dao.WidgetItemDao {
        return getDatabase(context).widgetItemDao()
    }
 
    fun getStickerDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.dao.StickerItemDao {
        return getDatabase(context).stickerDao()
    }
 
    fun getBackgroundDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.dao.BackgroundItemDao {
        return getDatabase(context).backgroundDao()
    }
 
    fun getTemplateDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.dao.TemplateDao {
        return getDatabase(context).templateDao()
    }

    fun getDownloadedCollectionDao(context: Context): com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.DownloadedCollectionDao {
        return getDatabase(context).downloadedCollectionDao()
    }

    @Volatile
    private var themeRepository: com.themes.diy.widgets.keyboard.controlcenter.core.data.repository.ThemeRepository? = null

    fun getThemeRepository(context: Context): com.themes.diy.widgets.keyboard.controlcenter.core.data.repository.ThemeRepository {
        return themeRepository ?: synchronized(this) {
            val repo = com.themes.diy.widgets.keyboard.controlcenter.core.data.repository.ThemeRepositoryImpl(
                assetDataSource = com.themes.diy.widgets.keyboard.controlcenter.core.data.source.AssetThemeDataSource(context.applicationContext),
                apiDataSource = null, // Can be injected when remote API is ready
                themeDao = getThemeDao(context.applicationContext)
            )
            themeRepository = repo
            repo
        }
    }
}
