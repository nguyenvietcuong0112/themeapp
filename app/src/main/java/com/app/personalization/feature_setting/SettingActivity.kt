package com.app.personalization.feature_setting

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.R
import com.app.personalization.databinding.ActivitySettingBinding
import com.app.personalization.feature_wallpaper.diy.DIYWallpaperActivity
import com.app.personalization.feature_widget.WidgetConfigActivity

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.titleTextView.text = getString(R.string.setting)
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // 1. Customize Themes -> DIY Wallpaper
        binding.siAddTheme.setOnClickListener {
            startActivity(Intent(this, DIYWallpaperActivity::class.java))
        }

        // 2. Customize Widgets -> Widget Configuration
        binding.siAddWidget.setOnClickListener {
            val intent = Intent(this, WidgetConfigActivity::class.java).apply {
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, 9999)
            }
            startActivity(intent)
        }

        // 3. Charging Animation
        binding.siStartCharging.setOnClickListener {
            showChargingAnimationDialog()
        }

        // 4. FAQ
        binding.siFQA.setOnClickListener {
            Toast.makeText(this, "Frequently Asked Questions", Toast.LENGTH_SHORT).show()
        }

        // 5. How to install icons
        binding.siGetIcon.setOnClickListener {
            Toast.makeText(this, "Select an Icon Pack and click Apply to add shortcuts", Toast.LENGTH_LONG).show()
        }

        // 6. How to add widgets
        binding.siGetWidget.setOnClickListener {
            Toast.makeText(this, "Select a Widget and click Add Widget to Home Screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun showChargingAnimationDialog() {
        val animNames = arrayOf(
            "Cyberpunk Circle (Theme 1)",
            "Water Bubbles (Theme 2)",
            "Neon Flow (Theme 3)",
            "Retro Pixel (Theme 4)",
            "Galaxy Nebula (Theme 5)"
        )
        val animFolders = arrayOf(
            "charging/charging_1",
            "charging/charging_2",
            "charging/charging_3",
            "charging/charging_4",
            "charging/charging_5"
        )

        val prefs = getSharedPreferences("charging_prefs", Context.MODE_PRIVATE)
        val currentFolder = prefs.getString("applied_charging_folder", "charging/charging_1")
        var selectedIdx = animFolders.indexOf(currentFolder).coerceAtLeast(0)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Charging Animation")
        builder.setSingleChoiceItems(animNames, selectedIdx) { _, which ->
            selectedIdx = which
        }
        builder.setPositiveButton("Apply") { dialog, _ ->
            val folder = animFolders[selectedIdx]
            prefs.edit().putString("applied_charging_folder", folder).apply()
            Toast.makeText(this, "${animNames[selectedIdx]} applied successfully!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
