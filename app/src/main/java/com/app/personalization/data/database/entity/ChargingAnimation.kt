package com.app.personalization.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "charging_animations")
data class ChargingAnimation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val folder: String,
    val isFree: Boolean = true,
    var isFavorite: Boolean = false
) : java.io.Serializable
