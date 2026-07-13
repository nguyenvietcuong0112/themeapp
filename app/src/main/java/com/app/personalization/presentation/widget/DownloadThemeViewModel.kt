package com.app.personalization.presentation.widget

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DownloadThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> = _coins

    init {
        loadCoins()
    }

    fun loadCoins() {
        _coins.value = prefs.getInt("user_coins", 100)
    }

    fun deductCoins(amount: Int): Boolean {
        val current = _coins.value ?: 0
        if (current >= amount) {
            val updated = current - amount
            prefs.edit().putInt("user_coins", updated).apply()
            _coins.value = updated
            com.app.personalization.data.EventBus.getDefault().post(com.app.personalization.data.CoinUpdatedEvent())
            return true
        }
        return false
    }

    fun addCoins(amount: Int) {
        val current = _coins.value ?: 0
        val updated = current + amount
        prefs.edit().putInt("user_coins", updated).apply()
        _coins.value = updated
        com.app.personalization.data.EventBus.getDefault().post(com.app.personalization.data.CoinUpdatedEvent())
    }

    fun isWallpaperUnlocked(wallpaperId: String, isFreeByDefault: Boolean): Boolean {
        return prefs.getBoolean("wp_unlocked_$wallpaperId", isFreeByDefault)
    }

    fun unlockWallpaper(wallpaperId: String) {
        prefs.edit().putBoolean("wp_unlocked_$wallpaperId", true).apply()
    }

    fun isIconPackUnlocked(themeId: String, isFreeByDefault: Boolean): Boolean {
        return prefs.getBoolean("icon_unlocked_$themeId", isFreeByDefault)
    }

    fun unlockIconPack(themeId: String) {
        prefs.edit().putBoolean("icon_unlocked_$themeId", true).apply()
    }

    fun isWidgetUnlocked(widgetId: String, isFreeByDefault: Boolean): Boolean {
        return prefs.getBoolean("widget_unlocked_$widgetId", isFreeByDefault)
    }

    fun unlockWidget(widgetId: String) {
        prefs.edit().putBoolean("widget_unlocked_$widgetId", true).apply()
    }
}
