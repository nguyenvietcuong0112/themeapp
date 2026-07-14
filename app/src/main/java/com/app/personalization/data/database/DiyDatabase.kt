package com.app.personalization.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.personalization.data.database.converter.Converters
import com.app.personalization.data.database.dao.DiyDao
import com.app.personalization.data.database.entity.DiyBackground
import com.app.personalization.data.database.entity.DiySticker
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Cơ sở dữ liệu chuyên biệt để quản lý các tài nguyên đồ họa tự thiết kế (DIY): hình nền họa tiết và sticker.
 */
@Database(
    entities = [
        DiyBackground::class,
        DiySticker::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DiyDatabase : RoomDatabase() {

    abstract fun diyDao(): DiyDao

    companion object {
        @Volatile
        private var INSTANCE: DiyDatabase? = null

        fun getDatabase(context: Context): DiyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiyDatabase::class.java,
                    "diy_resources_database"
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
                // Chạy một luồng background để ghi đè (populate) dữ liệu khởi tạo mặc định cho tài nguyên DIY
                Executors.newSingleThreadExecutor().execute {
                    populateDefaultDiyData(getDatabase(context))
                }
            }

            private fun populateDefaultDiyData(database: DiyDatabase) {
                val diyDao = database.diyDao()

                // Tạo danh sách hình nền DIY mẫu
                val defaultBackgrounds = listOf(
                    DiyBackground(UUID.randomUUID(), "Aesthetic", "pattern_blue_sky", "aesthetic"),
                    DiyBackground(UUID.randomUUID(), "Cute", "cute_pink_cat", "cute"),
                    DiyBackground(UUID.randomUUID(), "Texture", "vintage_paper", "textures")
                )
                diyDao.insertBackgrounds(defaultBackgrounds)

                // Tạo danh sách Sticker DIY mẫu
                val defaultStickers = listOf(
                    DiySticker(UUID.randomUUID(), "Wedding", "wedding_ring.svg", "wedding"),
                    DiySticker(UUID.randomUUID(), "Birthday", "birthday_cake.svg", "birthday"),
                    DiySticker(UUID.randomUUID(), "Flowers", "red_rose.svg", "flowers")
                )
                diyDao.insertStickers(defaultStickers)
            }
        }
    }
}
