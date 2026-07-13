package com.app.personalization.presentation.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.personalization.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.titleTextView.text = "Information"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }

        binding.siFQA.setOnClickListener {
            // FAQ Click action placeholder
        }

        binding.siGetIcon.setOnClickListener {
            // How to get icon guide action placeholder
        }

        binding.siGetWidget.setOnClickListener {
            // How to get widget guide action placeholder
        }
    }
}
