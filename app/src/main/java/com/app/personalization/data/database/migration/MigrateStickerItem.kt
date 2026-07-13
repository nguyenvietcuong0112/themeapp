package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateStickerItem {
    companion object {
        val INSTANCE = MigrateStickerItem()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val stickers = listOf(
            "('st_babies_62', 'Babies', 'Babies', 'ic_Babies_62.png', 1)",
            "('st_cat_1', 'Cute Cat', 'Cute', 'cute_cat.png', 1)",
            "('st_star_1', 'Neon Star', 'Aesthetic', 'star.png', 1)",
            "('st_sun_1', 'Golden Sun', 'Summer', 'sun.png', 1)"
        )
        for (st in stickers) {
            db.execSQL("INSERT OR IGNORE INTO sticker_items (id, name, category, imageName, isFree) VALUES $st")
        }
    }
}
