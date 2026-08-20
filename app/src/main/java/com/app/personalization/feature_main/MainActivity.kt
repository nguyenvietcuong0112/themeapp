package com.app.personalization.feature_main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R
import com.app.personalization.databinding.ActivityMainBinding
import com.app.personalization.feature_setting.InfoActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure any ad elements are hidden if present in layouts
        val idsToHide = listOf("bannerView", "bannerUpsale", "adContainer")
        for (idStr in idsToHide) {
            val id = resources.getIdentifier(idStr, "id", packageName)
            if (id != 0) {
                try {
                    binding.root.findViewById<View>(id)?.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        setupHeader()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupHeader() {
        // Premium button click
        binding.btnPremium.setOnClickListener {
            Toast.makeText(this, "Premium Features", Toast.LENGTH_SHORT).show()
        }

        // Setting button click -> Setting screen
        binding.btnSetting.setOnClickListener {
            startActivity(Intent(this, com.app.personalization.feature_setting.SettingActivity::class.java))
        }
    }

    private fun updateHeaderTitle(position: Int) {
        val title = when (position) {
            0 -> getString(R.string.tab_theme)
            1 -> getString(R.string.tab_icons)
            2 -> getString(R.string.tab_control)
            3 -> getString(R.string.tab_widget)
            else -> "Collection"
        }
        binding.tvHeaderTitle.text = title
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.isUserInputEnabled = false // disable swipe to change tab
        binding.viewpager.offscreenPageLimit = 1

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateHeaderTitle(position)
                val itemId = when (position) {
                    0 -> R.id.nav_theme
                    1 -> R.id.nav_icons
                    2 -> R.id.nav_control
                    3 -> R.id.nav_widget
                    else -> R.id.nav_collections
                }
                if (binding.bottomNavigationView.selectedItemId != itemId) {
                    binding.bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.nav_theme -> 0
                R.id.nav_icons -> 1
                R.id.nav_control -> 2
                R.id.nav_widget -> 3
                R.id.nav_collections -> 4
                else -> 0
            }
            if (binding.viewpager.currentItem != position) {
                binding.viewpager.setCurrentItem(position, false)
            }
            updateHeaderTitle(position)
            true
        }
    }
}
