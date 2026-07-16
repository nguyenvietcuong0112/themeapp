package com.app.personalization.presentation.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.AppDatabase
import com.app.personalization.data.database.entity.WidgetThemeWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeWidgetViewModel(application: Application) : AndroidViewModel(application) {

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
                // Load from theme_data_decorate.json (which is dynamically fetched from S3 CDN)
                val decorateCategories = try {
                    val urlConnection = java.net.URL("${com.app.personalization.data.ResourceConfig.S3_URL}/themes/json/theme_data_decorate.json?t=${System.currentTimeMillis()}").openConnection()
                    urlConnection.connectTimeout = 3000
                    urlConnection.readTimeout = 3000
                    val jsonStr = urlConnection.getInputStream().bufferedReader().use { it.readText() }
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                } catch (e: Exception) {
                    try {
                        val jsonStr = com.app.personalization.data.FileUtils.loadJsonFromAsset(getApplication(), "themes/json/theme_data_decorate.json")
                        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        emptyList()
                    }
                }

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
