package com.app.personalization.presentation.icon

import com.app.personalization.presentation.theme.CategoryTag

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.personalization.data.database.entity.WidgetThemeIcon
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

            val decorateCategories = withContext(Dispatchers.IO) {
                try {
                    val urlConnection = java.net.URL("${com.app.personalization.data.ResourceConfig.S3_URL}/themes/json/theme_data_decorate.json?t=${System.currentTimeMillis()}").openConnection()
                    urlConnection.connectTimeout = 3000
                    urlConnection.readTimeout = 3000
                    val jsonStr = urlConnection.getInputStream().bufferedReader().use { it.readText() }
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                } catch (e: Exception) {
                    try {
                        val jsonStr = com.app.personalization.data.FileUtils.loadJsonFromAsset(context, "themes/json/theme_data_decorate.json")
                        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        emptyList()
                    }
                }
            }

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
