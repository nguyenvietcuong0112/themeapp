package com.app.personalization.presentation.widget

import android.graphics.drawable.Drawable

data class ThemeIconItem(
    val id: String,
    val iconName: String,
    val assetPath: String,
    var targetPackageName: String? = null,
    var targetAppName: String? = null,
    var targetAppIcon: Drawable? = null,
    var isSelected: Boolean = true
)
