package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.databinding.ActivityThemePreviewBinding
import com.app.personalization.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemePreviewBinding
    private var themeId: String = ""
    private var isFav = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themeId = intent.getStringExtra("theme_id") ?: ""

        // Lock swipe gestures on ViewPager2
        binding.viewPager.isUserInputEnabled = false

        binding.ivClose.setOnClickListener {
            finish()
        }

        // Hide ads container
        binding.adContainer.visibility = View.GONE
        binding.llLoadingAd.visibility = View.GONE
        binding.ctOverlay.visibility = View.GONE

        // Load favorite status
        lifecycleScope.launch(Dispatchers.IO) {
            val folder = if (themeId.startsWith("default_")) {
                val path = themeId.substringAfter("default_")
                ResourceConfig.getThemeFolderByPath(this@ThemePreviewActivity, path)
            } else {
                val theme = ServiceLocator.getThemeDao(this@ThemePreviewActivity).getThemeById(themeId)
                if (theme != null) ResourceConfig.getThemeFolderByPath(this@ThemePreviewActivity, theme.path) else ""
            }

            val widgetTheme = ServiceLocator.getWidgetThemeDao(this@ThemePreviewActivity).getWidgetThemeByFolder(folder)
            isFav = widgetTheme?.isFavorite == true
            
            withContext(Dispatchers.Main) {
                binding.ivFavorite.isSelected = isFav
                binding.pbLoading.visibility = View.GONE
            }
        }

        binding.ivFavorite.setOnClickListener {
            isFav = !isFav
            binding.ivFavorite.isSelected = isFav
            lifecycleScope.launch(Dispatchers.IO) {
                val folder = if (themeId.startsWith("default_")) {
                    val path = themeId.substringAfter("default_")
                    ResourceConfig.getThemeFolderByPath(this@ThemePreviewActivity, path)
                } else {
                    val theme = ServiceLocator.getThemeDao(this@ThemePreviewActivity).getThemeById(themeId)
                    if (theme != null) ResourceConfig.getThemeFolderByPath(this@ThemePreviewActivity, theme.path) else ""
                }
                
                val widgetTheme = ServiceLocator.getWidgetThemeDao(this@ThemePreviewActivity).getWidgetThemeByFolder(folder)
                if (widgetTheme != null) {
                    widgetTheme.isFavorite = isFav
                    ServiceLocator.getWidgetThemeDao(this@ThemePreviewActivity).insertWidgetTheme(widgetTheme)
                }
            }
        }

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1
            override fun createFragment(position: Int): Fragment {
                return ThemePreviewFragment.newInstance(themeId)
            }
        }
    }
}
