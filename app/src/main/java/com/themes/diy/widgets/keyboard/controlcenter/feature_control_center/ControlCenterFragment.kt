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
        pbLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val categories = repository.getCategories()
            allCategories = categories

            val categoryItems = mutableListOf<ControlCategoryItem>()
            categoryItems.add(ControlCategoryItem(slug = "all", name = "All", isSelected = true))
            categories.forEach { cat ->
                categoryItems.add(ControlCategoryItem(slug = cat.slug, name = cat.name, isSelected = false))
            }

            val allThemes = categories.flatMap { it.themes }

            withContext(Dispatchers.Main) {
                pbLoading.visibility = View.GONE
                categoryAdapter.submitList(categoryItems)
                themeAdapter.updateData(allThemes)
            }
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
            allCategories.flatMap { it.themes }
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
