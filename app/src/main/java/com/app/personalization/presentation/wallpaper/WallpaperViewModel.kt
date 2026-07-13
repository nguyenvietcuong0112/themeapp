package com.app.personalization.presentation.wallpaper

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WallpaperCategory(
    val id: String,
    val name: String,
    val isAll: Boolean = false
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val wallpaperDao = ServiceLocator.getWallpaperDao(application)

    private val _categories = MutableLiveData<List<WallpaperCategory>>()
    val categories: LiveData<List<WallpaperCategory>> = _categories

    val selectedCategory = MutableLiveData<WallpaperCategory>()

    private val _wallpapers = MutableLiveData<List<WidgetThemeWallpaper>>()
    val wallpapers: LiveData<List<WidgetThemeWallpaper>> = _wallpapers

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    init {
        // Initialize default categories
        val defaultCategories = listOf(
            WallpaperCategory("all", "All", true),
            WallpaperCategory("aesthetic", "Aesthetic"),
            WallpaperCategory("cute", "Cute"),
            WallpaperCategory("hot", "Hot"),
            WallpaperCategory("anime", "Anime")
        )
        _categories.value = defaultCategories
        selectedCategory.value = defaultCategories.first()

        // Prepopulate wallpapers if empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = wallpaperDao.getAllWallpapers()
            if (existing.isEmpty()) {
                val presets = listOf(
                    WidgetThemeWallpaper(
                        id = "wp_aes_1", themeId = "aesthetic", name = "Blue Sky",
                        order = 1, folder = "Aesthetic", imageBg = "blue-sky",
                        category = "aesthetic"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_aes_2", themeId = "aesthetic", name = "Purple Galaxy",
                        order = 2, folder = "Aesthetic", imageBg = "purple-galaxy",
                        category = "aesthetic"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_cute_1", themeId = "cute", name = "Pink Rose",
                        order = 3, folder = "Romantic", imageBg = "pink-rose",
                        category = "cute"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_cute_2", themeId = "cute", name = "Love Letters",
                        order = 4, folder = "Romantic", imageBg = "love and letters",
                        category = "cute"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_hot_1", themeId = "hot", name = "Butterflies",
                        order = 5, folder = "Trending", imageBg = "butterflies",
                        category = "hot"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_hot_2", themeId = "hot", name = "Neon Galaxy",
                        order = 6, folder = "Trending", imageBg = "galaxy",
                        category = "hot"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_anime_1", themeId = "anime", name = "Go Green",
                        order = 7, folder = "Simple", imageBg = "go-green",
                        category = "anime"
                    ),
                    WidgetThemeWallpaper(
                        id = "wp_anime_2", themeId = "anime", name = "Bubble Soap",
                        order = 8, folder = "Simple", imageBg = "bubble-soap",
                        category = "anime"
                    )
                )
                wallpaperDao.insertWallpapers(presets)
            }
            loadWallpapers(false)
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
        viewModelScope.launch(Dispatchers.IO) {
            val cat = selectedCategory.value?.id ?: "all"
            val list = if (cat == "all") {
                wallpaperDao.getAllWallpapers()
            } else {
                wallpaperDao.getWallpapersByCategory(cat)
            }
            withContext(Dispatchers.Main) {
                if (loadMore) {
                    val current = _wallpapers.value ?: emptyList()
                    _wallpapers.value = current + list
                } else {
                    _wallpapers.value = list
                }
            }
        }
    }

    fun favoriteWallpaper(wallpaper: WidgetThemeWallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpaper.isFavorite = !wallpaper.isFavorite
            wallpaperDao.updateWallpaper(wallpaper)
            loadWallpapers(false)
        }
    }

    fun loadCoins() {
        val prefs = getApplication<Application>().getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        _coins.value = prefs.getInt("user_coins", 100)
    }
}
