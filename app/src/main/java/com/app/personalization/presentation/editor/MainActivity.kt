package com.app.personalization.presentation.editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R
import com.app.personalization.presentation.main.MainPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewpager)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // disable swipe to change tab
        viewPager.offscreenPageLimit = 1

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    0 -> R.id.theme
                    1 -> R.id.wallpaper
                    2 -> R.id.icon
                    3 -> R.id.shop
                    else -> R.id.sett_ng_res_0x7f0b0498
                }
                if (bottomNavigationView.selectedItemId != itemId) {
                    bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setOnItemSelectedListener { item ->
            val position = when (item.itemId) {
                R.id.theme -> 0
                R.id.wallpaper -> 1
                R.id.icon -> 2
                R.id.shop -> 3
                R.id.sett_ng_res_0x7f0b0498 -> 4
                else -> 0
            }
            if (viewPager.currentItem != position) {
                viewPager.setCurrentItem(position, false)
            }
            true
        }
    }
}
