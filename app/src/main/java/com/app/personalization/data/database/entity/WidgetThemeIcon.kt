package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "widget_theme_icons")
data class WidgetThemeIcon(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val folder: String,
    val category: String,
    val isFree: Boolean = true,
    var isFavorite: Boolean = false
) : java.io.Serializable
