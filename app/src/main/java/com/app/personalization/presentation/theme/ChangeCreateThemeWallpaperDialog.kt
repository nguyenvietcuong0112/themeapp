package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeWallpaper
import com.app.personalization.databinding.FragmentChangeCreateThemeWallpaperBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangeCreateThemeWallpaperDialog : BottomSheetDialogFragment() {

    interface OnChangeAppListener {
        fun onSelect(wallpaper: WidgetThemeWallpaper)
    }

    private var _binding: FragmentChangeCreateThemeWallpaperBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WallpaperViewModel
    private var listener: OnChangeAppListener? = null

    fun setOnChangeAppListener(listener: OnChangeAppListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

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
        viewModel = ViewModelProvider(this)[WallpaperViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Wallpapers"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Setup Category list (horizontal)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.visibility = View.VISIBLE

        // Setup Grid list (3 columns cuon doc)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        observeViewModel()
        viewModel.loadWallpapers()
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            val selected = viewModel.selectedCategory.value ?: "All"
            binding.categoryRecyclerView.adapter = CreateThemeCategoryAdapter(cats, selected) { cat ->
                viewModel.filterWallpapers(cat)
            }
        }

        viewModel.wallpapers.observe(viewLifecycleOwner) { list ->
            binding.pbCreate.visibility = View.GONE
            binding.recyclerView.adapter = WallpaperItemAdapter(list) { item ->
                listener?.onSelect(item)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Item Adapter
    private class WallpaperItemAdapter(
        private val list: List<WidgetThemeWallpaper>,
        private val onSelected: (WidgetThemeWallpaper) -> Unit
    ) : RecyclerView.Adapter<WallpaperItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_wallpaper_item_layout,
                parent,
                false
            )
            val density = parent.context.resources.displayMetrics.density
            val screenWidth = parent.context.resources.displayMetrics.widthPixels
            val itemWidth = (screenWidth - (32 * density).toInt()) / 3
            val itemHeight = (itemWidth * 16) / 9
            view.layoutParams = ViewGroup.LayoutParams(itemWidth, itemHeight)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.bind(item, onSelected)
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val ivFavorite: ImageView? = view.findViewById(R.id.ivFavorite)
            private val cardView: View? = view.findViewById(R.id.cardView)

            fun bind(item: WidgetThemeWallpaper, onSelected: (WidgetThemeWallpaper) -> Unit) {
                ivFavorite?.visibility = View.GONE
                
                val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(itemView.context, item.folder)
                val url = com.app.personalization.data.CdnPathResolver.getWallpaperFullUrl(resolvedFolder, item.imageBg)

                Glide.with(itemView.context)
                    .load(url)
                    .centerCrop()
                    .into(ivPreview)

                val clickTarget = cardView ?: itemView
                clickTarget.setOnClickListener {
                    onSelected(item)
                }
            }
        }
    }
}
