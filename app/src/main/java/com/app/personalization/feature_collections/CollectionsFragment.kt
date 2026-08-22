package com.app.personalization.feature_collections

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.feature_collections.adapter.CollectionDiscoveryAdapter
import com.app.personalization.feature_collections.adapter.CollectionTabAdapter

class CollectionsFragment : Fragment() {

    private lateinit var viewModel: CollectionsViewModel
    private lateinit var tabAdapter: CollectionTabAdapter
    private lateinit var discoveryAdapter: CollectionDiscoveryAdapter
    private lateinit var downloadedAdapter: CollectionDiscoveryAdapter

    private lateinit var rvCategoryTabs: RecyclerView
    private lateinit var rvDiscovery: RecyclerView
    private lateinit var rvDownloaded: RecyclerView
    private lateinit var layoutDownloadedEmpty: View
    private lateinit var btnExploreTheme: TextView
    private lateinit var btnSeeMore: View
    private lateinit var pbLoading: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CollectionsViewModel::class.java]

        initViews(view)
        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDataForSelectedTab()
    }

    private fun initViews(view: View) {
        rvCategoryTabs = view.findViewById(R.id.rvCategoryTabs)
        rvDiscovery = view.findViewById(R.id.rvDiscovery)
        rvDownloaded = view.findViewById(R.id.rvDownloaded)
        layoutDownloadedEmpty = view.findViewById(R.id.layoutDownloadedEmpty)
        btnExploreTheme = view.findViewById(R.id.btnExploreTheme)
        btnSeeMore = view.findViewById(R.id.btnSeeMore)
        pbLoading = view.findViewById(R.id.pbLoading)
    }

    private fun setupAdapters() {
        // 1. Category Tabs Adapter (Horizontal)
        tabAdapter = CollectionTabAdapter(emptyList()) { tab ->
            viewModel.selectTab(tab)
        }
        rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCategoryTabs.adapter = tabAdapter

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)

        // 2. Downloaded Items Adapter (Horizontal row or 3-col grid)
        downloadedAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(requireActivity(), item)
        }
        val downloadedLayoutManager = GridLayoutManager(context, 3)
        val downloadedSpanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = downloadedAdapter.getItem(position)
                return if (item?.category.equals("Widget", ignoreCase = true) && item?.extra == "4x2") 2 else 1
            }
        }
        downloadedSpanLookup.isSpanIndexCacheEnabled = false
        downloadedLayoutManager.spanSizeLookup = downloadedSpanLookup
        rvDownloaded.layoutManager = downloadedLayoutManager
        while (rvDownloaded.itemDecorationCount > 0) {
            rvDownloaded.removeItemDecorationAt(0)
        }
        rvDownloaded.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        rvDownloaded.adapter = downloadedAdapter

        // 3. Discovery Grid Adapter (3 Columns with Widget Span Support)
        discoveryAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(requireActivity(), item)
        }
        val discoveryLayoutManager = GridLayoutManager(context, 3)
        val discoverySpanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = discoveryAdapter.getItem(position)
                return if (item?.category.equals("Widget", ignoreCase = true) && item?.extra == "4x2") 2 else 1
            }
        }
        discoverySpanLookup.isSpanIndexCacheEnabled = false
        discoveryLayoutManager.spanSizeLookup = discoverySpanLookup
        rvDiscovery.layoutManager = discoveryLayoutManager
        while (rvDiscovery.itemDecorationCount > 0) {
            rvDiscovery.removeItemDecorationAt(0)
        }
        rvDiscovery.addItemDecoration(GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))
        rvDiscovery.adapter = discoveryAdapter
    }

    private fun setupListeners() {
        btnSeeMore.setOnClickListener {
            val selectedTabName = viewModel.selectedTab.value?.name ?: "Theme"
            val intent = Intent(requireContext(), DownloadedActivity::class.java).apply {
                putExtra(DownloadedActivity.EXTRA_CATEGORY, selectedTabName)
            }
            startActivity(intent)
        }

        btnExploreTheme.setOnClickListener {
            // Jump to the current tab exploration or scroll to discovery
            rvDiscovery.smoothScrollToPosition(0)
        }
    }

    private fun observeViewModel() {
        viewModel.tabs.observe(viewLifecycleOwner) { tabs ->
            tabAdapter.updateTabs(tabs)
        }

        viewModel.selectedTab.observe(viewLifecycleOwner) { tab ->
            btnExploreTheme.text = "Explore ${tab.name}"
        }

        viewModel.downloadedItems.observe(viewLifecycleOwner) { items ->
            if (items.isNullOrEmpty()) {
                rvDownloaded.visibility = View.GONE
                layoutDownloadedEmpty.visibility = View.VISIBLE
            } else {
                rvDownloaded.visibility = View.VISIBLE
                layoutDownloadedEmpty.visibility = View.GONE
                // Show at most 3 items in the preview card
                downloadedAdapter.submitList(items.take(3))
            }
        }

        viewModel.discoveryItems.observe(viewLifecycleOwner) { items ->
            Log.d("CollectionDebug", "Fragment: Received discoveryItems size=${items.size}")
            discoveryAdapter.submitList(items)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
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

            val lp = view.layoutParams as? GridLayoutManager.LayoutParams
            val spanIndex = lp?.spanIndex ?: (position % spanCount)
            val spanSize = lp?.spanSize ?: 1

            if (includeEdge) {
                outRect.left = spacingHorizontal - spanIndex * spacingHorizontal / spanCount
                outRect.right = (spanIndex + spanSize) * spacingHorizontal / spanCount
                if (position < spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            } else {
                outRect.left = spanIndex * spacingHorizontal / spanCount
                outRect.right = spacingHorizontal - (spanIndex + spanSize) * spacingHorizontal / spanCount
                if (position >= spanCount) {
                    outRect.top = spacingVertical
                }
                outRect.bottom = spacingVertical
            }
        }
    }

    companion object {
        fun newInstance() = CollectionsFragment()
    }
}
