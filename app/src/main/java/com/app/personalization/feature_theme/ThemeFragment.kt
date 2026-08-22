package com.app.personalization.feature_theme

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R
import com.app.personalization.feature_wallpaper.diy.DIYWallpaperActivity
import com.app.personalization.feature_main.MainActivity
import com.app.personalization.feature_theme.my_theme.MyThemeActivity
import com.app.personalization.feature_theme.creator.CreateThemeActivity
import com.app.personalization.feature_theme.ThemePreviewActivity
import com.app.personalization.feature_widget.WidgetConfigActivity
import com.app.personalization.feature_keyboard.AllKeyboardActivity
import com.app.personalization.core.ui.HomeActionView
import kotlin.math.abs

class ThemeFragment : Fragment() {

    private lateinit var viewModel: ThemeViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var pbCreate: ProgressBar
    private var vpBanner: ViewPager2? = null

    private val bannerHandler = Handler(Looper.getMainLooper())
    private val bannerRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null || !isVisible) return
            vpBanner?.let { vp ->
                val nextItem = vp.currentItem + 1
                vp.setCurrentItem(nextItem, true)
                bannerHandler.postDelayed(this, 3500)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setupBannerCarousel(view)
        setupQuickActions(view)
        setupCategoryList(view)
        setupThemeGrid(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        startBannerAutoScroll()
        // Reload categories and themes to capture any new DIY themes
        viewModel.selectCategory(viewModel.selectedCategoryId)
    }

    override fun onPause() {
        super.onPause()
        stopBannerAutoScroll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBannerAutoScroll()
        vpBanner = null
    }

    private fun setupBannerCarousel(view: View) {
        val viewPager = view.findViewById<ViewPager2>(R.id.vpBanner) ?: return
        vpBanner = viewPager

        val banners = listOf(
            ThemeBannerAdapter.TYPE_BANNER_1,
            ThemeBannerAdapter.TYPE_BANNER_2,
            ThemeBannerAdapter.TYPE_BANNER_3
        )

        val bannerAdapter = ThemeBannerAdapter(banners)
        viewPager.adapter = bannerAdapter
        viewPager.offscreenPageLimit = 3

        val childRv = viewPager.getChildAt(0) as? RecyclerView
        childRv?.apply {
            clipToPadding = false
            clipChildren = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val compositeTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(resources.getDimensionPixelOffset(R.dimen.dp_8)))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.90f + r * 0.10f
                page.alpha = 0.85f + r * 0.15f
            }
        }
        viewPager.setPageTransformer(compositeTransformer)

        val initialPosition = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % banners.size)
        viewPager.setCurrentItem(initialPosition, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopBannerAutoScroll()
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startBannerAutoScroll()
                }
            }
        })
    }

    private fun startBannerAutoScroll() {
        stopBannerAutoScroll()
        bannerHandler.postDelayed(bannerRunnable, 3500)
    }

    private fun stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    private fun setupQuickActions(view: View) {
        val actionView = view.findViewById<HomeActionView>(R.id.actionView) ?: return
        actionView.setListener(object : HomeActionView.OnHomeActionViewListener {
            override fun onSelect(action: HomeActionView.HomeActionType) {
                when (action) {
                    HomeActionView.HomeActionType.WALLPAPER -> {
                        startActivity(Intent(context, DIYWallpaperActivity::class.java))
                    }
                    HomeActionView.HomeActionType.THEME -> {
                        startActivity(Intent(context, CreateThemeActivity::class.java))
                    }
                    HomeActionView.HomeActionType.WIDGET -> {
                        startActivity(Intent(context, WidgetConfigActivity::class.java))
                    }
                    HomeActionView.HomeActionType.KEYBOARD -> {
                        startActivity(Intent(context, AllKeyboardActivity::class.java))
                    }
                }
            }
        })
    }

    private fun setupCategoryList(view: View) {
        val rvCategories = view.findViewById<RecyclerView>(R.id.categoryRecyclerView) ?: return
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        categoryAdapter = ThemeCategoryAdapter { categoryTag ->
            viewModel.selectCategory(categoryTag.id)
        }
        rvCategories.adapter = categoryAdapter
    }

    private fun setupThemeGrid(view: View) {
        val rvThemes = view.findViewById<RecyclerView>(R.id.recyclerView) ?: return
        rvThemes.layoutManager = GridLayoutManager(context, 3)

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        while (rvThemes.itemDecorationCount > 0) {
            rvThemes.removeItemDecorationAt(0)
        }
        rvThemes.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        
        pbCreate = view.findViewById(R.id.pbCreate)
        pbCreate.visibility = View.VISIBLE

        themeAdapter = ThemeAdapter(
            onThemeClick = { theme ->
                val intent = Intent(context, ThemePreviewActivity::class.java).apply {
                    putExtra("theme_id", theme.id)
                    putExtra("theme_name", theme.name)
                    putExtra("theme_path", theme.path)
                    putExtra("theme_type", theme.rawType)
                }
                startActivity(intent)
            }
        )
        rvThemes.adapter = themeAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.themes.observe(viewLifecycleOwner) { themes ->
            pbCreate.visibility = View.GONE
            themeAdapter.submitList(themes)
        }
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacingHorizontal: Int,
        private val spacingVertical: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position < 0) return
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacingHorizontal - column * spacingHorizontal / spanCount
                outRect.right = (column + 1) * spacingHorizontal / spanCount
                if (position < spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            } else {
                outRect.left = column * spacingHorizontal / spanCount
                outRect.right = spacingHorizontal - (column + 1) * spacingHorizontal / spanCount
                if (position >= spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ThemeFragment()
    }
}
