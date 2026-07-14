package com.app.personalization.presentation.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.ViewModelProvider
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.ActivityDownloadThemeBinding

class DownloadThemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadThemeBinding
    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: KeyboardTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(DownloadThemeViewModel::class.java)

        val themeId = intent.getStringExtra("theme_id") ?: ""
        val themeName = intent.getStringExtra("theme_name") ?: "Default Theme"
        val themePath = intent.getStringExtra("theme_path") ?: ""
        val themeType = intent.getStringExtra("theme_type") ?: "default"
        theme = KeyboardTheme(id = themeId, name = themeName, path = themePath, rawType = themeType)

        // Set Tab background color dynamically using ?attr/secondaryBackgroundColor
        binding.llTab.setBackgroundColor(getAttrColor(R.attr.secondaryBackgroundColor))

        initToolbar()
        initViewPager()
        initTabs()
        val startTab = intent.getIntExtra("start_tab", 0)
        binding.viewPager.setCurrentItem(startTab, false)
    }

    private fun getAttrColor(attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        this.getTheme().resolveAttribute(attrId, typedValue, true)
        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
    }

    private fun initToolbar() {
        val toolbarBinding = binding.toolbar
        toolbarBinding.titleTextView.text = theme.name
        
        // Hide adContainer completely as requested
        binding.adContainer.visibility = View.GONE

        toolbarBinding.ivBack.setOnClickListener {
            finish()
        }

        toolbarBinding.ivInfo.setOnClickListener {
            val intent = Intent(this, com.app.personalization.presentation.setting.InfoActivity::class.java)
            startActivity(intent)
        }
        
        toolbarBinding.llCreateWallpaper.root.setOnClickListener {
            val intent = Intent(this, com.app.personalization.presentation.wallpaper.DIYWallpaperActivity::class.java)
            startActivity(intent)
        }

        toolbarBinding.llCreateWidget.root.setOnClickListener {
            val intent = Intent(this, com.app.personalization.presentation.widget.WidgetConfigActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViewPager() {
        binding.viewPager.adapter = DownloadThemePagerAdapter(this, theme)
        // Lock manual swiping
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabUI(position)
                updateToolbarButtons(position)
            }
        })
    }

    private fun initTabs() {
        binding.tvWallpaper.setOnClickListener {
            binding.viewPager.currentItem = 0
        }
        binding.tvIcon.setOnClickListener {
            binding.viewPager.currentItem = 1
        }
        binding.tvWidget.setOnClickListener {
            binding.viewPager.currentItem = 2
        }
    }

    private fun updateTabUI(position: Int) {
        val tabs = listOf(binding.tvWallpaper, binding.tvIcon, binding.tvWidget)
        for (i in tabs.indices) {
            val tab = tabs[i]
            if (i == position) {
                tab.isSelected = true
                tab.setTextColor(Color.WHITE)
                tab.setBackgroundResource(R.drawable.tab_indicator_primary)
                tab.typeface = ResourcesCompat.getFont(this, R.font.helvetica_medium)
            } else {
                tab.isSelected = false
                tab.setTextColor(getAttrColor(R.attr.detailTextColor))
                tab.setBackgroundResource(0)
                tab.typeface = ResourcesCompat.getFont(this, R.font.helvetica_regular)
            }
        }
    }

    private fun updateToolbarButtons(position: Int) {
        val toolbarBinding = binding.toolbar
        when (position) {
            0 -> {
                toolbarBinding.llCreateWallpaper.root.visibility = View.VISIBLE
                toolbarBinding.llCreateWidget.root.visibility = View.GONE
                toolbarBinding.titleTextView.visibility = View.GONE
            }
            1 -> {
                toolbarBinding.llCreateWallpaper.root.visibility = View.GONE
                toolbarBinding.llCreateWidget.root.visibility = View.GONE
                toolbarBinding.titleTextView.visibility = View.VISIBLE
            }
            2 -> {
                toolbarBinding.llCreateWallpaper.root.visibility = View.GONE
                toolbarBinding.llCreateWidget.root.visibility = View.VISIBLE
                toolbarBinding.titleTextView.visibility = View.GONE
            }
        }
    }

    class DownloadThemePagerAdapter(
        activity: AppCompatActivity,
        private val theme: KeyboardTheme
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> DownloadWallpaperFragment.newInstance(theme)
                1 -> DownloadIconFragment.newInstance(theme)
                2 -> DownloadWidgetFragment.newInstance(theme)
                else -> throw IllegalStateException("Invalid position")
            }
        }
    }
}
