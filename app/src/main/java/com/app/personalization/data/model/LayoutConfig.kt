package com.app.personalization.data.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyConfig(
    val label: String,
    val code: Int = 0,
    val keyWidthPercent: Float = 0.1f,
    val popupKeys: List<String> = emptyList()
)

@Serializable
data class LayoutConfig(
    val rows: List<List<KeyConfig>>
)
