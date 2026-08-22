package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.controller

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Window
import android.view.WindowManager

class BrightnessController(private val context: Context) {

    private var onBrightnessChangedListener: ((current: Int, max: Int, percent: Float) -> Unit)? = null
    private val contentResolver = context.contentResolver
    private val handler = Handler(Looper.getMainLooper())

    private val brightnessObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            notifyBrightness()
        }
    }

    init {
        try {
            contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                brightnessObserver
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun canWriteSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    fun setOnBrightnessChangedListener(listener: (current: Int, max: Int, percent: Float) -> Unit) {
        this.onBrightnessChangedListener = listener
        notifyBrightness()
    }

    fun getMaxBrightness(): Int = 255

    fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
    }

    fun getBrightnessPercent(): Float {
        val current = getCurrentBrightness()
        return (current.toFloat() / getMaxBrightness()).coerceIn(0f, 1f)
    }

    fun setBrightnessPercent(percent: Float, window: Window? = null) {
        val max = getMaxBrightness()
        val target = (percent.coerceIn(0.02f, 1f) * max).toInt()
        setBrightness(target, window)
    }

    fun setBrightness(brightness: Int, window: Window? = null) {
        val safeBrightness = brightness.coerceIn(5, 255)

        // If window is provided, apply directly to window attributes for instant response
        window?.let { w ->
            val lp = w.attributes
            lp.screenBrightness = safeBrightness.toFloat() / 255f
            w.attributes = lp
        }

        // Apply system-wide if permission granted
        if (canWriteSettings()) {
            try {
                // Ensure automatic brightness is turned off if setting manual
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    safeBrightness
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        notifyBrightness(safeBrightness)
    }

    fun notifyBrightness(customCurrent: Int? = null) {
        val max = getMaxBrightness()
        val current = customCurrent ?: getCurrentBrightness()
        val percent = (current.toFloat() / max).coerceIn(0f, 1f)
        onBrightnessChangedListener?.invoke(current, max, percent)
    }

    fun release() {
        try {
            contentResolver.unregisterContentObserver(brightnessObserver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
