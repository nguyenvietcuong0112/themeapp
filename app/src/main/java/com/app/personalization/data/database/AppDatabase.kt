package com.app.personalization.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.personalization.data.database.converter.ThemeConfigConverter
import com.app.personalization.data.database.dao.KeyboardThemeDao
import com.app.personalization.data.database.dao.WidgetConfigDao
import com.app.personalization.data.database.dao.WidgetThemeWallpaperDao
import com.app.personalization.data.database.dao.WidgetThemeIconDao
import com.app.personalization.data.database.dao.ChargingAnimationDao
import com.app.personalization.data.database.dao.WidgetThemeWidgetDao
import com.app.personalization.data.database.dao.WidgetItemDao
import com.app.personalization.data.database.dao.StickerItemDao
import com.app.personalization.data.database.dao.BackgroundItemDao
import com.app.personalization.data.database.dao.TemplateDao
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.data.database.entity.WidgetThemeIcon
import com.app.personalization.data.database.entity.ChargingAnimation
import com.app.personalization.data.database.entity.WidgetThemeWidget
import com.app.personalization.data.database.entity.WidgetItem
import com.app.personalization.data.database.entity.StickerItem
import com.app.personalization.data.database.entity.BackgroundItem
import com.app.personalization.data.database.entity.Template

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
                        com.app.personalization.data.database.migration.MigrateWidgetTheme.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateWidgetThemeWallpaperCommon.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateWidgetThemeIconV2.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateChargingAnimation.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateWidgetThemeWidget.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateWidgetItemWeather.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateStickerItem.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateBackgroundItem.INSTANCE.migrate(db)
                        com.app.personalization.data.database.migration.MigrateTemplate.INSTANCE.migrate(db)
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
