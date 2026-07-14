package com.app.personalization.presentation.setting

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.personalization.R
import com.app.personalization.presentation.wallpaper.DIYWallpaperActivity
import com.app.personalization.presentation.widget.WidgetConfigActivity

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupClickListeners(view)
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<View>(R.id.toolbar) ?: return
        


        val titleText = toolbar.findViewById<android.widget.TextView>(R.id.titleTextView)
        titleText?.text = "Settings"
    }

    private fun setupClickListeners(view: View) {
        val context = requireContext()

        // 1. Customize Themes -> DIY Wallpaper
        view.findViewById<View>(R.id.siAddTheme)?.setOnClickListener {
            startActivity(Intent(context, DIYWallpaperActivity::class.java))
        }

        // 2. Customize Widgets -> Widget Configuration (small dial config)
        view.findViewById<View>(R.id.siAddWidget)?.setOnClickListener {
            // Start WidgetConfigActivity with a mock widgetId to let them customize directly
            val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, 9999)
            }
            startActivity(intent)
        }

        // 3. Charging Animation
        view.findViewById<View>(R.id.siStartCharging)?.setOnClickListener {
            showChargingAnimationDialog()
        }
    }

    private fun showChargingAnimationDialog() {
        val context = context ?: return
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

        val prefs = context.getSharedPreferences("charging_prefs", Context.MODE_PRIVATE)
        val currentFolder = prefs.getString("applied_charging_folder", "charging/charging_1")
        var selectedIdx = animFolders.indexOf(currentFolder).coerceAtLeast(0)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Charging Animation")
        builder.setSingleChoiceItems(animNames, selectedIdx) { _, which ->
            selectedIdx = which
        }
        builder.setPositiveButton("Apply") { dialog, _ ->
            val folder = animFolders[selectedIdx]
            prefs.edit().putString("applied_charging_folder", folder).apply()
            Toast.makeText(context, "${animNames[selectedIdx]} applied successfully! Plug in charger to preview.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingFragment()
    }
}
