package com.app.personalization.feature_theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.core.di.ServiceLocator
import kotlinx.coroutines.launch

data class CategoryTag(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ServiceLocator.getThemeRepository(application)

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
            // 1. Load Categories from Repository
            val decorateCategories = themeRepository.getCategories()

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

            // 2. Load Themes from Repository
            allLoadedThemes = themeRepository.getThemes()
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
