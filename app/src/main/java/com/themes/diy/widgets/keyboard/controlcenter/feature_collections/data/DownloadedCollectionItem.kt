package com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_collections")
data class DownloadedCollectionItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val targetPath: String,
    val previewPath: String,
    val downloads: Long = 7654321,
    val rawType: String = "",
    val extra: String? = null,
    val appliedTimestamp: Long = System.currentTimeMillis()
)
