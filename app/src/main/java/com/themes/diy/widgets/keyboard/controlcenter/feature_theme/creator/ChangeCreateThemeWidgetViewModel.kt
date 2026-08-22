package com.themes.diy.widgets.keyboard.controlcenter.feature_theme.creator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.data.entity.WidgetThemeWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeWidgetViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator.getThemeRepository(application)

    private val _widgets = MutableLiveData<List<WidgetThemeWidget>>()
    val widgets: LiveData<List<WidgetThemeWidget>> = _widgets

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    private var allWidgetsList = emptyList<WidgetThemeWidget>()

    fun loadWidgets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val decorateCategories = themeRepository.getCategories()

                val list = decorateCategories.flatMap { cat ->
                    cat.themes.map { t ->
                        WidgetThemeWidget(
                            id = "widget_${t.themePath.replace("/", "_")}",
                            name = t.themeName,
                            folder = t.themePath,
                            category = cat.name.lowercase(),
                            isFree = true,
                            isFavorite = false
                        )
                    }
                }
                allWidgetsList = list

                val cats = list.map { it.category }.distinct().filter { it.isNotEmpty() }
                val finalCats = listOf("All") + cats

                withContext(Dispatchers.Main) {
                    _categories.value = finalCats
                    filterWidgets("All")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterWidgets(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                if (category == "All") {
                    allWidgetsList
                } else {
                    allWidgetsList.filter { it.category == category }
                }
            }
            _widgets.value = filtered
        }
    }
}
