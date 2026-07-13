package com.app.personalization.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.personalization.R

class PermissionActivity : AppCompatActivity() {

    private lateinit var btnGrantStorage: Button
    private lateinit var btnGrantLocation: Button
    private lateinit var btnGrantNotifications: Button
    private lateinit var btnGetStarted: Button

    // Register Activity Results for Permission Requests
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

        // Check if permissions are already granted, skip directly to MainActivity
        if (isStorageGranted() && isLocationGranted()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_permission)

        btnGrantStorage = findViewById(R.id.btnGrantStorage)
        btnGrantLocation = findViewById(R.id.btnGrantLocation)
        btnGrantNotifications = findViewById(R.id.btnGrantNotifications)
        btnGetStarted = findViewById(R.id.btnGetStarted)

        setupClickListeners()
        updateButtonStates()
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun setupClickListeners() {
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

        btnGrantLocation.setOnClickListener {
            if (isLocationGranted()) return@setOnClickListener
            
            requestLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        btnGrantNotifications.setOnClickListener {
            if (isNotificationsGranted()) return@setOnClickListener
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(this, "Notification permission not required for this Android version", Toast.LENGTH_SHORT).show()
            }
        }

        btnGetStarted.setOnClickListener {
            navigateToMain()
        }
    }

    private fun updateButtonStates() {
        // Storage Button
        if (isStorageGranted()) {
            btnGrantStorage.text = "Granted"
            btnGrantStorage.setBackgroundColor(Color.parseColor("#2E2E3E"))
            btnGrantStorage.setTextColor(Color.parseColor("#00E5FF"))
            btnGrantStorage.isEnabled = false
        } else {
            btnGrantStorage.text = "Grant"
            btnGrantStorage.setBackgroundColor(Color.parseColor("#00E5FF"))
            btnGrantStorage.setTextColor(Color.parseColor("#12121A"))
            btnGrantStorage.isEnabled = true
        }

        // Location Button
        if (isLocationGranted()) {
            btnGrantLocation.text = "Granted"
            btnGrantLocation.setBackgroundColor(Color.parseColor("#2E2E3E"))
            btnGrantLocation.setTextColor(Color.parseColor("#00E5FF"))
            btnGrantLocation.isEnabled = false
        } else {
            btnGrantLocation.text = "Grant"
            btnGrantLocation.setBackgroundColor(Color.parseColor("#00E5FF"))
            btnGrantLocation.setTextColor(Color.parseColor("#12121A"))
            btnGrantLocation.isEnabled = true
        }

        // Notifications Button
        if (isNotificationsGranted()) {
            btnGrantNotifications.text = "Granted"
            btnGrantNotifications.setBackgroundColor(Color.parseColor("#2E2E3E"))
            btnGrantNotifications.setTextColor(Color.parseColor("#00E5FF"))
            btnGrantNotifications.isEnabled = false
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                btnGrantNotifications.text = "N/A"
                btnGrantNotifications.setBackgroundColor(Color.parseColor("#2E2E3E"))
                btnGrantNotifications.setTextColor(Color.parseColor("#888899"))
                btnGrantNotifications.isEnabled = false
            } else {
                btnGrantNotifications.text = "Grant"
                btnGrantNotifications.setBackgroundColor(Color.parseColor("#00E5FF"))
                btnGrantNotifications.setTextColor(Color.parseColor("#12121A"))
                btnGrantNotifications.isEnabled = true
            }
        }
    }

    private fun isStorageGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
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

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
