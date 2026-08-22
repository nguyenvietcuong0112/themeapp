package com.themes.diy.widgets.keyboard.controlcenter.feature_widget

data class ThemeWidgetItem(
    val id: String,
    val name: String,
    val size: String,
    val providerClass: Class<*>,
    val previewUrl: String,
    var isSelected: Boolean = true
)
