package com.app.personalization.data

import kotlinx.serialization.Serializable

@Serializable
data class DecorateCategory(
    val category: String,
    val name: String,
    val themes: List<DecorateThemeItem>
)

@Serializable
data class DecorateThemeItem(
    val themeName: String,
    val themePath: String,
    val themeType: String,
    val isNew: Boolean = false,
    val isPremium: Boolean = false,
    val order: Int = 0
)
