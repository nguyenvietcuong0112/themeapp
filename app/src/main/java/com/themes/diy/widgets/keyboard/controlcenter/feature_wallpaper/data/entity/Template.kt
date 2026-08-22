package com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "diy_templates")
data class Template(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val templateFolder: String,
    val isLive: Boolean = false,
    val isFree: Boolean = true
) : java.io.Serializable
