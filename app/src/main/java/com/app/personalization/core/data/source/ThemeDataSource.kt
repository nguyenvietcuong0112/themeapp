package com.app.personalization.core.data.source

import com.app.personalization.feature_theme.data.model.DecorateCategory
import com.app.personalization.feature_theme.data.model.DecorateThemeItem
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme

/**
 * Interface trừu tượng hóa việc nạp dữ liệu (Categories, Themes, Decorates)
 * Hỗ trợ chuyển đổi linh hoạt giữa Local JSON Asset và Remote REST API.
 */
interface ThemeDataSource {
    suspend fun getDecorateCategories(): List<DecorateCategory>
    suspend fun getThemesByCategory(categoryId: String): List<KeyboardTheme>
    suspend fun getAllThemes(): List<KeyboardTheme>
    suspend fun getThemeDetail(themePath: String): DecorateThemeItem?
}
