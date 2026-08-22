package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.controller.*
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.model.ControlThemeAssets

class ControlCenterPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivPanelBackground: ImageView
    private val viewPullBar: View

    // Connectivity
    private val ivConnectivityBg: ImageView
    private val btnWifi: ImageView
    private val btnBluetooth: ImageView
    private val btnAirplane: ImageView
    private val btnData: ImageView

    // Music
    private val ivMusicBg: ImageView
    private val tvMusicTitle: TextView
    private val tvMusicArtist: TextView
    private val btnMusicPrev: ImageView
    private val btnMusicPlayPause: ImageView
    private val btnMusicNext: ImageView

    // Middle Row
    private val btnOrientationLock: View
    private val ivOrientationLock: ImageView
    private val btnSilentMode: View
    private val ivSilentMode: ImageView
    private val btnFocus: View
    private val ivFocus: ImageView
    private val sliderBrightness: VerticalSliderView
    private val sliderVolume: VerticalSliderView

    // Bottom Row 1 Shortcuts
    private val btnFlashlight: View
    private val ivFlashlight: ImageView
    private val btnClock: View
    private val ivClock: ImageView
    private val btnCalculator: View
    private val ivCalculator: ImageView
    private val btnCamera: View
    private val ivCamera: ImageView

    // Bottom Row 2 Shortcuts
    private val btnRecording: View
    private val ivRecording: ImageView
    private val btnRecord: View
    private val ivRecord: ImageView
    private val btnBattery: View
    private val ivBattery: ImageView
    private val btnScreenshot: View
    private val ivScreenshot: ImageView

    // Bottom Row 3 Settings
    private val btnSetup: View
    private val ivSetup: ImageView

    // Controllers
    val flashlightController = FlashlightController(context)
    val volumeController = VolumeController(context)
    val brightnessController = BrightnessController(context)
    val connectivityController = ConnectivityController(context)
    val mediaController = MediaController(context)
    val shortcutController = ShortcutController(context)

    private var currentAssets: ControlThemeAssets? = null
    var onCloseRequested: (() -> Unit)? = null

    private var startY: Float = 0f

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_control_center_panel, this, true)

        ivPanelBackground = findViewById(R.id.ivPanelBackground)
        viewPullBar = findViewById(R.id.viewPullBar)

        ivConnectivityBg = findViewById(R.id.ivConnectivityBg)
        btnWifi = findViewById(R.id.btnWifi)
        btnBluetooth = findViewById(R.id.btnBluetooth)
        btnAirplane = findViewById(R.id.btnAirplane)
        btnData = findViewById(R.id.btnData)

        ivMusicBg = findViewById(R.id.ivMusicBg)
        tvMusicTitle = findViewById(R.id.tvMusicTitle)
        tvMusicArtist = findViewById(R.id.tvMusicArtist)
        btnMusicPrev = findViewById(R.id.btnMusicPrev)
        btnMusicPlayPause = findViewById(R.id.btnMusicPlayPause)
        btnMusicNext = findViewById(R.id.btnMusicNext)

        btnOrientationLock = findViewById(R.id.btnOrientationLock)
        ivOrientationLock = findViewById(R.id.ivOrientationLock)
        btnSilentMode = findViewById(R.id.btnSilentMode)
        ivSilentMode = findViewById(R.id.ivSilentMode)
        btnFocus = findViewById(R.id.btnFocus)
        ivFocus = findViewById(R.id.ivFocus)
        sliderBrightness = findViewById(R.id.sliderBrightness)
        sliderVolume = findViewById(R.id.sliderVolume)

        btnFlashlight = findViewById(R.id.btnFlashlight)
        ivFlashlight = findViewById(R.id.ivFlashlight)
        btnClock = findViewById(R.id.btnClock)
        ivClock = findViewById(R.id.ivClock)
        btnCalculator = findViewById(R.id.btnCalculator)
        ivCalculator = findViewById(R.id.ivCalculator)
        btnCamera = findViewById(R.id.btnCamera)
        ivCamera = findViewById(R.id.ivCamera)

        btnRecording = findViewById(R.id.btnRecording)
        ivRecording = findViewById(R.id.ivRecording)
        btnRecord = findViewById(R.id.btnRecord)
        ivRecord = findViewById(R.id.ivRecord)
        btnBattery = findViewById(R.id.btnBattery)
        ivBattery = findViewById(R.id.ivBattery)
        btnScreenshot = findViewById(R.id.btnScreenshot)
        ivScreenshot = findViewById(R.id.ivScreenshot)

        btnSetup = findViewById(R.id.btnSetup)
        ivSetup = findViewById(R.id.ivSetup)

        setupListeners()
    }

    private fun setupListeners() {
        viewPullBar.setOnClickListener {
            onCloseRequested?.invoke()
        }

        ivPanelBackground.setOnClickListener {
            onCloseRequested?.invoke()
        }

        var startTouchX = 0f
        var startTouchY = 0f

        // Swipe up or swipe right to close
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTouchX = event.x
                    startTouchY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.x - startTouchX
                    val deltaY = event.y - startTouchY
                    if (deltaY < -50 || deltaX > 80) { // Swipe up or swipe right
                        onCloseRequested?.invoke()
                    }
                    true
                }
                else -> false
            }
        }

        // Connectivity Toggles
        btnWifi.setOnClickListener {
            connectivityController.toggleWifi()
            updateUIStates()
        }

        btnBluetooth.setOnClickListener {
            connectivityController.toggleBluetooth()
            updateUIStates()
        }

        btnAirplane.setOnClickListener {
            connectivityController.openAirplaneSettings()
        }

        btnData.setOnClickListener {
            connectivityController.openDataSettings()
        }

        // Quick Action Toggles
        btnOrientationLock.setOnClickListener {
            connectivityController.toggleAutoRotation()
            updateUIStates()
        }

        btnSilentMode.setOnClickListener {
            connectivityController.toggleSilentMode()
            updateUIStates()
        }

        btnFocus.setOnClickListener {
            connectivityController.toggleSilentMode()
            updateUIStates()
        }

        // Shortcuts Row 1
        btnFlashlight.setOnClickListener {
            flashlightController.toggle()
            updateUIStates()
        }

        btnClock.setOnClickListener {
            shortcutController.openClock()
            onCloseRequested?.invoke()
        }

        btnCalculator.setOnClickListener {
            shortcutController.openCalculator()
            onCloseRequested?.invoke()
        }

        btnCamera.setOnClickListener {
            shortcutController.openCamera()
            onCloseRequested?.invoke()
        }

        // Shortcuts Row 2 & 3
        btnRecording.setOnClickListener {
            shortcutController.openVoiceRecorder()
            onCloseRequested?.invoke()
        }

        btnRecord.setOnClickListener {
            shortcutController.openScreenRecorder()
            onCloseRequested?.invoke()
        }

        btnBattery.setOnClickListener {
            shortcutController.openBatterySettings()
            onCloseRequested?.invoke()
        }

        btnScreenshot.setOnClickListener {
            onCloseRequested?.invoke()
            postDelayed({
                shortcutController.takeScreenshot()
            }, 300)
        }

        btnSetup.setOnClickListener {
            shortcutController.openSettings()
            onCloseRequested?.invoke()
        }

        // Media Controls
        btnMusicPlayPause.setOnClickListener {
            mediaController.playPause()
            postDelayed({ updateUIStates() }, 200)
        }

        btnMusicNext.setOnClickListener {
            mediaController.skipToNext()
        }

        btnMusicPrev.setOnClickListener {
            mediaController.skipToPrevious()
        }

        // Vertical Sliders
        sliderVolume.progress = volumeController.getVolumePercent()
        sliderVolume.setOnProgressChangeListener { percent, fromUser ->
            if (fromUser) {
                volumeController.setVolumePercent(percent)
            }
        }

        sliderBrightness.progress = brightnessController.getBrightnessPercent()
        sliderBrightness.setOnProgressChangeListener { percent, fromUser ->
            if (fromUser) {
                brightnessController.setBrightnessPercent(percent)
            }
        }

        // Observers
        volumeController.setOnVolumeChangedListener { _, _, percent ->
            sliderVolume.progress = percent
        }

        brightnessController.setOnBrightnessChangedListener { _, _, percent ->
            sliderBrightness.progress = percent
        }

        connectivityController.setOnConnectivityChangedListener {
            post { updateUIStates() }
        }

        flashlightController.setOnStateChangedListener {
            post { updateUIStates() }
        }
    }

    fun applyTheme(assets: ControlThemeAssets) {
        this.currentAssets = assets

        // Backgrounds
        ivPanelBackground.setImageBitmap(assets.homeBg)
        ivConnectivityBg.setImageBitmap(assets.controlBg)
        ivMusicBg.setImageBitmap(assets.musicBg)

        // Sliders
        sliderBrightness.setAssets(assets.lightBg, assets.lightSlide, assets.lightIcon)
        sliderVolume.setAssets(assets.soundBg, assets.soundSlide, assets.soundBig)

        // Media buttons
        btnMusicPrev.setImageBitmap(assets.musicPrev)
        btnMusicNext.setImageBitmap(assets.musicNext)

        // Bottom Row 1 Shortcuts
        ivFlashlight.setImageBitmap(assets.flashlightOff)
        ivClock.setImageBitmap(assets.icTiming)
        ivCalculator.setImageBitmap(assets.icCalculator)
        ivCamera.setImageBitmap(assets.icCamera)

        // Bottom Row 2 Shortcuts
        ivRecording.setImageBitmap(assets.icRecording ?: assets.icRecord)
        ivRecord.setImageBitmap(assets.icRecord ?: assets.icRecording)
        ivBattery.setImageBitmap(assets.icBattery)
        ivScreenshot.setImageBitmap(assets.icScreenshot ?: assets.icCamera)

        // Bottom Row 3 Settings
        ivSetup.setImageBitmap(assets.icSetup)

        // Pull bar color
        viewPullBar.backgroundTintList = android.content.res.ColorStateList.valueOf(assets.spec.pullBarColor)

        updateUIStates()
    }

    fun updateUIStates() {
        val a = currentAssets ?: return

        // Wi-Fi
        val isWifi = connectivityController.isWifiEnabled()
        btnWifi.setImageBitmap(if (isWifi) a.wifiOn ?: a.wifiOff else a.wifiOff)

        // Bluetooth
        val isBt = connectivityController.isBluetoothEnabled()
        btnBluetooth.setImageBitmap(if (isBt) a.btOn ?: a.btOff else a.btOff)

        // Airplane
        val isAirplane = connectivityController.isAirplaneModeOn()
        btnAirplane.setImageBitmap(if (isAirplane) a.airplaneOn ?: a.airplaneOff else a.airplaneOff)

        // Data
        btnData.setImageBitmap(a.dataOff)

        // Orientation Lock
        val isAutoRotate = connectivityController.isAutoRotationEnabled()
        ivOrientationLock.setImageBitmap(if (!isAutoRotate) a.lockOn ?: a.lockOff else a.lockOff)

        // Silent & Focus
        val isSilent = connectivityController.isSilentMode()
        ivSilentMode.setImageBitmap(if (isSilent) a.ringOn ?: a.ringOff else a.ringOff)
        ivFocus.setImageBitmap(if (isSilent) a.focusOn ?: a.focusOff else a.focusOff)

        // Flashlight
        val isTorch = flashlightController.isFlashlightOn
        ivFlashlight.setImageBitmap(if (isTorch) a.flashlightOn ?: a.flashlightOff else a.flashlightOff)

        // Music Play/Pause
        val isPlaying = mediaController.isMusicActive()
        btnMusicPlayPause.setImageBitmap(if (isPlaying) a.musicPause ?: a.musicPlay else a.musicPlay)

        // Sliders sync
        sliderVolume.progress = volumeController.getVolumePercent()
        sliderBrightness.progress = brightnessController.getBrightnessPercent()
    }

    fun release() {
        flashlightController.release()
        volumeController.release()
        brightnessController.release()
        connectivityController.release()
    }
}
