package com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data

data class CollectionTab(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

data class CollectionItem(
    val id: String,
    val name: String,
    val category: String,
    val targetPath: String,
    val previewPath: String,
    val downloads: Long = 7654321,
    val rawType: String = "",
    val extra: String? = null
)
