package com.themes.diy.widgets.keyboard.controlcenter.core.data.source

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateCategory
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.themes.diy.widgets.keyboard.controlcenter.core.data.FileUtils
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Local DataSource đọc dữ liệu trực tiếp từ file JSON trong assets:
 * assets/themes/json/theme_data_decorate.json
 * Có In-Memory Caching để tối ưu hiệu năng.
 */
class AssetThemeDataSource(
    private val context: Context,
    private val jsonFilePath: String = "assets_theme/json/theme_data_decorate.json"
) : ThemeDataSource {

    private var cachedCategories: List<DecorateCategory>? = null
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    @Synchronized
    private fun getOrLoadCategoriesSync(): List<DecorateCategory> {
        cachedCategories?.let { return it }
        return try {
            val jsonStr = FileUtils.loadJsonFromAsset(context, jsonFilePath)
            val parsed = json.decodeFromString<List<DecorateCategory>>(jsonStr)
            cachedCategories = parsed
            parsed
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getDecorateCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        getOrLoadCategoriesSync()
    }

    override suspend fun getThemesByCategory(categoryId: String): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        val matchedCategory = categories.find { it.category.equals(categoryId, ignoreCase = true) }
            ?: return@withContext emptyList()

        matchedCategory.themes.map { item ->
            KeyboardTheme(
                id = "default_${item.themePath}",
                categoryId = matchedCategory.category.lowercase(),
                name = item.themeName,
                path = item.themePath,
                rawType = "default",
                isPremium = item.isPremium,
                downloads = item.downloads
            )
        }
    }

    override suspend fun getAllThemes(): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        val result = mutableListOf<KeyboardTheme>()
        for (cat in categories) {
            val catId = cat.category.lowercase()
            for (item in cat.themes) {
                result.add(
                    KeyboardTheme(
                        id = "default_${item.themePath}",
                        categoryId = catId,
                        name = item.themeName,
                        path = item.themePath,
                        rawType = "default",
                        isPremium = item.isPremium,
                        downloads = item.downloads
                    )
                )
            }
        }
        result
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        val categories = getOrLoadCategoriesSync()
        for (cat in categories) {
            val item = cat.themes.find { it.themePath.equals(themePath, ignoreCase = true) }
            if (item != null) return@withContext item
        }
        null
    }
}
