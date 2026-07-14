package com.app.personalization.presentation.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateThemeViewModel : ViewModel() {

    private val _wallpaperState = MutableLiveData<String>()
    val wallpaperState: LiveData<String> = _wallpaperState

    private val _widgetState = MutableLiveData<String>()
    val widgetState: LiveData<String> = _widgetState

    private val _iconPackState = MutableLiveData<List<String>>()
    val iconPackState: LiveData<List<String>> = _iconPackState

    // Default presets using CdnPathResolver
    private val defaultWallpaper = com.app.personalization.data.CdnPathResolver.getWallpaperFullUrl("theme_1", "bg_wallpaper.png")
    private val defaultWidget = com.app.personalization.data.CdnPathResolver.getThemePreviewUrl("theme_1")
    private val defaultIcons = listOf(
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "facebook"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "instagram"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "messenger"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "tiktok"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "chrome"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "gmail"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "camera"),
        com.app.personalization.data.CdnPathResolver.getSingleIconUrl("theme_1", "settings")
    )

    init {
        loadDefaultTheme()
    }

    fun loadDefaultTheme() {
        _wallpaperState.value = defaultWallpaper
        _widgetState.value = defaultWidget
        _iconPackState.value = defaultIcons
    }

    fun selectWallpaper(url: String) {
        _wallpaperState.value = url
    }

    fun selectWidget(url: String) {
        _widgetState.value = url
    }

    fun selectIconPack(list: List<String>) {
        _iconPackState.value = list
    }

    fun resetTheme() {
        loadDefaultTheme()
    }
}
