package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.databinding.FragmentChangeCreateThemeWallpaperBinding
import com.app.personalization.di.ServiceLocator
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeWallpaperBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentChangeCreateThemeWallpaperBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: CreateThemeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeCreateThemeWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[CreateThemeViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Select Wallpaper"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Hide category recycler
        binding.categoryRecyclerView.visibility = View.GONE

        // Setup Wallpaper Grid (3 columns)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        loadWallpapers()
    }

    private fun loadWallpapers() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbList = com.app.personalization.data.database.ThemeDatabase.getDatabase(requireContext()).wallpaperDao().getAllWallpapers()
            
            // Fallback list of 15 default wallpapers if DB is empty
            val wallpaperUrls = if (dbList.isNotEmpty()) {
                dbList.map { com.app.personalization.data.CdnPathResolver.getWallpaperFullUrl(it.folder, it.imageName) }
            } else {
                (1..15).map { com.app.personalization.data.CdnPathResolver.getWallpaperFullUrl("theme_$it", "bg_wallpaper") }
            }

            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                binding.recyclerView.adapter = WallpaperAdapter(wallpaperUrls) { selectedUrl ->
                    sharedViewModel.selectWallpaper(selectedUrl)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter
    private class WallpaperAdapter(
        private val urls: List<String>,
        private val onSelected: (String) -> Unit
    ) : RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_wallpaper_item_layout,
                parent,
                false
            )
            // Fix height/width aspect ratio
            val density = parent.context.resources.displayMetrics.density
            val screenWidth = parent.context.resources.displayMetrics.widthPixels
            val itemWidth = (screenWidth - (32 * density).toInt()) / 3
            val itemHeight = (itemWidth * 16) / 9
            view.layoutParams = ViewGroup.LayoutParams(itemWidth, itemHeight)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val url = urls[position]
            holder.bind(url, onSelected)
        }

        override fun getItemCount(): Int = urls.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val ivFavorite: ImageView? = view.findViewById(R.id.ivFavorite)

            fun bind(url: String, onSelected: (String) -> Unit) {
                ivFavorite?.visibility = View.GONE
                Glide.with(itemView.context)
                    .load(url)
                    .centerCrop()
                    .into(ivPreview)

                itemView.setOnClickListener {
                    onSelected(url)
                }
            }
        }
    }
}
