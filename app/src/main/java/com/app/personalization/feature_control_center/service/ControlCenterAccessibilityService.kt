package com.app.personalization.feature_control_center.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.app.personalization.feature_control_center.model.ControlCenterPreferences

class ControlCenterAccessibilityService : AccessibilityService() {

    private lateinit var prefs: ControlCenterPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        prefs = ControlCenterPreferences(this)
        if (prefs.isEnabled) {
            ControlCenterOverlayService.start(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun takeScreenshot(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    companion object {
        var instance: ControlCenterAccessibilityService? = null
            private set
    }
}
