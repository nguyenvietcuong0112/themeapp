package com.theme.customizer.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel quản lý trạng thái Live-Preview của bộ Theme đang thiết kế.
 */
class CreateThemeViewModel : ViewModel() {

    private val _wallpaperState = MutableLiveData<String>()
    val wallpaperState: LiveData<String> = _wallpaperState

    private val _widgetState = MutableLiveData<String>()
    val widgetState: LiveData<String> = _widgetState

    private val _iconPackState = MutableLiveData<List<String>>()
    val iconPackState: LiveData<List<String>> = _iconPackState

    // Preset URL mẫu cấu hình từ CDN của bạn
    private val defaultWallpaper = "https://csc-themeapp-widget.pages.dev/themes/category/Aesthetic/theme_1/wallpapers/bg_wallpaper.png"
    private val defaultWidget = "https://csc-themeapp-widget.pages.dev/themes/category/Aesthetic/theme_1/bg_preview.png"
    private val defaultIcons = (1..24).map {
        "https://csc-themeapp-widget.pages.dev/themes/category/Aesthetic/theme_1/icons/ic_facebook.png"
    }

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
