package com.themes.diy.widgets.keyboard.controlcenter.feature_icon

import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.CategoryTag

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator.getThemeRepository(application)

    private val _categories = MutableLiveData<List<CategoryTag>>()
    val categories: LiveData<List<CategoryTag>> = _categories

    private val _icons = MutableLiveData<List<WidgetThemeIcon>>()
    val icons: LiveData<List<WidgetThemeIcon>> = _icons

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    private var selectedCategoryId: String = "all"
    private var allLoadedIcons = listOf<WidgetThemeIcon>()

    init {
        loadCoins()
        loadCategoriesAndIcons()
    }

    fun loadCoins() {
        val prefs = getApplication<Application>().getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        _coins.value = prefs.getInt("user_coins", 100)
    }

    private fun loadCategoriesAndIcons() {
        viewModelScope.launch {
            val decorateCategories = themeRepository.getCategories()

            // Exclude Aesthetic category to match Theme tab layout, and sort Trending to top
            val filteredCategories = decorateCategories.filter { !it.category.equals("Aesthetic", ignoreCase = true) }

            val list = filteredCategories.flatMap { cat ->
                cat.themes.map { t ->
                    WidgetThemeIcon(
                        id = "icon_${t.themePath.replace("/", "_")}",
                        name = t.themeName,
                        folder = t.themePath,
                        category = cat.category.lowercase(),
                        isFree = !t.isPremium
                    )
                }
            }
            allLoadedIcons = list

            val cats = filteredCategories.map { cat ->
                CategoryTag(
                    id = cat.category.lowercase(),
                    name = cat.name,
                    isSelected = (selectedCategoryId == cat.category.lowercase())
                )
            }.sortedByDescending { it.id == "trending" }

            val finalCats = listOf(CategoryTag("all", "All", isSelected = (selectedCategoryId == "all"))) + cats
            _categories.value = finalCats

            filterIcons()
        }
    }

    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        val updated = _categories.value?.map {
            it.copy(isSelected = (it.id == categoryId))
        } ?: emptyList()
        _categories.value = updated
        filterIcons()
    }

    private fun filterIcons() {
        val filtered = if (selectedCategoryId == "all") {
            allLoadedIcons
        } else {
            allLoadedIcons.filter { it.category == selectedCategoryId }
        }
        _icons.value = filtered
    }
}
