package com.app.personalization.presentation.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.di.ServiceLocator
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

    private var selectedCategoryId: String = "all"
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
            
            // 1. Load Categories
            val assetCategories = withContext(Dispatchers.IO) {
                try {
                    context.assets.list("theme_decorates")?.toList() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val categoryTags = mutableListOf<CategoryTag>()
            categoryTags.add(CategoryTag("all", "All", isSelected = (selectedCategoryId == "all")))
            
            for (cat in assetCategories) {
                val displayName = cat.replace("-", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else it } }
                categoryTags.add(CategoryTag(cat.lowercase(), displayName, isSelected = (selectedCategoryId == cat.lowercase())))
            }
            categoryTags.add(CategoryTag("diy", "My DIY", isSelected = (selectedCategoryId == "diy")))
            _categories.value = categoryTags

            // 2. Load Themes
            val loadedThemes = withContext(Dispatchers.IO) {
                val list = mutableListOf<KeyboardTheme>()
                
                // Load preset default themes from assets
                for (cat in assetCategories) {
                    try {
                        val themeDirs = context.assets.list("theme_decorates/$cat") ?: emptyArray()
                        for (dirName in themeDirs) {
                            val path = "$cat/$dirName"
                            val hasConfig = try {
                                context.assets.open("theme_decorates/$path/config.json").use { true }
                            } catch (e: Exception) {
                                false
                            }
                            if (hasConfig) {
                                val name = dirName.replace("-", " ")
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else it } }
                                list.add(
                                    KeyboardTheme(
                                        id = "default_$path",
                                        categoryId = cat.lowercase(),
                                        name = name,
                                        path = path,
                                        rawType = "default",
                                        isPremium = (themeDirs.indexOf(dirName) % 2 == 1) // Set some themes as premium
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
        val filtered = when (selectedCategoryId) {
            "all" -> allLoadedThemes
            "diy" -> allLoadedThemes.filter { it.rawType == "diy" }
            else -> allLoadedThemes.filter { it.categoryId == selectedCategoryId }
        }
        _themes.value = filtered
    }
}
