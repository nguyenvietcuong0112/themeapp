package com.app.personalization.presentation.theme

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.R

class PremiumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_premium_dialog)

        findViewById<View>(R.id.ivClose)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.layoutPremiumFlexApp)?.setOnClickListener {
            activatePremiumTesting()
        }

        findViewById<View>(R.id.scrollView)?.setOnClickListener {
            activatePremiumTesting()
        }
    }

    private fun activatePremiumTesting() {
        val prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_coins", 9999).apply()
        Toast.makeText(this, "Premium Features Unlocked (9999 Gems added)!", Toast.LENGTH_LONG).show()
        finish()
    }
}
