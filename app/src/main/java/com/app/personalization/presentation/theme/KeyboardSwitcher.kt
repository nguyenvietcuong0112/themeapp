package com.app.personalization.presentation.theme

import android.content.Context
import android.view.inputmethod.EditorInfo
import com.app.personalization.data.database.entity.KeyboardTheme
import kotlinx.serialization.json.Json

class KeyboardSwitcher private constructor() {

    private var currentThemeId: String? = null

    companion object {
        private var instance: KeyboardSwitcher? = null

        fun getInstance(): KeyboardSwitcher {
            if (instance == null) {
                instance = KeyboardSwitcher()
            }
            return instance!!
        }
    }

    fun updateKeyboardTheme(displayContext: Context, imeService: CustomKeyboardIME) {
        val prefs = displayContext.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        
        // Check for serialized KEYBOARD_THEME or selected_theme_id
        val themeId = prefs.getString("selected_theme_id", null)
        val jsonTheme = prefs.getString("KEYBOARD_THEME", null)
        
        val resolvedThemeId = if (jsonTheme != null) {
            try {
                val theme = Json { ignoreUnknownKeys = true }.decodeFromString<KeyboardTheme>(jsonTheme)
                theme.id
            } catch (e: Exception) {
                themeId
            }
        } else {
            themeId
        }

        if (resolvedThemeId != currentThemeId) {
            currentThemeId = resolvedThemeId
            imeService.reloadActiveTheme()
        }
    }

    fun loadKeyboard(editorInfo: EditorInfo, imeService: CustomKeyboardIME) {
        imeService.reloadActiveTheme()
    }
}
