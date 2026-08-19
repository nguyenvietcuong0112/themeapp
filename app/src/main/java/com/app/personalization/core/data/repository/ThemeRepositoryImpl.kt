package com.app.personalization.core.data.repository

import com.app.personalization.feature_theme.data.model.DecorateCategory
import com.app.personalization.feature_theme.data.model.DecorateThemeItem
import com.app.personalization.feature_keyboard.data.dao.KeyboardThemeDao
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.core.data.source.ThemeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThemeRepositoryImpl(
    private val assetDataSource: ThemeDataSource,
    private var apiDataSource: ThemeDataSource? = null,
    private val themeDao: KeyboardThemeDao
) : ThemeRepository {

    private var currentMode: DataSourceMode = DataSourceMode.LOCAL_ASSET

    private val activeDataSource: ThemeDataSource
        get() = if (currentMode == DataSourceMode.REMOTE_API && apiDataSource != null) {
            apiDataSource!!
        } else {
            assetDataSource
        }

    override suspend fun getCategories(): List<DecorateCategory> = withContext(Dispatchers.IO) {
        activeDataSource.getDecorateCategories()
    }

    override suspend fun getPresetThemes(categoryId: String?): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
            activeDataSource.getAllThemes()
        } else {
            activeDataSource.getThemesByCategory(categoryId)
        }
    }

    override suspend fun getThemes(categoryId: String?): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        val result = mutableListOf<KeyboardTheme>()
        // 1. Load preset themes from data source
        val presets = getPresetThemes(categoryId)
        result.addAll(presets)

        // 2. Load custom themes from local Room database
        try {
            val customList = themeDao.getAllThemes()
            if (categoryId.isNullOrEmpty() || categoryId.equals("all", ignoreCase = true)) {
                result.addAll(customList)
            } else {
                result.addAll(customList.filter { it.categoryId.equals(categoryId, ignoreCase = true) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result
    }

    override suspend fun getCustomThemes(): List<KeyboardTheme> = withContext(Dispatchers.IO) {
        try {
            themeDao.getAllThemes()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getThemeDetail(themePath: String): DecorateThemeItem? = withContext(Dispatchers.IO) {
        activeDataSource.getThemeDetail(themePath)
    }

    override suspend fun saveCustomTheme(theme: KeyboardTheme): Unit = withContext(Dispatchers.IO) {
        try {
            themeDao.insertTheme(theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Unit
    }

    override suspend fun deleteCustomTheme(theme: KeyboardTheme): Unit = withContext(Dispatchers.IO) {
        try {
            themeDao.deleteTheme(theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Unit
    }

    override fun setDataSourceMode(mode: DataSourceMode) {
        currentMode = mode
    }

    override fun getDataSourceMode(): DataSourceMode {
        return currentMode
    }
}
