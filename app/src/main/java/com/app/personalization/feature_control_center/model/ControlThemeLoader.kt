package com.app.personalization.feature_control_center.model

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

    fun loadTheme(themePath: String): ControlThemeAssets {
        val cleanPath = themePath.removePrefix("file:///android_asset/").removePrefix("android_asset/")
        val spec = loadSpec("$cleanPath/control_spec.json")

        return ControlThemeAssets(
            themeFolder = cleanPath,
            spec = spec,
            homeBg = loadBitmap("$cleanPath/home_bg.jpg"),
            controlBg = loadBitmap("$cleanPath/home_control_bg.png"),
            musicBg = loadBitmap("$cleanPath/home_music_bg.png"),
            wifiOff = loadBitmap("$cleanPath/common_control_wifi.png"),
            wifiOn = loadBitmap("$cleanPath/common_control_wifi_select.png"),
            btOff = loadBitmap("$cleanPath/common_control_ic_bluetooth.png"),
            btOn = loadBitmap("$cleanPath/common_control_ic_bluetooth_select.png"),
            airplaneOff = loadBitmap("$cleanPath/common_control_ic_airplane.png"),
            airplaneOn = loadBitmap("$cleanPath/common_control_ic_airplane_select.png"),
            dataOff = loadBitmap("$cleanPath/common_control_ic_data.png"),
            dataOn = loadBitmap("$cleanPath/common_control_ic_data_select.png"),
            lightBg = loadBitmap("$cleanPath/common_light_bg.png"),
            lightSlide = loadBitmap("$cleanPath/common_light_slide.png"),
            lightIcon = loadBitmap("$cleanPath/home_light_icon.png") ?: loadBitmap("$cleanPath/light_ic_light.png"),
            soundBg = loadBitmap("$cleanPath/common_home_sound_bg.png"),
            soundSlide = loadBitmap("$cleanPath/common_home_sound_slide.png"),
            soundBig = loadBitmap("$cleanPath/common_home_sound_big.png"),
            soundMute = loadBitmap("$cleanPath/common_home_sound_mute.png"),
            musicPlay = loadBitmap("$cleanPath/common_music_ic_play.png"),
            musicPause = loadBitmap("$cleanPath/common_music_ic_pause.png"),
            musicNext = loadBitmap("$cleanPath/common_music_ic_next.png"),
            musicPrev = loadBitmap("$cleanPath/common_music_ic_previous.png"),
            lockOff = loadBitmap("$cleanPath/home_second_ic_locking.png"),
            lockOn = loadBitmap("$cleanPath/home_second_ic_locking_select.png"),
            ringOff = loadBitmap("$cleanPath/home_second_ic_ring.png"),
            ringOn = loadBitmap("$cleanPath/home_second_ic_ring_select.png"),
            focusOff = loadBitmap("$cleanPath/home_second_ic_focus.png"),
            focusOn = loadBitmap("$cleanPath/home_second_ic_focus_select.png"),
            flashlightOff = loadBitmap("$cleanPath/home_bottom_ic_flashlight.png"),
            flashlightOn = loadBitmap("$cleanPath/home_bottom_ic_flashlight_select.png"),
            icCalculator = loadBitmap("$cleanPath/home_bottom_ic_calculator.png"),
            icCamera = loadBitmap("$cleanPath/home_bottom_ic_camera.png"),
            icTiming = loadBitmap("$cleanPath/home_bottom_ic_timing.png"),
            icBattery = loadBitmap("$cleanPath/home_bottom_ic_battery.png"),
            icSetup = loadBitmap("$cleanPath/home_bottom_ic_setup.png"),
            icRecord = loadBitmap("$cleanPath/home_bottom_ic_record.png"),
            icRecording = loadBitmap("$cleanPath/home_bottom_ic_recording.png"),
            icScreenshot = loadBitmap("$cleanPath/home_bottom_ic_screenshot.png")
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
