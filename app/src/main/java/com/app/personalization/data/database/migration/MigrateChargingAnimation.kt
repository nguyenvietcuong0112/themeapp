package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateChargingAnimation {
    companion object {
        val INSTANCE = MigrateChargingAnimation()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val anims = listOf(
            "('charging_1', 'Cyberpunk Circle', 'charging/charging_1', 1, 0)",
            "('charging_2', 'Water Bubbles', 'charging/charging_2', 1, 0)",
            "('charging_3', 'Neon Flow', 'charging/charging_3', 1, 0)",
            "('charging_4', 'Retro Pixel', 'charging/charging_4', 1, 0)",
            "('charging_5', 'Galaxy Nebula', 'charging/charging_5', 1, 0)"
        )
        for (anim in anims) {
            db.execSQL("INSERT OR IGNORE INTO charging_animations (id, name, folder, isFree, isFavorite) VALUES $anim")
        }
    }
}
