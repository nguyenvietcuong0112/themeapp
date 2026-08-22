package com.themes.diy.widgets.keyboard.controlcenter.feature_theme

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper.diy.DIYWallpaperActivity
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.creator.CreateThemeActivity
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.ThemePreviewActivity
import com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.AllKeyboardActivity
import com.themes.diy.widgets.keyboard.controlcenter.core.ui.HomeActionView

class ThemeFragment : Fragment() {

    private lateinit var viewModel: ThemeViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var pbCreate: ProgressBar

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

        setupQuickActions(view)
        setupCategoryList(view)
        setupThemeGrid(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Reload categories and themes to capture any new DIY themes
        viewModel.selectCategory(viewModel.selectedCategoryId)
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
