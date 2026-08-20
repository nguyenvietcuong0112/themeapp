package com.app.personalization.feature_control_center

data class ControlTheme(
    val key: String,
    val name: String,
    val slug: String,
    val category: String,
    val categorySlug: String,
    val folderPath: String,
    val thumbPath: String,
    val previewPath: String,
    val isFree: Boolean = true,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val downloads: Long = 7654321
)

data class ControlCategory(
    val slug: String,
    val name: String,
    val themes: List<ControlTheme>
)
