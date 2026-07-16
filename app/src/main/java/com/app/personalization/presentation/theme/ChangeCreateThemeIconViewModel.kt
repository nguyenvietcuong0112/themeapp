package com.app.personalization.presentation.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.AppDatabase
import com.app.personalization.data.database.entity.WidgetThemeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeIconViewModel(application: Application) : AndroidViewModel(application) {

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
