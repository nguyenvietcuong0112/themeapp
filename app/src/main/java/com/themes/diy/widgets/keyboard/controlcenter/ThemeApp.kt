package com.themes.diy.widgets.keyboard.controlcenter

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.themes.diy.widgets.keyboard.controlcenter.feature_charging.ChargingActivity

class ThemeApp : Application() {

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_CONNECTED) {
                val launchIntent = Intent(context, ChargingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        registerReceiver(chargingReceiver, filter)

        setupFullScreenLifecycle()
        checkCdnConnection()
    }

    private fun checkCdnConnection() {
        Thread {
            val cdnBase = com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.ASSET_BASE_URL
            android.util.Log.i("CDN_CHECK", "==================================================================")
            android.util.Log.i("CDN_CHECK", "🚀 TESTING CLOUDFLARE CDN CONNECTION: $cdnBase")
            android.util.Log.i("CDN_CHECK", "==================================================================")

            val testEndpoints = listOf(
                "index.html",
                "assets_collection/collections.json",
                "assets_keyboard/themes/Animal/autumn/preview.png",
                "assets_theme/category/Aesthetic/blue-sky/bg_preview.png"
            )

            for (endpoint in testEndpoints) {
                val fullUrl = "$cdnBase/$endpoint"
                val start = System.currentTimeMillis()
                try {
                    val url = java.net.URL(fullUrl)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "HEAD"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val code = conn.responseCode
                    val duration = System.currentTimeMillis() - start
                    if (code in 200..299) {
                        android.util.Log.i("CDN_CHECK", "🟢 [CDN SUCCESS $code] $endpoint (${conn.contentLength} bytes in ${duration}ms)")
                    } else {
                        android.util.Log.w("CDN_CHECK", "🟡 [CDN HTTP $code] $endpoint (in ${duration}ms)")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CDN_CHECK", "🔴 [CDN FAILED] $endpoint -> Error: ${e.message}")
                }
            }
            android.util.Log.i("CDN_CHECK", "==================================================================")
        }.start()
    }

    private fun setupFullScreenLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                hideStatusBar(activity)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                hideStatusBar(activity)
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun hideStatusBar(activity: Activity) {
        val window = activity.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
