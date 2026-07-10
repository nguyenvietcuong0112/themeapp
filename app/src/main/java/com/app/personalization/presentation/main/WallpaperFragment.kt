package com.app.personalization.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.presentation.editor.DIYWallpaperActivity
import com.app.personalization.presentation.theme.MyThemeActivity
import com.app.personalization.presentation.theme.PremiumActivity
import com.app.personalization.presentation.theme.ThemePreviewActivity
import com.app.personalization.presentation.widget.GemView
import com.app.personalization.presentation.editor.DownloadWallpaperActivity

class WallpaperFragment : Fragment() {

    private lateinit var viewModel: WallpaperViewModel
    private lateinit var categoryAdapter: WallpaperCategoryAdapter
    private lateinit var itemAdapter: WallpaperItemAdapter
    
    private lateinit var pbCreate: ProgressBar
    private var gemView: GemView? = null

    // Scroll listener for pagination / endless scroll
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            // Trigger when reaching near the end (within 3 items)
            if (totalItemCount <= lastVisibleItem + 3) {
                viewModel.loadWallpapers(true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[WallpaperViewModel::class.java]

        setupToolbar(view)
        setupCategories(view)
        setupWallpaperGrid(view)
        setupFABs(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCoins()
        viewModel.loadWallpapers(false)
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<View>(R.id.toolbar) ?: return
        
        // Hide shuffle refresh, show title
        toolbar.findViewById<View>(R.id.ivRefresh)?.visibility = View.GONE
        
        val titleText = toolbar.findViewById<android.widget.TextView>(R.id.titleTextView)
        titleText?.text = "Wallpapers"

        val llUpgrade = toolbar.findViewById<View>(R.id.llUpgrade)
        llUpgrade?.setOnClickListener {
            startActivity(Intent(context, PremiumActivity::class.java))
        }

        gemView = toolbar.findViewById(R.id.gemView)
    }

    private fun setupCategories(view: View) {
        val rvCategories = view.findViewById<RecyclerView>(R.id.categoryRecyclerView) ?: return
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        categoryAdapter = WallpaperCategoryAdapter { category ->
            viewModel.selectedCategory.value = category
        }
        rvCategories.adapter = categoryAdapter
    }

    private fun setupWallpaperGrid(view: View) {
        val rvWallpapers = view.findViewById<RecyclerView>(R.id.recyclerView) ?: return
        pbCreate = view.findViewById(R.id.pbCreate)
        pbCreate.visibility = View.VISIBLE

        val context = requireContext()
        val columns = viewModel.getNumberOfColumns(context)
        rvWallpapers.layoutManager = GridLayoutManager(context, columns)

        // Calculate final width for adapter dynamic sizing
        val margin = context.resources.getDimensionPixelSize(R.dimen.dp_8) * 2
        val displayWidth = context.resources.displayMetrics.widthPixels
        val availableWidth = displayWidth - margin

        itemAdapter = WallpaperItemAdapter(
            screenWidth = availableWidth,
            columns = columns,
            onItemClick = { wallpaper ->
                val intent = Intent(context, DownloadWallpaperActivity::class.java).apply {
                    putExtra("wallpaper_item", wallpaper)
                }
                startActivity(intent)
            },
            onFavoriteClick = { wallpaper ->
                viewModel.favoriteWallpaper(wallpaper)
                Toast.makeText(context, "Favorite updated!", Toast.LENGTH_SHORT).show()
            }
        )

        rvWallpapers.adapter = itemAdapter
        rvWallpapers.addOnScrollListener(scrollListener)
    }

    private fun setupFABs(view: View) {
        // tvAdd -> Opens MyThemeActivity
        view.findViewById<View>(R.id.tvAdd)?.setOnClickListener {
            startActivity(Intent(context, MyThemeActivity::class.java))
        }

        // tvCreate -> Opens DIY Wallpaper Creator (DIYWallpaperActivity)
        view.findViewById<View>(R.id.tvCreate)?.setOnClickListener {
            startActivity(Intent(context, DIYWallpaperActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            val selected = viewModel.selectedCategory.value ?: return@observe
            categoryAdapter.submitList(list, selected)
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { selected ->
            val list = viewModel.categories.value ?: return@observe
            categoryAdapter.submitList(list, selected)
            viewModel.loadWallpapers(false)
        }

        viewModel.wallpapers.observe(viewLifecycleOwner) { list ->
            pbCreate.visibility = View.GONE
            itemAdapter.submitList(list)
        }

        viewModel.coins.observe(viewLifecycleOwner) { amount ->
            gemView?.setCoins(amount)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment()
    }
}
