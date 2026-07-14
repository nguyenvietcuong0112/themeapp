package com.app.personalization.presentation.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R

class MyThemeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tvMine: TextView
    private lateinit var tvFavorite: TextView
    private lateinit var tvCustomize: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_theme)

        val toolbar = findViewById<View>(R.id.toolbar)
        toolbar?.findViewById<View>(R.id.ivBack)?.setOnClickListener {
            finish()
        }

        tvMine = findViewById(R.id.tvMine)
        tvFavorite = findViewById(R.id.tvFavorite)
        tvCustomize = findViewById(R.id.tvCustomize)
        viewPager = findViewById(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3
            override fun createFragment(position: Int): Fragment {
                return MyThemeListFragment()
            }
        }

        setupTabClicks()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        })

        findViewById<View>(R.id.tvCreate)?.setOnClickListener {
            startActivity(Intent(this, CreateThemeActivity::class.java))
        }
    }

    private fun setupTabClicks() {
        tvMine.setOnClickListener {
            viewPager.currentItem = 0
        }
        tvFavorite.setOnClickListener {
            viewPager.currentItem = 1
        }
        tvCustomize.setOnClickListener {
            viewPager.currentItem = 2
        }
    }

    private fun updateTabSelection(position: Int) {
        val selectedColor = Color.parseColor("#12121A")
        val unselectedColor = Color.parseColor("#FFFFFF")
        
        tvMine.setTextColor(if (position == 0) selectedColor else unselectedColor)
        tvMine.backgroundTintList = android.content.res.ColorStateList.valueOf(if (position == 0) Color.parseColor("#00E5FF") else Color.TRANSPARENT)

        tvFavorite.setTextColor(if (position == 1) selectedColor else unselectedColor)
        tvFavorite.backgroundTintList = android.content.res.ColorStateList.valueOf(if (position == 1) Color.parseColor("#00E5FF") else Color.TRANSPARENT)

        tvCustomize.setTextColor(if (position == 2) selectedColor else unselectedColor)
        tvCustomize.backgroundTintList = android.content.res.ColorStateList.valueOf(if (position == 2) Color.parseColor("#00E5FF") else Color.TRANSPARENT)
    }
}
