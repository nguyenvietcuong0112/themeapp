package com.app.personalization.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.personalization.core.data.database.converter.ThemeConfigConverter
import com.app.personalization.feature_keyboard.data.dao.KeyboardThemeDao
import com.app.personalization.feature_widget.data.dao.WidgetConfigDao
import com.app.personalization.feature_widget.data.dao.WidgetThemeWallpaperDao
import com.app.personalization.feature_widget.data.dao.WidgetThemeIconDao
import com.app.personalization.feature_charging.data.dao.ChargingAnimationDao
import com.app.personalization.feature_widget.data.dao.WidgetThemeWidgetDao
import com.app.personalization.feature_widget.data.dao.WidgetItemDao
import com.app.personalization.feature_wallpaper.data.dao.StickerItemDao
import com.app.personalization.feature_wallpaper.data.dao.BackgroundItemDao
import com.app.personalization.feature_wallpaper.data.dao.TemplateDao
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.feature_widget.data.entity.WidgetConfig
import com.app.personalization.feature_widget.data.entity.WidgetThemeWallpaper
import com.app.personalization.feature_widget.data.entity.WidgetThemeIcon
import com.app.personalization.feature_charging.data.entity.ChargingAnimation
import com.app.personalization.feature_widget.data.entity.WidgetThemeWidget
import com.app.personalization.feature_widget.data.entity.WidgetItem
import com.app.personalization.feature_wallpaper.data.entity.StickerItem
import com.app.personalization.feature_wallpaper.data.entity.BackgroundItem
import com.app.personalization.feature_wallpaper.data.entity.Template

@Database(entities = [KeyboardTheme::class, WidgetConfig::class, WidgetThemeWallpaper::class, WidgetThemeIcon::class, ChargingAnimation::class, WidgetThemeWidget::class, WidgetItem::class, StickerItem::class, BackgroundItem::class, Template::class], version = 6, exportSchema = false)
@TypeConverters(ThemeConfigConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themeDao(): KeyboardThemeDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun wallpaperDao(): WidgetThemeWallpaperDao
    abstract fun iconPackDao(): WidgetThemeIconDao
    abstract fun chargingDao(): ChargingAnimationDao
    abstract fun widgetThemeDao(): WidgetThemeWidgetDao
    abstract fun widgetItemDao(): WidgetItemDao
    abstract fun stickerDao(): StickerItemDao
    abstract fun backgroundDao(): BackgroundItemDao
    abstract fun templateDao(): TemplateDao

    companion object {
        const val DATABASE_NAME = "personalization_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        com.app.personalization.feature_widget.data.migration.MigrateWidgetTheme.INSTANCE.migrate(db)
                        com.app.personalization.feature_widget.data.migration.MigrateWidgetThemeWallpaperCommon.INSTANCE.migrate(db)
                        com.app.personalization.feature_widget.data.migration.MigrateWidgetThemeIconV2.INSTANCE.migrate(db)
                        com.app.personalization.feature_charging.data.migration.MigrateChargingAnimation.INSTANCE.migrate(db)
                        com.app.personalization.feature_widget.data.migration.MigrateWidgetThemeWidget.INSTANCE.migrate(db)
                        com.app.personalization.feature_widget.data.migration.MigrateWidgetItemWeather.INSTANCE.migrate(db)
                        com.app.personalization.feature_wallpaper.data.migration.MigrateStickerItem.INSTANCE.migrate(db)
                        com.app.personalization.feature_wallpaper.data.migration.MigrateBackgroundItem.INSTANCE.migrate(db)
                        com.app.personalization.feature_wallpaper.data.migration.MigrateTemplate.INSTANCE.migrate(db)
                    }
                })
                .fallbackToDestructiveMigration() // Prevent crashes due to schema changes
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
