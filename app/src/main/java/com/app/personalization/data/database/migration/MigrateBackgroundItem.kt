package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateBackgroundItem {
    companion object {
        val INSTANCE = MigrateBackgroundItem()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val bgs = listOf(
            "('bg_pattern_1', 'Pattern Star', 'Backgrounds', 'pattern_star.png', 1)",
            "('bg_texture_1', 'Paper Texture', 'Textures', 'paper_texture.png', 1)",
            "('bg_wood_1', 'Wood Grain', 'Textures', 'wood_grain.png', 1)"
        )
        for (bg in bgs) {
            db.execSQL("INSERT OR IGNORE INTO background_items (id, name, category, imageName, isFree) VALUES $bg")
        }
    }
}
