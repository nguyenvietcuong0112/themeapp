package com.app.personalization.presentation.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.personalization.data.database.entity.WidgetTheme
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.data.database.entity.WidgetThemeWidget
import com.app.personalization.data.database.entity.WidgetThemeIcon
import java.util.UUID

class CreateThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _theme = MutableLiveData<WidgetTheme>()
    val theme: LiveData<WidgetTheme> = _theme

    private val _wallpaper = MutableLiveData<WidgetThemeWallpaper>()
    val wallpaper: LiveData<WidgetThemeWallpaper> = _wallpaper

    private val _widget = MutableLiveData<WidgetThemeWidget>()
    val widget: LiveData<WidgetThemeWidget> = _widget

    private val _icon = MutableLiveData<WidgetThemeIcon>()
    val icon: LiveData<WidgetThemeIcon> = _icon

    var isChanged = false

    // Default configuration corresponding to "category/Trending/theme_7"
    val defaultTheme = WidgetTheme(
        id = UUID.randomUUID(),
        name = "Custom Theme",
        folder = "category/Trending/theme_7",
        order = 999,
        isNew = false,
        isFavorite = false,
        isCustom = true
    )

    val defaultWallpaper = WidgetThemeWallpaper(
        id = "default_wallpaper",
        themeId = defaultTheme.id.toString(),
        name = "Default Wallpaper",
        order = 1,
        folder = "category/Trending/theme_7",
        imageBg = "bg_wallpaper"
    )

    val defaultWidget = WidgetThemeWidget(
        id = "default_widget",
        name = "Default Widget",
        folder = "category/Trending/theme_7",
        category = "clock",
        isFree = true,
        isFavorite = false
    )

    val defaultIcon = WidgetThemeIcon(
        id = "default_icon",
        name = "Default Icon",
        folder = "category/Trending/theme_7",
        category = "cute",
        isFree = true,
        isFavorite = false
    )

    init {
        loadTheme(null)
    }

    fun loadTheme(existingTheme: WidgetTheme? = null) {
        if (existingTheme != null) {
            _theme.value = existingTheme
            isChanged = false
            _wallpaper.value = WidgetThemeWallpaper(
                id = "loaded_wallpaper_${existingTheme.id}",
                themeId = existingTheme.id.toString(),
                name = "Wallpaper of ${existingTheme.name}",
                order = 1,
                folder = existingTheme.folder,
                imageBg = "bg_wallpaper"
            )
            _widget.value = WidgetThemeWidget(
                id = "loaded_widget_${existingTheme.id}",
                name = "Widget of ${existingTheme.name}",
                folder = existingTheme.folder,
                category = "clock",
                isFree = true,
                isFavorite = false
            )
            _icon.value = WidgetThemeIcon(
                id = "loaded_icon_${existingTheme.id}",
                name = "Icon of ${existingTheme.name}",
                folder = existingTheme.folder,
                category = "cute",
                isFree = true,
                isFavorite = false
            )
        } else {
            // New Custom configuration
            _theme.value = defaultTheme
            _wallpaper.value = defaultWallpaper
            _widget.value = defaultWidget
            _icon.value = defaultIcon
            isChanged = false
        }
    }

    fun loadWallpaper(selectedWallpaper: WidgetThemeWallpaper) {
        _wallpaper.value = selectedWallpaper
        isChanged = true
    }

    fun loadWidget(selectedWidget: WidgetThemeWidget) {
        _widget.value = selectedWidget
        isChanged = true
    }

    fun loadIcons(selectedIcon: WidgetThemeIcon) {
        _icon.value = selectedIcon
        isChanged = true
    }

    fun resetTheme() {
        loadTheme(null)
    }
}
