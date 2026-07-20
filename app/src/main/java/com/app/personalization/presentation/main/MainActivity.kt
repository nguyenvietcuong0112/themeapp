package com.app.personalization.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R
import com.app.personalization.databinding.ActivityMainBinding
import com.app.personalization.presentation.main.MainPagerAdapter

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

        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.isUserInputEnabled = false // disable swipe to change tab
        binding.viewpager.offscreenPageLimit = 1

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    0 -> R.id.theme
                    1 -> R.id.wallpaper
                    2 -> R.id.icon
                    3 -> R.id.shop
                    else -> R.id.sett_ng_res_0x7f0b0498
                }
                if (binding.bottomNavigationView.selectedItemId != itemId) {
                    binding.bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.itemIconTintList = null
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.theme -> 0
                R.id.wallpaper -> 1
                R.id.icon -> 2
                R.id.shop -> 3
                R.id.sett_ng_res_0x7f0b0498 -> 4
                else -> 0
            }
            if (binding.viewpager.currentItem != position) {
                binding.viewpager.setCurrentItem(position, false)
            }
            true
        }
    }
}
