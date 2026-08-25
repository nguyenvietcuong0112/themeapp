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
import com.bumptech.glide.Glide
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.model.ControlCenterPreferences
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.service.ControlCenterOverlayService

class ControlCenterPreviewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ivPreviewThumb: ImageView
    private lateinit var btnApplyTheme: TextView

    private lateinit var prefs: ControlCenterPreferences
    private var themePath: String = ""
    private var themeName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_center_preview)

        prefs = ControlCenterPreferences(this)

        themePath = intent.getStringExtra(EXTRA_THEME_PATH) ?: ""
        themeName = intent.getStringExtra(EXTRA_THEME_NAME) ?: "Control Theme"

        initViews()
        loadThemePreview()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvHeaderTitle)
        ivPreviewThumb = findViewById(R.id.ivPreviewThumb)
        btnApplyTheme = findViewById(R.id.btnApplyTheme)

        tvTitle.text = themeName
    }

    private fun loadThemePreview() {
        val thumbPath = if (themePath.startsWith("file://") || themePath.startsWith("http")) {
            themePath
        } else {
            "${com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.ASSET_BASE_URL}/$themePath/thumb.webp"
        }

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
