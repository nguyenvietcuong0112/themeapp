package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.model.ControlCenterPreferences
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.service.ControlCenterOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlCenterPreviewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ivPreviewThumb: ImageView
    private lateinit var ivAmbientBg: ImageView
    private lateinit var btnApplyTheme: TextView

    private lateinit var prefs: ControlCenterPreferences
    private var themePath: String = ""
    private var themeName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_center_preview)

        prefs = ControlCenterPreferences(this)

        val rawPath = intent.getStringExtra(EXTRA_THEME_PATH) ?: ""
        val rawName = intent.getStringExtra(EXTRA_THEME_NAME) ?: "Control Theme"
        val (_, resolvedName, resolvedPath) = ControlCenterRepository.resolveThemeMetadata(rawPath.substringAfterLast("/"), rawName, rawPath)
        themePath = resolvedPath
        themeName = resolvedName

        initViews()
        loadThemePreview()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvHeaderTitle)
        ivPreviewThumb = findViewById(R.id.ivPreviewThumb)
        ivAmbientBg = findViewById(R.id.ivAmbientBg)
        btnApplyTheme = findViewById(R.id.btnApplyTheme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ivAmbientBg.setRenderEffect(
                android.graphics.RenderEffect.createBlurEffect(
                    70f, 70f, android.graphics.Shader.TileMode.CLAMP
                )
            )
        }

        tvTitle.text = themeName
    }

    private fun loadThemePreview() {
        val thumbPath = if (themePath.startsWith("file://") || themePath.startsWith("http")) {
            themePath
        } else {
            "${com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.ASSET_BASE_URL}/$themePath/thumb.webp"
        }

        // Ambient blurred backdrop
        Glide.with(this)
            .load(thumbPath)
            .override(40, 80)
            .into(ivAmbientBg)

        // Mockup preview inside phone card
        Glide.with(this)
            .load(thumbPath)
            .placeholder(R.color.grayF2F2F2)
            .error(R.color.grayF2F2F2)
            .into(ivPreviewThumb)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnApplyTheme.setOnClickListener {
            if (!hasOverlayPermission()) {
                showPermissionBottomSheet()
            } else {
                applyControlTheme()
            }
        }
    }

    private fun showPermissionBottomSheet() {
        val bottomSheet = ControlCenterPermissionBottomSheet.newInstance().apply {
            onPermissionUpdated = {
                if (hasOverlayPermission()) {
                    applyControlTheme()
                }
            }
        }
        bottomSheet.show(supportFragmentManager, "permission_dialog")
    }

    private fun applyControlTheme() {
        val isAsset = try {
            assets.open("$themePath/control_spec.json").close()
            true
        } catch (_: Exception) {
            false
        }

        val targetDir = java.io.File(filesDir, themePath)
        val isDownloaded = java.io.File(targetDir, "control_spec.json").exists()

        if (isAsset || isDownloaded) {
            executeApply()
        } else {
            downloadAndApplyTheme()
        }
    }

    private fun downloadAndApplyTheme() {
        btnApplyTheme.isEnabled = false
        btnApplyTheme.text = "Applying..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val targetDir = java.io.File(filesDir, themePath)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val filesToDownload = listOf(
                    "control_spec.json",
                    "home_bg.jpg",
                    "home_control_bg.png",
                    "home_music_bg.png",
                    "common_control_wifi.png",
                    "common_control_wifi_select.png",
                    "common_control_ic_bluetooth.png",
                    "common_control_ic_bluetooth_select.png",
                    "common_control_ic_airplane.png",
                    "common_control_ic_airplane_select.png",
                    "common_control_ic_data.png",
                    "common_control_ic_data_select.png",
                    "common_light_bg.png",
                    "common_light_slide.png",
                    "home_light_icon.png",
                    "light_ic_light.png",
                    "common_home_sound_bg.png",
                    "common_home_sound_slide.png",
                    "common_home_sound_big.png",
                    "common_home_sound_mute.png",
                    "common_music_ic_play.png",
                    "common_music_ic_pause.png",
                    "common_music_ic_next.png",
                    "common_music_ic_previous.png",
                    "home_second_ic_locking.png",
                    "home_second_ic_locking_select.png",
                    "home_second_ic_ring.png",
                    "home_second_ic_ring_select.png",
                    "home_second_ic_focus.png",
                    "home_second_ic_focus_select.png",
                    "home_bottom_ic_flashlight.png",
                    "home_bottom_ic_flashlight_select.png",
                    "home_bottom_ic_calculator.png",
                    "home_bottom_ic_camera.png",
                    "home_bottom_ic_timing.png",
                    "home_bottom_ic_battery.png",
                    "home_bottom_ic_setup.png",
                    "home_bottom_ic_record.png",
                    "home_bottom_ic_recording.png",
                    "home_bottom_ic_screenshot.png",
                    "home_bottom_diy_bg.png",
                    "control_bg.png"
                )

                for (fn in filesToDownload) {
                    val file = java.io.File(targetDir, fn)
                    if (!file.exists()) {
                        try {
                            val url = java.net.URL("${com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.ASSET_BASE_URL}/$themePath/$fn")
                            val conn = url.openConnection()
                            conn.connectTimeout = 5000
                            conn.readTimeout = 5000
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                            val stream = conn.getInputStream()
                            file.outputStream().use { out ->
                                stream.copyTo(out)
                            }
                        } catch (_: Exception) {
                            // Non-critical file can fallback to default
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                btnApplyTheme.isEnabled = true
                btnApplyTheme.text = "Apply"
                executeApply()
            }
        }
    }

    private fun executeApply() {
        prefs.activeThemePath = themePath
        prefs.activeThemeName = themeName
        prefs.isEnabled = true

        ControlCenterOverlayService.start(this)
        ControlCenterOverlayService.reloadTheme(this)
        ControlCenterOverlayService.openPanel(this)

        Toast.makeText(this, "Control Center applied successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    companion object {
        const val EXTRA_THEME_PATH = "extra_theme_path"
        const val EXTRA_THEME_NAME = "extra_theme_name"

        fun start(context: Context, themePath: String, themeName: String) {
            val intent = Intent(context, ControlCenterPreviewActivity::class.java).apply {
                putExtra(EXTRA_THEME_PATH, themePath)
                putExtra(EXTRA_THEME_NAME, themeName)
            }
            context.startActivity(intent)
        }
    }
}
