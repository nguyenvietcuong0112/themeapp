package com.app.personalization.presentation.widget

import java.io.Serializable

data class WidgetTheme(
    val id: String,
    val name: String,
    val folder: String,
    var isFavorite: Boolean = false
) : Serializable
