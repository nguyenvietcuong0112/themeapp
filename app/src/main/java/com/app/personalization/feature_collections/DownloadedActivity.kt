package com.app.personalization.feature_collections

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.personalization.R
import com.app.personalization.databinding.ActivityDownloadedBinding
import com.app.personalization.feature_collections.adapter.CollectionDiscoveryAdapter
import com.app.personalization.feature_collections.adapter.CollectionTabAdapter
import com.app.personalization.feature_collections.data.CollectionItem
import com.app.personalization.feature_collections.data.CollectionRepository
import com.app.personalization.feature_collections.data.CollectionTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadedBinding
    private lateinit var repository: CollectionRepository
    private lateinit var tabAdapter: CollectionTabAdapter
    private lateinit var contentAdapter: CollectionDiscoveryAdapter

    private var currentCategory: String = "Theme"
    private var tabs: List<CollectionTab> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = CollectionRepository(this)
        currentCategory = intent.getStringExtra(EXTRA_CATEGORY) ?: "Theme"

        initViews()
        setupAdapters()
        setupListeners()
        initTabs()
        observeDownloadedItems()
    }

    private fun initViews() {
        binding.tvTitle.text = "Downloaded"
    }

    private fun setupAdapters() {
        tabAdapter = CollectionTabAdapter(emptyList()) { selectedTab ->
            selectTab(selectedTab.name)
        }
        binding.rvCategoryTabs.adapter = tabAdapter

        contentAdapter = CollectionDiscoveryAdapter(emptyList()) { item ->
            CollectionNavigator.navigateToDetail(this, item)
        }
        val gridLayoutManager = GridLayoutManager(this, 3)
        val spanLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = contentAdapter.getItem(position)
                return if (item?.category.equals("Widget", ignoreCase = true) && item?.extra == "4x2") 2 else 1
            }
        }
        spanLookup.isSpanIndexCacheEnabled = false
        gridLayoutManager.spanSizeLookup = spanLookup
        binding.rvDownloadedGrid.layoutManager = gridLayoutManager

        val spacingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)
        val spacingVertical = resources.getDimensionPixelSize(R.dimen.dp_8)
        while (binding.rvDownloadedGrid.itemDecorationCount > 0) {
            binding.rvDownloadedGrid.removeItemDecorationAt(0)
        }
        binding.rvDownloadedGrid.addItemDecoration(CollectionsFragment.GridSpacingItemDecoration(3, spacingHorizontal, spacingVertical, false))

        binding.rvDownloadedGrid.adapter = contentAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initTabs() {
        tabs = CollectionRepository.TABS.map { name ->
            CollectionTab(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isSelected = name.equals(currentCategory, ignoreCase = true)
            )
        }
        tabAdapter.updateTabs(tabs)
        updateEmptyStateTitle(currentCategory)
    }

    private fun selectTab(categoryName: String) {
        currentCategory = categoryName
        tabs = tabs.map { it.copy(isSelected = it.name.equals(categoryName, ignoreCase = true)) }
        tabAdapter.updateTabs(tabs)
        updateEmptyStateTitle(categoryName)
        observeDownloadedItems()
    }

    private fun updateEmptyStateTitle(categoryName: String) {
        binding.tvEmptySubtitle.text = "Your downloaded $categoryName items will appear here."
    }

    private fun observeDownloadedItems() {
        lifecycleScope.launch {
            repository.getDownloadedItemsFlow(currentCategory).collectLatest { items ->
                if (items.isEmpty()) {
                    binding.rvDownloadedGrid.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvDownloadedGrid.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    val collectionItems = items.map { entity ->
                        CollectionItem(
                            id = entity.id,
                            name = entity.name,
                            category = entity.category,
                            targetPath = entity.targetPath,
                            previewPath = entity.previewPath,
                            downloads = entity.downloads,
                            rawType = entity.rawType,
                            extra = entity.extra
                        )
                    }
                    contentAdapter.submitList(collectionItems)
                }
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }
}
