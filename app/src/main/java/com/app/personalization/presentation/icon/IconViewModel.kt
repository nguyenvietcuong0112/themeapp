package com.app.personalization.presentation.icon

import com.app.personalization.presentation.theme.CategoryTag

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.WidgetThemeIcon
import com.app.personalization.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconViewModel(application: Application) : AndroidViewModel(application) {

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
            val context = getApplication<Application>()

            // 1. Create categories list
            val categoryTags = listOf(
                CategoryTag("all", "All", isSelected = (selectedCategoryId == "all")),
                CategoryTag("aesthetic", "Aesthetic", isSelected = (selectedCategoryId == "aesthetic")),
                CategoryTag("cute", "Cute", isSelected = (selectedCategoryId == "cute")),
                CategoryTag("hot", "Trending", isSelected = (selectedCategoryId == "hot")),
                CategoryTag("anime", "Anime", isSelected = (selectedCategoryId == "anime")),
                CategoryTag("simple", "Simple", isSelected = (selectedCategoryId == "simple"))
            )
            _categories.value = categoryTags

            // 2. Load Icons from DB
            val loadedIcons = withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.getIconPackDao(context).getAllIcons()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            }

            allLoadedIcons = loadedIcons
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
