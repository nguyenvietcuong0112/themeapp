package com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.migration

import androidx.sqlite.db.SupportSQLiteDatabase

class MigrateWidgetTheme {
    companion object {
        val INSTANCE = MigrateWidgetTheme()
    }
    fun migrate(db: SupportSQLiteDatabase) {
        // Pre-populate widget themes if necessary
    }
}
