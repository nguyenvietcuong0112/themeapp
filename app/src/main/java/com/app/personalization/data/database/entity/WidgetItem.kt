package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "widget_items")
data class WidgetItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val themeFolder: String,
    val name: String,
    val widgetType: String, // clock, weather, calendar
    val size: String, // small, medium, large
    val isFree: Boolean = true,
    var isFavorite: Boolean = false
) : java.io.Serializable
