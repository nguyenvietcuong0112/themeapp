package com.themes.diy.widgets.keyboard.controlcenter.feature_main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.databinding.ActivityMainBinding
import com.themes.diy.widgets.keyboard.controlcenter.feature_setting.InfoActivity

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeAndHideSystemNav()

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

    private fun setupEdgeToEdgeAndHideSystemNav() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        ViewCompat.setOnApplyWindowInsetsListener(binding.cardBottomNav) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val baseMarginBottom = (16 * resources.displayMetrics.density).toInt()
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navBarInset + baseMarginBottom
            }
            insets
        }
    }

    private fun setupHeader() {
        // Premium button click
        binding.btnPremium.setOnClickListener {
            Toast.makeText(this, "Premium Features", Toast.LENGTH_SHORT).show()
        }

        // Setting button click -> Setting screen
        binding.btnSetting.setOnClickListener {
            startActivity(Intent(this, com.themes.diy.widgets.keyboard.controlcenter.feature_setting.SettingActivity::class.java))
        }
    }

    private fun updateHeaderTitle(position: Int) {
        val title = when (position) {
            0 -> "Theme App"
            1 -> "Icons App"
            2 -> "Widgets"
            else -> "Control Center"
        }
        binding.tvHeaderTitle.text = title
        if (position != 0) {
            binding.tvHeaderSubtitle.visibility = View.GONE
        } else {
            binding.tvHeaderSubtitle.visibility = View.VISIBLE
            binding.tvHeaderSubtitle.text = "Make your phone unique"
        }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.isUserInputEnabled = false // disable swipe to change tab
        binding.viewpager.offscreenPageLimit = 1

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateHeaderTitle(position)
                updateCustomNavSelection(position)
            }
        })
    }

    private fun setupBottomNavigation() {
        val items = listOf(
            binding.navItemTheme to 0,
            binding.navItemIcons to 1,
            binding.navItemWidget to 2,
            binding.navItemControl to 3
        )

        for ((view, position) in items) {
            view.setOnClickListener {
                if (binding.viewpager.currentItem != position) {
                    binding.viewpager.setCurrentItem(position, false)
                }
                updateHeaderTitle(position)
                updateCustomNavSelection(position)
            }
        }
        updateCustomNavSelection(0)
    }

    private fun updateCustomNavSelection(position: Int) {
        val activeColor = ContextCompat.getColor(this, R.color.brand_primary)
        val inactiveColor = Color.parseColor("#8E8E93")

        val containers = listOf(
            binding.navItemTheme,
            binding.navItemIcons,
            binding.navItemWidget,
            binding.navItemControl
        )

        val icons = listOf(
            binding.ivNavTheme,
            binding.ivNavIcons,
            binding.ivNavWidget,
            binding.ivNavControl
        )

        val texts = listOf(
            binding.tvNavTheme,
            binding.tvNavIcons,
            binding.tvNavWidget,
            binding.tvNavControl
        )

        for (i in icons.indices) {
            val isActive = (i == position)
            val color = if (isActive) activeColor else inactiveColor

            if (isActive) {
                containers[i].setBackgroundResource(R.drawable.bg_nav_active_item)
            } else {
                containers[i].background = null
            }

            icons[i].setColorFilter(color)
            texts[i].setTextColor(color)
            texts[i].typeface = if (isActive) {
                ResourcesCompat.getFont(this, R.font.inter_bold)
            } else {
                ResourcesCompat.getFont(this, R.font.inter_medium)
            }
        }
    }
}
