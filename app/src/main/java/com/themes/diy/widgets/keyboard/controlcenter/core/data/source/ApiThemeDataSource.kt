package com.themes.diy.widgets.keyboard.controlcenter.core.data.source

import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateCategory
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.data.model.DecorateThemeItem
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.data.entity.KeyboardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Remote DataSource kết nối tới REST API Server trong tương lai.
 * Cấu trúc Response JSON từ API Server sẽ tương thích hoàn toàn với DecorateCategory.
 */
class ApiThemeDataSource(
    private val baseUrl: String,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) : ThemeDataSource {

    private var cachedCategories: List<DecorateCategory>? = null

    override suspend fun getDecorateCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val endpoint = if (baseUrl.endsWith("/")) "${baseUrl}api/themes" else "$baseUrl/api/themes"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val parsed = json.decodeFromString<List<DecorateCategory>>(jsonStr)
                cachedCategories = parsed
                parsed
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getThemesByCategory(categoryId: String): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        val categories = getDecorateCategories()
        val matchedCategory = categories.find { it.category.equals(categoryId, ignoreCase = true) }
            ?: return@withContext emptyList()

        matchedCategory.themes.map { item ->
            KeyboardTheme(
                id = "default_${item.themePath}",
                categoryId = matchedCategory.category.lowercase(),
                name = item.themeName,
                path = item.themePath,
                rawType = "default",
                isPremium = item.isPremium
            )
        }
    }

    override suspend fun getAllThemes(): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        val categories = getDecorateCategories()
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
                        isPremium = item.isPremium
                    )
                )
            }
        }
        result
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        val categories = getDecorateCategories()
        for (cat in categories) {
            val item = cat.themes.find { it.themePath.equals(themePath, ignoreCase = true) }
            if (item != null) return@withContext item
        }
        null
    }
}
