package com.app.personalization.presentation.wallpaper

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class WallpaperCategory(
    val id: String,
    val name: String,
    val isAll: Boolean = false
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val _categories = MutableLiveData<List<WallpaperCategory>>()
    val categories: LiveData<List<WallpaperCategory>> = _categories

    val selectedCategory = MutableLiveData<WallpaperCategory>()

    private val _wallpapers = MutableLiveData<List<WidgetThemeWallpaper>>()
    val wallpapers: LiveData<List<WidgetThemeWallpaper>> = _wallpapers

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    private var allWallpapersList = emptyList<WidgetThemeWallpaper>()

    init {
        loadOnlineCategoriesAndWallpapers()
    }

    private fun loadOnlineCategoriesAndWallpapers() {
        viewModelScope.launch(Dispatchers.IO) {
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

            // Map all themes to wallpapers
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

            val cats = list.map { it.category }.distinct().filter { it.isNotEmpty() }.map { catName ->
                val displayName = catName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                WallpaperCategory(catName, displayName)
            }
            val finalCats = listOf(WallpaperCategory("all", "All", true)) + cats

            withContext(Dispatchers.Main) {
                _categories.value = finalCats
                if (selectedCategory.value == null) {
                    selectedCategory.value = finalCats.first()
                } else {
                    loadWallpapers(false)
                }
            }
        }
    }

    fun getNumberOfColumns(context: Context): Int {
        val config = context.resources.configuration
        val isTablet = config.smallestScreenWidthDp >= 600
        val isUltraWide = config.screenWidthDp >= 1000
        return when {
            isUltraWide -> 5
            isTablet -> 4
            else -> 3
        }
    }

    fun loadWallpapers(loadMore: Boolean) {
        val cat = selectedCategory.value?.id ?: "all"
        val filtered = if (cat == "all") {
            allWallpapersList
        } else {
            allWallpapersList.filter { it.category == cat }
        }
        _wallpapers.value = filtered
    }

    fun favoriteWallpaper(wallpaper: WidgetThemeWallpaper) {
        // Toggle in-memory state for UI responsiveness
        wallpaper.isFavorite = !wallpaper.isFavorite
        _wallpapers.value = _wallpapers.value
    }

    fun loadCoins() {
        val prefs = getApplication<Application>().getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        _coins.value = prefs.getInt("user_coins", 100)
    }
}
