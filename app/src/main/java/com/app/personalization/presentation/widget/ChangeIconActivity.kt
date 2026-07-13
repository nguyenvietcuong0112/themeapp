package com.app.personalization.presentation.widget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangeIconActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPkg = intent.getStringExtra("target_package") ?: ""
        if (targetPkg.isNotEmpty()) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(targetPkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this, "Target application not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        finish()
    }
}
