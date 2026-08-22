package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.model

import android.content.Context
import android.content.SharedPreferences

class ControlCenterPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("control_center_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ENABLED, value).apply()

    var activeThemePath: String
        get() = prefs.getString(KEY_ACTIVE_THEME_PATH, DEFAULT_THEME_PATH) ?: DEFAULT_THEME_PATH
        set(value) = prefs.edit().putString(KEY_ACTIVE_THEME_PATH, value).apply()

    var activeThemeName: String
        get() = prefs.getString(KEY_ACTIVE_THEME_NAME, "Autumn Study") ?: "Autumn Study"
        set(value) = prefs.edit().putString(KEY_ACTIVE_THEME_NAME, value).apply()

    var triggerPosition: String
        get() = prefs.getString(KEY_TRIGGER_POS, POS_TOP_RIGHT) ?: POS_TOP_RIGHT
        set(value) = prefs.edit().putString(KEY_TRIGGER_POS, value).apply()

    var triggerSizeDp: Int
        get() = prefs.getInt(KEY_TRIGGER_SIZE_DP, 100)
        set(value) = prefs.edit().putInt(KEY_TRIGGER_SIZE_DP, value).apply()

    var triggerThicknessDp: Int
        get() = prefs.getInt(KEY_TRIGGER_THICKNESS_DP, 14)
        set(value) = prefs.edit().putInt(KEY_TRIGGER_THICKNESS_DP, value).apply()

    var vibrateOnOpen: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ON_OPEN, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ON_OPEN, value).apply()

    companion object {
        private const val KEY_IS_ENABLED = "key_is_enabled"
        private const val KEY_ACTIVE_THEME_PATH = "key_active_theme_path"
        private const val KEY_ACTIVE_THEME_NAME = "key_active_theme_name"
        private const val KEY_TRIGGER_POS = "key_trigger_pos"
        private const val KEY_TRIGGER_SIZE_DP = "key_trigger_size_dp"
        private const val KEY_TRIGGER_THICKNESS_DP = "key_trigger_thickness_dp"
        private const val KEY_VIBRATE_ON_OPEN = "key_vibrate_on_open"

        const val POS_TOP_RIGHT = "top_right"
        const val POS_TOP_CENTER = "top_center"
        const val POS_RIGHT_EDGE = "right_edge"

        const val DEFAULT_THEME_PATH = "assets_control_center/control_themes/aesthetic/autumn_study"
    }
}
