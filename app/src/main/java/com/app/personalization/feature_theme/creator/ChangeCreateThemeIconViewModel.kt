package com.app.personalization.feature_theme.creator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.feature_widget.data.entity.WidgetThemeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeIconViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = com.app.personalization.core.di.ServiceLocator.getThemeRepository(application)

    private val _icons = MutableLiveData<List<WidgetThemeIcon>>()
    val icons: LiveData<List<WidgetThemeIcon>> = _icons

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    private var allIconsList = emptyList<WidgetThemeIcon>()

    fun loadIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val decorateCategories = themeRepository.getCategories()

                val list = decorateCategories.flatMap { cat ->
                    cat.themes.map { t ->
                        WidgetThemeIcon(
                            id = "icon_${t.themePath.replace("/", "_")}",
                            name = t.themeName,
                            folder = t.themePath,
                            category = cat.name.lowercase(),
                            isFree = true,
                            isFavorite = false
                        )
                    }
                }
                allIconsList = list

                val cats = list.map { it.category }.distinct().filter { it.isNotEmpty() }
                val finalCats = listOf("All") + cats

                withContext(Dispatchers.Main) {
                    _categories.value = finalCats
                    filterIcons("All")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterIcons(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                if (category == "All") {
                    allIconsList
                } else {
                    allIconsList.filter { it.category == category }
                }
            }
            _icons.value = filtered
        }
    }
}
