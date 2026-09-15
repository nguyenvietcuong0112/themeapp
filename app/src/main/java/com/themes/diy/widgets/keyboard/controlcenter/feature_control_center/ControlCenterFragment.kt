package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlCenterFragment : Fragment() {

    private lateinit var rvCategoryTabs: RecyclerView
    private lateinit var rvControlThemes: RecyclerView
    private lateinit var pbLoading: ProgressBar

    private lateinit var categoryAdapter: ControlCategoryAdapter
    private lateinit var themeAdapter: ControlCenterAdapter
    private val repository by lazy { ControlCenterRepository(requireContext()) }

    private var allCategories: List<ControlCategory> = emptyList()
    private var selectedCategorySlug: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_control_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCategoryTabs()
        setupThemeGrid()
        loadControlThemes()
    }

    private fun initViews(view: View) {
        rvCategoryTabs = view.findViewById(R.id.rvCategoryTabs)
        rvControlThemes = view.findViewById(R.id.rvControlThemes)
        pbLoading = view.findViewById(R.id.pbLoading)
    }

    private fun setupCategoryTabs() {
        categoryAdapter = ControlCategoryAdapter(emptyList()) { categoryItem ->
            selectCategory(categoryItem.slug)
        }
        rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCategoryTabs.adapter = categoryAdapter
    }

    private fun setupThemeGrid() {
        val columns = 3
        rvControlThemes.layoutManager = GridLayoutManager(requireContext(), columns)

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        while (rvControlThemes.itemDecorationCount > 0) {
            rvControlThemes.removeItemDecorationAt(0)
        }
        rvControlThemes.addItemDecoration(GridSpacingItemDecoration(columns, spacingHorizontal, spacingVertical, false))

        themeAdapter = ControlCenterAdapter(emptyList()) { theme ->
            ControlCenterPreviewActivity.start(requireContext(), theme.folderPath, theme.name)
        }
        rvControlThemes.adapter = themeAdapter
    }

    private fun loadControlThemes() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Fast instant load from memory/disk/local asset (0ms, no blank screen)
            val fastCategories = repository.getCategoriesFast()
            if (fastCategories.isNotEmpty()) {
                allCategories = fastCategories
                val categoryItems = buildCategoryItems(fastCategories)
                val allThemes = fastCategories.flatMap { it.themes }.distinctBy { it.name }

                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    categoryAdapter.submitList(categoryItems)
                    themeAdapter.updateData(allThemes)
                }

                // Preload top 15 thumbnails into Glide cache
                preloadThumbnails(allThemes.take(15))
            } else {
                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.VISIBLE
                }
            }

            // 2. Background sync from CDN (Stale-While-Revalidate)
            val updatedCategories = repository.refreshFromCdn()
            if (updatedCategories != null && updatedCategories.isNotEmpty()) {
                allCategories = updatedCategories
                val categoryItems = buildCategoryItems(updatedCategories)
                val currentThemes = if (selectedCategorySlug == "all") {
                    updatedCategories.flatMap { it.themes }.distinctBy { it.name }
                } else {
                    updatedCategories.firstOrNull { it.slug == selectedCategorySlug }?.themes ?: emptyList()
                }

                withContext(Dispatchers.Main) {
                    pbLoading.visibility = View.GONE
                    categoryAdapter.submitList(categoryItems)
                    themeAdapter.updateData(currentThemes)
                }
            }
        }
    }

    private fun buildCategoryItems(categories: List<ControlCategory>): List<ControlCategoryItem> {
        val categoryItems = mutableListOf<ControlCategoryItem>()
        categoryItems.add(ControlCategoryItem(slug = "all", name = "All", isSelected = (selectedCategorySlug == "all")))
        categories.forEach { cat ->
            categoryItems.add(ControlCategoryItem(slug = cat.slug, name = cat.name, isSelected = (selectedCategorySlug == cat.slug)))
        }
        return categoryItems
    }

    private fun preloadThumbnails(themes: List<ControlTheme>) {
        val ctx = context ?: return
        for (theme in themes) {
            try {
                Glide.with(ctx)
                    .load(theme.thumbPath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload()
            } catch (_: Exception) {}
        }
    }

    private fun selectCategory(slug: String) {
        selectedCategorySlug = slug

        val updatedTabs = mutableListOf<ControlCategoryItem>()
        updatedTabs.add(ControlCategoryItem(slug = "all", name = "All", isSelected = (slug == "all")))
        allCategories.forEach { cat ->
            updatedTabs.add(ControlCategoryItem(slug = cat.slug, name = cat.name, isSelected = (cat.slug == slug)))
        }
        categoryAdapter.submitList(updatedTabs)

        val filteredThemes = if (slug == "all") {
            allCategories.flatMap { it.themes }.distinctBy { it.name }
        } else {
            allCategories.firstOrNull { it.slug == slug }?.themes ?: emptyList()
        }
        themeAdapter.updateData(filteredThemes)
        rvControlThemes.smoothScrollToPosition(0)
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
        fun newInstance() = ControlCenterFragment()
    }
}
