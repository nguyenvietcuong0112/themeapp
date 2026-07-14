package com.app.personalization.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.personalization.data.database.converter.Converters
import com.app.personalization.data.database.dao.ThemeDao
import com.app.personalization.data.database.dao.WallpaperDao
import com.app.personalization.data.database.dao.IconDao
import com.app.personalization.data.database.dao.WidgetDao
import com.app.personalization.data.database.entity.*
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Cơ sở dữ liệu chính cho các chủ đề Theme, hình nền, biểu tượng và tiện ích đi kèm.
 */
@Database(
    entities = [
        WidgetTheme::class,
        ThemeWallpaper::class,
        ThemeIconPack::class,
        AppIconItem::class,
        ThemeWidget::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ThemeDatabase : RoomDatabase() {

    abstract fun themeDao(): ThemeDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun iconDao(): IconDao
    abstract fun widgetDao(): WidgetDao

    companion object {
        @Volatile
        private var INSTANCE: ThemeDatabase? = null

        fun getDatabase(context: Context): ThemeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThemeDatabase::class.java,
                    "theme_builder_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Chạy một luồng background để ghi đè (populate) dữ liệu khởi tạo mặc định
                Executors.newSingleThreadExecutor().execute {
                    populateDefaultData(getDatabase(context))
                }
            }

            private fun populateDefaultData(database: ThemeDatabase) {
                val themeDao = database.themeDao()
                val wallpaperDao = database.wallpaperDao()
                val iconDao = database.iconDao()
                val widgetDao = database.widgetDao()

                // Tạo 3 chủ đề mặc định ban đầu
                val themeId1 = UUID.randomUUID()
                val themeId2 = UUID.randomUUID()
                val themeId3 = UUID.randomUUID()

                val defaultThemes = listOf(
                    WidgetTheme(themeId1, "Aesthetic Pastel", "theme_1", 1, isNew = true),
                    WidgetTheme(themeId2, "Dark Cyberpunk", "theme_2", 2),
                    WidgetTheme(themeId3, "Neon Vibe", "theme_3", 3)
                )
                themeDao.insertThemes(defaultThemes)

                // Tạo hình nền mặc định
                val wallpapers = listOf(
                    ThemeWallpaper(UUID.randomUUID(), themeId1, "theme_1", "bg_wallpaper"),
                    ThemeWallpaper(UUID.randomUUID(), themeId2, "theme_2", "bg_wallpaper"),
                    ThemeWallpaper(UUID.randomUUID(), themeId3, "theme_3", "bg_wallpaper")
                )
                wallpaperDao.insertWallpapers(wallpapers)

                // Tạo gói Icon và Icon con lẻ
                val iconPackId1 = UUID.randomUUID()
                val iconPackId2 = UUID.randomUUID()
                val iconPackId3 = UUID.randomUUID()

                iconDao.insertIconPack(ThemeIconPack(iconPackId1, themeId1, "theme_1", "Pastel Pack"))
                iconDao.insertIconPack(ThemeIconPack(iconPackId2, themeId2, "theme_2", "Cyber Pack"))
                iconDao.insertIconPack(ThemeIconPack(iconPackId3, themeId3, "theme_3", "Neon Pack"))

                val iconItems = listOf(
                    AppIconItem("facebook", iconPackId1, "Facebook", "com.facebook.katana"),
                    AppIconItem("tiktok", iconPackId1, "TikTok", "com.zhiliaoapp.musically"),
                    AppIconItem("facebook", iconPackId2, "Facebook", "com.facebook.katana"),
                    AppIconItem("tiktok", iconPackId2, "TikTok", "com.zhiliaoapp.musically"),
                    AppIconItem("facebook", iconPackId3, "Facebook", "com.facebook.katana"),
                    AppIconItem("tiktok", iconPackId3, "TikTok", "com.zhiliaoapp.musically")
                )
                iconDao.insertIconItems(iconItems)

                // Tạo Widget mặc định cho các Theme
                val widgets = listOf(
                    ThemeWidget(UUID.randomUUID(), themeId1, "templates/clock_pastel", "MEDIUM", "CLOCK"),
                    ThemeWidget(UUID.randomUUID(), themeId2, "templates/weather_cyber", "LARGE", "WEATHER"),
                    ThemeWidget(UUID.randomUUID(), themeId3, "templates/calendar_neon", "SMALL", "CALENDAR")
                )
                widgetDao.insertWidgets(widgets)
            }
        }
    }
}
