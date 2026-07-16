package com.app.personalization.presentation.theme

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import com.app.personalization.R
import com.app.personalization.databinding.ActivitySetupKeyboardBinding

class SetupKeyboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupKeyboardBinding
    private val handler = Handler(Looper.getMainLooper())

    private val checkStep1Runnable = object : Runnable {
        override fun run() {
            if (isKeyboardEnabled()) {
                updateStepStates()
            } else {
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Close Button
        binding.ivClose.setOnClickListener {
            finish()
        }

        // Setup Clicks
        binding.tvEnabledStep1.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        binding.tvEnabledStep2.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        updateStepStates()
    }

    override fun onResume() {
        super.onResume()
        // Start polling Step 1 status when user returns
        handler.removeCallbacks(checkStep1Runnable)
        if (!isKeyboardEnabled()) {
            handler.post(checkStep1Runnable)
        } else {
            updateStepStates()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkStep1Runnable)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            updateStepStates()
            if (isKeyboardEnabled() && isKeyboardDefault()) {
                showSuccessDialog()
            }
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val list = imm.enabledInputMethodList ?: return false
        for (info in list) {
            if (info.id.contains(packageName) && info.id.contains("CustomKeyboardIME")) {
                return true
            }
        }
        return false
    }

    private fun isKeyboardDefault(): Boolean {
        val currentIme = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false
        return currentIme.contains(packageName)
    }

    private fun updateStepStates() {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.primaryColor, typedValue, true)
        val primaryColor = typedValue.data
        theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true)
        val inactiveBg = typedValue.data

        val enabled = isKeyboardEnabled()

        if (!enabled) {
            // Step 1 active, Step 2 inactive
            binding.tvEnabledStep1.isEnabled = true
            binding.tvEnabledStep1.text = "Click to settings"
            binding.tvEnabledStep1.backgroundTintList = ColorStateList.valueOf(primaryColor)

            binding.tvEnabledStep2.isEnabled = false
            binding.tvEnabledStep2.text = "Click to settings"
            binding.tvEnabledStep2.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            binding.tvEnabledStep2.setTextColor(Color.GRAY)
        } else {
            // Step 1 finished, Step 2 active
            binding.tvEnabledStep1.isEnabled = false
            binding.tvEnabledStep1.text = "Enabled"
            binding.tvEnabledStep1.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            binding.tvEnabledStep1.setTextColor(Color.GRAY)

            val isDefault = isKeyboardDefault()
            if (!isDefault) {
                binding.tvEnabledStep2.isEnabled = true
                binding.tvEnabledStep2.text = "Click to settings"
                binding.tvEnabledStep2.backgroundTintList = ColorStateList.valueOf(primaryColor)
                binding.tvEnabledStep2.setTextColor(Color.WHITE)
            } else {
                binding.tvEnabledStep2.isEnabled = false
                binding.tvEnabledStep2.text = "Default Keyboard"
                binding.tvEnabledStep2.backgroundTintList = ColorStateList.valueOf(inactiveBg)
                binding.tvEnabledStep2.setTextColor(Color.GRAY)
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("Theme Keyboard has been successfully set up and is ready to use!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
