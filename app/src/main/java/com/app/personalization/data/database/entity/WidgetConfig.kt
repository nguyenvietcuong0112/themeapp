package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_configs")
data class WidgetConfig(
    @PrimaryKey val widgetId: Int,
    val bgType: String, // "COLOR", "IMAGE", or "GRADIENT"
    val solidColor: Int,
    val imageUri: String?,
    val textColor: Int,
    val fontStyle: String,
    val gradientStartColor: Int = 0xFFFFFFFF.toInt(),
    val gradientEndColor: Int = 0xFF000000.toInt()
)
