package com.app.personalization.presentation.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.AppDatabase
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val _wallpapers = MutableLiveData<List<WidgetThemeWallpaper>>()
    val wallpapers: LiveData<List<WidgetThemeWallpaper>> = _wallpapers

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    private var allWallpapersList = emptyList<WidgetThemeWallpaper>()

    fun loadWallpapers() {
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
                        WidgetThemeWallpaper(
                            id = "wp_${t.themePath.replace("/", "_")}",
                            themeId = t.themePath,
                            name = t.themeName,
                            order = t.order,
                            folder = t.themePath,
                            imageBg = "bg_wallpaper",
                            category = cat.name.lowercase(),
                            isNew = t.isNew
                        )
                    }
                }
                allWallpapersList = list

                val cats = list.map { it.category }.distinct().filter { it.isNotEmpty() }
                val finalCats = listOf("All", "New") + cats

                withContext(Dispatchers.Main) {
                    _categories.value = finalCats
                    filterWallpapers("All")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterWallpapers(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                when (category) {
                    "All" -> allWallpapersList
                    "New" -> allWallpapersList.filter { it.isNew }
                    else -> allWallpapersList.filter { it.category == category }
                }
            }
            _wallpapers.value = filtered
        }
    }
}
