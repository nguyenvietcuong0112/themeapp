package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.json.JSONObject
import java.io.InputStream

data class ControlSpec(
    val pullBarColor: Int = Color.WHITE,
    val controlTextColor: Int = Color.WHITE,
    val brightnessTextColor: Int = Color.WHITE,
    val volumeTextColor: Int = Color.WHITE,
    val musicKnownTitleColor: Int = Color.WHITE,
    val musicKnownSingerColor: Int = Color.WHITE
)

data class ControlThemeAssets(
    val themeFolder: String,
    val spec: ControlSpec,
    val homeBg: Bitmap?,
    val controlBg: Bitmap?,
    val musicBg: Bitmap?,
    val wifiOff: Bitmap?,
    val wifiOn: Bitmap?,
    val btOff: Bitmap?,
    val btOn: Bitmap?,
    val airplaneOff: Bitmap?,
    val airplaneOn: Bitmap?,
    val dataOff: Bitmap?,
    val dataOn: Bitmap?,
    val lightBg: Bitmap?,
    val lightSlide: Bitmap?,
    val lightIcon: Bitmap?,
    val soundBg: Bitmap?,
    val soundSlide: Bitmap?,
    val soundBig: Bitmap?,
    val soundMute: Bitmap?,
    val musicPlay: Bitmap?,
    val musicPause: Bitmap?,
    val musicNext: Bitmap?,
    val musicPrev: Bitmap?,
    val lockOff: Bitmap?,
    val lockOn: Bitmap?,
    val ringOff: Bitmap?,
    val ringOn: Bitmap?,
    val focusOff: Bitmap?,
    val focusOn: Bitmap?,
    val flashlightOff: Bitmap?,
    val flashlightOn: Bitmap?,
    val icCalculator: Bitmap?,
    val icCamera: Bitmap?,
    val icTiming: Bitmap?,
    val icBattery: Bitmap?,
    val icSetup: Bitmap?,
    val icRecord: Bitmap? = null,
    val icRecording: Bitmap? = null,
    val icScreenshot: Bitmap? = null
)

class ControlThemeLoader(private val context: Context) {

    private val defaultFolder = "assets_control_center/control_themes/aesthetic/autumn_study"

    fun loadTheme(themePath: String): ControlThemeAssets {
        val cleanPath = themePath.removePrefix("file:///android_asset/").removePrefix("android_asset/")
        val spec = loadSpec("$cleanPath/control_spec.json")

        fun loadBitmapWithFallback(filename: String): Bitmap? {
            val primary = loadBitmap("$cleanPath/$filename")
            if (primary != null) return primary
            return loadBitmap("$defaultFolder/$filename")
        }

        return ControlThemeAssets(
            themeFolder = cleanPath,
            spec = spec,
            homeBg = loadBitmapWithFallback("home_bg.jpg"),
            controlBg = loadBitmapWithFallback("home_control_bg.png"),
            musicBg = loadBitmapWithFallback("home_music_bg.png"),
            wifiOff = loadBitmapWithFallback("common_control_wifi.png"),
            wifiOn = loadBitmapWithFallback("common_control_wifi_select.png"),
            btOff = loadBitmapWithFallback("common_control_ic_bluetooth.png"),
            btOn = loadBitmapWithFallback("common_control_ic_bluetooth_select.png"),
            airplaneOff = loadBitmapWithFallback("common_control_ic_airplane.png"),
            airplaneOn = loadBitmapWithFallback("common_control_ic_airplane_select.png"),
            dataOff = loadBitmapWithFallback("common_control_ic_data.png"),
            dataOn = loadBitmapWithFallback("common_control_ic_data_select.png"),
            lightBg = loadBitmapWithFallback("common_light_bg.png"),
            lightSlide = loadBitmapWithFallback("common_light_slide.png"),
            lightIcon = loadBitmapWithFallback("home_light_icon.png") ?: loadBitmapWithFallback("light_ic_light.png"),
            soundBg = loadBitmapWithFallback("common_home_sound_bg.png"),
            soundSlide = loadBitmapWithFallback("common_home_sound_slide.png"),
            soundBig = loadBitmapWithFallback("common_home_sound_big.png"),
            soundMute = loadBitmapWithFallback("common_home_sound_mute.png"),
            musicPlay = loadBitmapWithFallback("common_music_ic_play.png"),
            musicPause = loadBitmapWithFallback("common_music_ic_pause.png"),
            musicNext = loadBitmapWithFallback("common_music_ic_next.png"),
            musicPrev = loadBitmapWithFallback("common_music_ic_previous.png"),
            lockOff = loadBitmapWithFallback("home_second_ic_locking.png"),
            lockOn = loadBitmapWithFallback("home_second_ic_locking_select.png"),
            ringOff = loadBitmapWithFallback("home_second_ic_ring.png"),
            ringOn = loadBitmapWithFallback("home_second_ic_ring_select.png"),
            focusOff = loadBitmapWithFallback("home_second_ic_focus.png"),
            focusOn = loadBitmapWithFallback("home_second_ic_focus_select.png"),
            flashlightOff = loadBitmapWithFallback("home_bottom_ic_flashlight.png"),
            flashlightOn = loadBitmapWithFallback("home_bottom_ic_flashlight_select.png"),
            icCalculator = loadBitmapWithFallback("home_bottom_ic_calculator.png"),
            icCamera = loadBitmapWithFallback("home_bottom_ic_camera.png"),
            icTiming = loadBitmapWithFallback("home_bottom_ic_timing.png"),
            icBattery = loadBitmapWithFallback("home_bottom_ic_battery.png"),
            icSetup = loadBitmapWithFallback("home_bottom_ic_setup.png"),
            icRecord = loadBitmapWithFallback("home_bottom_ic_record.png"),
            icRecording = loadBitmapWithFallback("home_bottom_ic_recording.png"),
            icScreenshot = loadBitmapWithFallback("home_bottom_ic_screenshot.png")
        )
    }

    private fun loadSpec(jsonPath: String): ControlSpec {
        return try {
            val jsonStr = context.assets.open(jsonPath).bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)
            ControlSpec(
                pullBarColor = parseColorSafe(json.optString("pullBarColor"), Color.WHITE),
                controlTextColor = parseColorSafe(json.optString("controlTextColor"), Color.WHITE),
                brightnessTextColor = parseColorSafe(json.optString("brightnessTextColor"), Color.WHITE),
                volumeTextColor = parseColorSafe(json.optString("volumeTextColor"), Color.WHITE),
                musicKnownTitleColor = parseColorSafe(json.optString("musicKnownTitleColor"), Color.WHITE),
                musicKnownSingerColor = parseColorSafe(json.optString("musicKnownSingerColor"), Color.WHITE)
            )
        } catch (e: Exception) {
            ControlSpec()
        }
    }

    private fun parseColorSafe(colorStr: String?, defaultColor: Int): Int {
        if (colorStr.isNullOrBlank()) return defaultColor
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            defaultColor
        }
    }

    private fun loadBitmap(assetPath: String): Bitmap? {
        return try {
            val isStream: InputStream = context.assets.open(assetPath)
            isStream.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
