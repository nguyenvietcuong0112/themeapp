package com.app.personalization.presentation.theme

data class ThemeWidgetItem(
    val id: String,
    val name: String,
    val size: String,
    val providerClass: Class<*>,
    val previewUrl: String,
    var isSelected: Boolean = true
)
