package com.app.personalization.feature_main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.personalization.R

class PermissionActivity : AppCompatActivity() {

    private lateinit var btnGrantStorage: Button
    private lateinit var btnGrantOverlay: Button
    private lateinit var btnGrantWriteSettings: Button
    private lateinit var btnGrantNotifications: Button
    private lateinit var btnGrantLocation: Button
    private lateinit var btnGetStarted: Button

    // Launchers
    private val requestStorageLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        updateButtonStates()
        if (results.values.all { it }) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        updateButtonStates()
        if (results.values.all { it }) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updateButtonStates()
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If core permissions already granted, jump directly to Main
        if (isStorageGranted() && isOverlayGranted()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_permission)

        initViews()
        setupClickListeners()
        updateButtonStates()
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun initViews() {
        btnGrantStorage = findViewById(R.id.btnGrantStorage)
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay)
        btnGrantWriteSettings = findViewById(R.id.btnGrantWriteSettings)
        btnGrantNotifications = findViewById(R.id.btnGrantNotifications)
        btnGrantLocation = findViewById(R.id.btnGrantLocation)
        btnGetStarted = findViewById(R.id.btnGetStarted)
    }

    private fun setupClickListeners() {
        // 1. Storage
        btnGrantStorage.setOnClickListener {
            if (isStorageGranted()) return@setOnClickListener

            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            requestStorageLauncher.launch(permissions)
        }

        // 2. Display Over Other Apps (Overlay)
        btnGrantOverlay.setOnClickListener {
            if (isOverlayGranted()) return@setOnClickListener

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)

                com.app.personalization.core.utils.PermissionDetector.startDetectingPermission(
                    activity = this,
                    checkPermission = { isOverlayGranted() },
                    onGranted = {
                        updateButtonStates()
                        Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 3. Write Settings
        btnGrantWriteSettings.setOnClickListener {
            if (isWriteSettingsGranted()) return@setOnClickListener

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)

                com.app.personalization.core.utils.PermissionDetector.startDetectingPermission(
                    activity = this,
                    checkPermission = { isWriteSettingsGranted() },
                    onGranted = {
                        updateButtonStates()
                        Toast.makeText(this, "Settings permission granted!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 4. Notifications
        btnGrantNotifications.setOnClickListener {
            if (isNotificationsGranted()) return@setOnClickListener

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(this, "Notification permission not required for this version", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Location
        btnGrantLocation.setOnClickListener {
            if (isLocationGranted()) return@setOnClickListener

            requestLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Continue
        btnGetStarted.setOnClickListener {
            navigateToMain()
        }
    }

    private fun updateButtonStates() {
        applyButtonState(btnGrantStorage, isStorageGranted())
        applyButtonState(btnGrantOverlay, isOverlayGranted())
        applyButtonState(btnGrantWriteSettings, isWriteSettingsGranted())
        applyButtonState(btnGrantNotifications, isNotificationsGranted())
        applyButtonState(btnGrantLocation, isLocationGranted())
    }

    private fun applyButtonState(button: Button, isGranted: Boolean) {
        if (isGranted) {
            button.text = "Granted"
            button.setBackgroundColor(Color.parseColor("#2A2A3C"))
            button.setTextColor(Color.parseColor("#4CAF50")) // Green text
            button.isEnabled = false
        } else {
            button.text = "Grant"
            button.setBackgroundColor(Color.parseColor("#FF4081")) // Pink accent
            button.setTextColor(Color.parseColor("#FFFFFF"))
            button.isEnabled = true
        }
    }

    private fun isStorageGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isOverlayGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun isWriteSettingsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        } else {
            true
        }
    }

    private fun isLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onDestroy() {
        com.app.personalization.core.utils.PermissionDetector.stopDetecting()
        super.onDestroy()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
