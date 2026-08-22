package com.app.personalization.feature_control_center.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.app.personalization.R
import com.app.personalization.feature_control_center.model.ControlCenterPreferences
import com.app.personalization.feature_control_center.model.ControlThemeLoader
import com.app.personalization.feature_control_center.view.ControlCenterPanelView

class ControlCenterOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: ControlCenterPreferences
    private lateinit var themeLoader: ControlThemeLoader

    private var topTriggerView: View? = null
    private var rightEdgeTriggerView: View? = null
    private var panelView: ControlCenterPanelView? = null
    private var isPanelOpen = false

    private val CHANNEL_ID = "control_center_service_channel"
    private val NOTIF_ID = 2001

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = ControlCenterPreferences(this)
        themeLoader = ControlThemeLoader(this)

        createNotificationChannel()
        startForeground(NOTIF_ID, createNotification())

        createTriggerViews()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_RELOAD_THEME -> {
                reloadTheme()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_OPEN_PANEL -> {
                openControlCenter()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Control Center Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps Control Center edge gesture active"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, com.app.personalization.feature_control_center.ControlCenterPreviewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Control Center")
            .setContentText("Active in background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createTriggerViews() {
        if (!Settings.canDrawOverlays(this)) return

        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // 1. RIGHT EDGE HANDLE (Thanh mép phải: Hỗ trợ cả VUỐT SANG TRÁI lẫn CHẠM / BẤM VÀO THANH)
        if (rightEdgeTriggerView == null) {
            val touchAreaWidth = (36 * density).toInt()  // Vùng cảm ứng rộng 36dp để cực kỳ dễ chạm
            val touchAreaHeight = (120 * density).toInt() // Chiều cao 120dp rộng rãi

            val rightParams = WindowManager.LayoutParams(
                touchAreaWidth,
                touchAreaHeight,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 0
                y = (screenHeight * 0.28f).toInt() // Vị trí 28% từ trên xuống (ngay tầm ngón tay cái)
            }

            // Container chứa thanh pill nhìn thấy được ở mép phải
            val container = FrameLayout(this)
            
            // Thanh pill màu xanh tím nhìn thấy
            val pillView = View(this).apply {
                val pillWidth = (14 * density).toInt()
                val pillHeight = (80 * density).toInt()
                val lp = FrameLayout.LayoutParams(pillWidth, pillHeight).apply {
                    gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
                layoutParams = lp

                val gd = GradientDrawable().apply {
                    setColor(Color.parseColor("#995C6BC0")) // Màu tím xanh đẹp mắt
                    cornerRadii = floatArrayOf(
                        16f * density, 16f * density, // top-left
                        0f, 0f,                       // top-right
                        0f, 0f,                       // bottom-right
                        16f * density, 16f * density  // bottom-left
                    )
                }
                background = gd
            }
            container.addView(pillView)

            var startX = 0f
            var startY = 0f
            var downTime = 0L
            var isTouchActive = false

            container.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        downTime = System.currentTimeMillis()
                        isTouchActive = true
                        pillView.alpha = 0.6f
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = startX - event.rawX // Dương khi vuốt từ phải sang trái
                        val deltaY = Math.abs(event.rawY - startY)
                        
                        // Vuốt sang trái > 10dp hoặc vuốt dọc > 15dp -> Mở ngay!
                        if ((deltaX > 10 * density || deltaY > 15 * density) && isTouchActive && !isPanelOpen) {
                            isTouchActive = false
                            pillView.alpha = 1.0f
                            openControlCenter()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        pillView.alpha = 1.0f
                        val deltaX = startX - event.rawX
                        val duration = System.currentTimeMillis() - downTime
                        
                        // Nếu vuốt sang trái HOẶC CHẠM NHẸ (TAP < 400ms) -> ĐỀU MỞ ĐƯỢC!
                        if ((deltaX > 8 * density || duration < 400) && isTouchActive && !isPanelOpen) {
                            openControlCenter()
                        }
                        isTouchActive = false
                        true
                    }
                    else -> false
                }
            }

            this.rightEdgeTriggerView = container
            try {
                windowManager.addView(container, rightParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. TOP-RIGHT TRIGGER (Thanh mép trên bên phải: Vuốt xuống hoặc Chạm)
        if (topTriggerView == null) {
            val topWidth = (screenWidth * 0.52f).toInt()
            val topHeight = (42 * density).toInt()

            val topParams = WindowManager.LayoutParams(
                topWidth,
                topHeight,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 0
                y = 0
            }

            val topView = View(this).apply {
                val gd = GradientDrawable().apply {
                    setColor(Color.parseColor("#22FFFFFF"))
                    cornerRadius = 12f * density
                }
                background = gd
            }

            var startY = 0f
            var isDragging = false

            topView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                        isDragging = true
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = event.rawY - startY
                        if (deltaY > 10 * density && isDragging && !isPanelOpen) {
                            isDragging = false
                            openControlCenter()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val deltaY = event.rawY - startY
                        if (deltaY > 10 * density && isDragging && !isPanelOpen) {
                            openControlCenter()
                        }
                        isDragging = false
                        true
                    }
                    else -> false
                }
            }

            this.topTriggerView = topView
            try {
                windowManager.addView(topView, topParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openControlCenter() {
        if (isPanelOpen) return
        if (!Settings.canDrawOverlays(this)) return

        isPanelOpen = true

        // Vibrate if enabled
        if (prefs.vibrateOnOpen) {
            vibrate()
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            windowAnimations = android.R.style.Animation_Dialog
        }

        val panel = ControlCenterPanelView(this).apply {
            fitsSystemWindows = false
            systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
        val assets = themeLoader.loadTheme(prefs.activeThemePath)
        panel.applyTheme(assets)

        panel.onCloseRequested = {
            closeControlCenter()
        }

        this.panelView = panel
        try {
            windowManager.addView(panel, params)
        } catch (e: Exception) {
            e.printStackTrace()
            isPanelOpen = false
        }
    }

    fun closeControlCenter() {
        panelView?.let { panel ->
            try {
                panel.release()
                windowManager.removeView(panel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        panelView = null
        isPanelOpen = false
    }

    private fun reloadTheme() {
        if (isPanelOpen && panelView != null) {
            val assets = themeLoader.loadTheme(prefs.activeThemePath)
            panelView?.applyTheme(assets)
        }
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(30)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        closeControlCenter()
        topTriggerView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        topTriggerView = null

        rightEdgeTriggerView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        rightEdgeTriggerView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_RELOAD_THEME = "com.app.personalization.RELOAD_CONTROL_THEME"
        const val ACTION_STOP_SERVICE = "com.app.personalization.STOP_CONTROL_SERVICE"
        const val ACTION_OPEN_PANEL = "com.app.personalization.OPEN_CONTROL_PANEL"

        fun start(context: Context) {
            val intent = Intent(context, ControlCenterOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ControlCenterOverlayService::class.java)
            context.stopService(intent)
        }

        fun reloadTheme(context: Context) {
            val intent = Intent(context, ControlCenterOverlayService::class.java).apply {
                action = ACTION_RELOAD_THEME
            }
            context.startService(intent)
        }

        fun openPanel(context: Context) {
            val intent = Intent(context, ControlCenterOverlayService::class.java).apply {
                action = ACTION_OPEN_PANEL
            }
            context.startService(intent)
        }
    }
}
