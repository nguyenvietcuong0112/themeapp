package com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyLayoutItem(
    val label: String,
    val code: Int = 0,
    val keyWidthPercent: Float = 0.1f,
    val popupKeys: List<String> = emptyList()
)

@Serializable
data class LayoutConfig(
    val rows: List<List<KeyLayoutItem>>
)
