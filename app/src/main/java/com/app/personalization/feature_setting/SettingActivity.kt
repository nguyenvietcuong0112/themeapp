package com.app.personalization.feature_setting

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // VIP / Crown Button
        binding.btnPremium.setOnClickListener {
            showPremiumDialog()
        }

        // 1. Language
        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // 2. Rate App
        binding.btnRateApp.setOnClickListener {
            rateApp()
        }

        // 3. Share App
        binding.btnShareApp.setOnClickListener {
            shareApp()
        }

        // 4. Policy
        binding.btnPolicy.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            "English",
            "Tiếng Việt",
            "Español",
            "Français",
            "Deutsch",
            "日本語",
            "한국어",
            "Português"
        )
        var selectedIdx = 0

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedIdx) { _, which ->
                selectedIdx = which
            }
            .setPositiveButton("OK") { dialog, _ ->
                Toast.makeText(this, "Language switched to ${languages[selectedIdx]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun rateApp() {
        val packageName = packageName
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun shareApp() {
        val packageName = packageName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Themes & Widgets App")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this amazing Theme & Widget customization app! Download here: https://play.google.com/store/apps/details?id=$packageName"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun openPrivacyPolicy() {
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://policies.google.com/privacy")
            )
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open privacy policy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle("👑 VIP Premium")
            .setMessage("Unlock all exclusive aesthetic themes, custom widgets, live charging animations and premium control center designs!")
            .setPositiveButton("Upgrade") { dialog, _ ->
                Toast.makeText(this, "Welcome to VIP!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
