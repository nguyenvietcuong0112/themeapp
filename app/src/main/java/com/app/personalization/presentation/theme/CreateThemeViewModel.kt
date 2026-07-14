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

    // Default presets using theme_1 (Aesthetic Blue Sky theme on Cloudflare CDN)
    private val defaultWallpaper = "https://csc-themeapp-widget.pages.dev/theme_1/wallpapers/bg_wallpaper.png"
    private val defaultWidget = "https://csc-themeapp-widget.pages.dev/theme_1/bg_preview.png"
    private val defaultIcons = listOf(
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_facebook.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_instagram.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_messenger.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_tiktok.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_chrome.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_gmail.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_camera.png",
        "https://csc-themeapp-widget.pages.dev/theme_1/icons/ic_settings.png"
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
