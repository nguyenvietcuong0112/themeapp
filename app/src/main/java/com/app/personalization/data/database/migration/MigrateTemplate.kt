package com.app.personalization.data.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateTemplate {
    companion object {
        val INSTANCE = MigrateTemplate()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        val templates = listOf(
            "('tmpl_1', 'Analog Classic Clock', 'template_clock_1', 0, 1)",
            "('tmpl_2', 'Futuristic HUD', 'template_hud_1', 0, 1)",
            "('tmpl_3', 'Interactive Particle Live', 'live_particle_1', 1, 1)"
        )
        for (tmpl in templates) {
            db.execSQL("INSERT OR IGNORE INTO diy_templates (id, name, templateFolder, isLive, isFree) VALUES $tmpl")
        }
    }
}
