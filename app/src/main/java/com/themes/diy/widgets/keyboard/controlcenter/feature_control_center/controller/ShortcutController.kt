package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.service.ControlCenterAccessibilityService

class ShortcutController(private val context: Context) {

    fun openCalculator() {
        val intentList = listOf(
            Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR),
            Intent().setClassName("com.google.android.calculator", "com.android.calculator2.Calculator"),
            Intent().setClassName("com.android.calculator2", "com.android.calculator2.Calculator"),
            Intent().setClassName("com.sec.android.app.popupcalculator", "com.sec.android.app.popupcalculator.Calculator"),
            Intent().setClassName("com.miui.calculator", "com.miui.calculator.cal.CalculatorActivity")
        )

        for (intent in intentList) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // Try next
            }
        }
        // Fallback
        try {
            val fallback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openCamera() {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun openClock() {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun openVoiceRecorder() {
        val recorderIntents = listOf(
            Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION),
            Intent().setClassName("com.sec.android.app.voicenote", "com.sec.android.app.voicenote.main.VNMainActivity"),
            Intent().setClassName("com.google.android.apps.recorder", "com.google.android.apps.recorder.ui.MainActivity"),
            Intent().setClassName("com.miui.soundrecorder", "com.miui.soundrecorder.SoundRecorder"),
            Intent().setClassName("com.coloros.soundrecorder", "com.coloros.soundrecorder.SoundRecorder"),
            Intent().setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder")
        )
        for (intent in recorderIntents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // Try next
            }
        }
        // Fallback to alarm/clock
        openClock()
    }

    fun openScreenRecorder() {
        val screenRecIntents = listOf(
            Intent().setClassName("com.miui.screenrecorder", "com.miui.screenrecorder.activity.MainActivity"),
            Intent().setClassName("com.samsung.android.app.screenrecorder", "com.samsung.android.app.screenrecorder.ScreenRecorderActivity"),
            Intent().setClassName("com.coloros.screenrecorder", "com.coloros.screenrecorder.ScreenRecorderActivity")
        )
        for (intent in screenRecIntents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // Try next
            }
        }
        openCamera()
    }

    fun takeScreenshot(): Boolean {
        val accService = ControlCenterAccessibilityService.instance
        if (accService != null && accService.takeScreenshot()) {
            return true
        }
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (e2: Exception) {
                openSettings()
            }
        }
    }
}
