package com.themes.diy.widgets.keyboard.controlcenter.core.data.repository

import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateCategory
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme

enum class DataSourceMode {
    LOCAL_ASSET,
    REMOTE_API
}

interface ThemeRepository {
    suspend fun getCategories(): List<DecorateCategory>
    suspend fun getThemes(categoryId: String? = null): List<KeyboardTheme>
    suspend fun getPresetThemes(categoryId: String? = null): List<KeyboardTheme>
    suspend fun getCustomThemes(): List<KeyboardTheme>
    suspend fun getThemeDetail(themePath: String): DecorateThemeItem?
    suspend fun saveCustomTheme(theme: KeyboardTheme)
    suspend fun deleteCustomTheme(theme: KeyboardTheme)
    fun setDataSourceMode(mode: DataSourceMode)
    fun getDataSourceMode(): DataSourceMode
}
