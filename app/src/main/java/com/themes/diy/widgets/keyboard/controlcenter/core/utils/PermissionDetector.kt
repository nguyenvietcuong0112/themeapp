package com.themes.diy.widgets.keyboard.controlcenter.core.utils

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper

object PermissionDetector {

    private val handler = Handler(Looper.getMainLooper())
    private var activeRunnable: Runnable? = null

    /**
     * Polls for permission state changes every 400ms while user is in System Settings.
     * As soon as permission is granted, brings the activity back to front automatically.
     */
    fun startDetectingPermission(
        activity: Activity,
        checkPermission: () -> Boolean,
        onGranted: () -> Unit
    ) {
        stopDetecting()

        var attempts = 0
        val maxAttempts = 150 // 60 seconds timeout

        val runnable = object : Runnable {
            override fun run() {
                attempts++
                if (checkPermission()) {
                    bringActivityToFront(activity)
                    onGranted()
                    stopDetecting()
                } else if (attempts < maxAttempts && !activity.isFinishing && !activity.isDestroyed) {
                    handler.postDelayed(this, 400)
                }
            }
        }
        activeRunnable = runnable
        handler.postDelayed(runnable, 600)
    }

    fun stopDetecting() {
        activeRunnable?.let { handler.removeCallbacks(it) }
        activeRunnable = null
    }

    fun bringActivityToFront(activity: Activity) {
        try {
            val intent = Intent(activity, activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
