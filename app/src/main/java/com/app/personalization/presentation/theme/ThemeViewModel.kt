package com.app.personalization.presentation.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.di.ServiceLocator
import com.app.personalization.data.FileUtils
import com.app.personalization.data.DecorateCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class CategoryTag(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _categories = MutableLiveData<List<CategoryTag>>()
    val categories: LiveData<List<CategoryTag>> = _categories

    private val _themes = MutableLiveData<List<KeyboardTheme>>()
    val themes: LiveData<List<KeyboardTheme>> = _themes

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    var selectedCategoryId: String = "trending"
    private var allLoadedThemes = listOf<KeyboardTheme>()

    init {
        loadCoins()
        loadCategoriesAndThemes()
    }

    fun loadCoins() {
        val prefs = getApplication<Application>().getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        _coins.value = prefs.getInt("user_coins", 100) // Default 100 coins
    }

    fun addCoins(amount: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("user_coins", 100)
        val updated = current + amount
        prefs.edit().putInt("user_coins", updated).apply()
        _coins.value = updated
    }

    private fun loadCategoriesAndThemes() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            
            // 1. Load & Parse Categories dynamically from S3 CDN or local assets fallback
            val decorateCategories = withContext(Dispatchers.IO) {
                try {
                    val urlConnection = java.net.URL("${com.app.personalization.data.ResourceConfig.S3_URL}/themes/json/theme_data_decorate.json?t=${System.currentTimeMillis()}").openConnection()
                    urlConnection.connectTimeout = 3000
                    urlConnection.readTimeout = 3000
                    val jsonStr = urlConnection.getInputStream().bufferedReader().use { it.readText() }
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<DecorateCategory>>(jsonStr)
                } catch (e: Exception) {
                    try {
                        val jsonStr = FileUtils.loadJsonFromAsset(context, "themes/json/theme_data_decorate.json")
                        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<DecorateCategory>>(jsonStr)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        emptyList()
                    }
                }
            }

            val categoryTags = mutableListOf<CategoryTag>()
            val sortedCategories = decorateCategories.sortedByDescending { it.category.equals("Trending", ignoreCase = true) }
            if (selectedCategoryId.isEmpty() && sortedCategories.isNotEmpty()) {
                selectedCategoryId = "trending"
            }
            
            for (decorCat in sortedCategories) {
                categoryTags.add(
                    CategoryTag(
                        id = decorCat.category.lowercase(),
                        name = decorCat.name,
                        isSelected = (selectedCategoryId == decorCat.category.lowercase())
                    )
                )
            }
            _categories.value = categoryTags

            // 2. Load Themes
            val loadedThemes = withContext(Dispatchers.IO) {
                val list = mutableListOf<KeyboardTheme>()
                
                // Load preset default themes from parsed JSON
                for (decorCat in decorateCategories) {
                    val catId = decorCat.category.lowercase()
                    for (decorTheme in decorCat.themes) {
                        val path = decorTheme.themePath
                        list.add(
                            KeyboardTheme(
                                id = "default_$path",
                                categoryId = catId,
                                name = decorTheme.themeName,
                                path = path,
                                rawType = "default",
                                isPremium = decorTheme.isPremium
                            )
                        )
                    }
                }

                // Load custom DIY themes from database
                try {
                    val dbThemes = ServiceLocator.getThemeDao(context).getAllThemes()
                    list.addAll(dbThemes)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                list
            }

            allLoadedThemes = loadedThemes
            filterThemes()
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        
        // Update selected state in categories list
        val updatedCategories = _categories.value?.map {
            it.copy(isSelected = (it.id == categoryId))
        } ?: emptyList()
        _categories.value = updatedCategories

        filterThemes()
    }

    fun loadRandomThemes() {
        viewModelScope.launch {
            allLoadedThemes = allLoadedThemes.shuffled()
            filterThemes()
        }
    }

    private fun filterThemes() {
        val filtered = allLoadedThemes.filter { it.categoryId == selectedCategoryId }
        _themes.value = filtered
    }
}
