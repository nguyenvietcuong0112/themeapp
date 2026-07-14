package com.app.personalization.presentation.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentDownloadWidgetBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class DownloadWidgetFragment : Fragment() {

    private var _binding: FragmentDownloadWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DownloadThemeViewModel
    private lateinit var theme: KeyboardTheme

    private val widgetItems = mutableListOf<ThemeWidgetItem>()
    private lateinit var adapter: DownloadWidgetItemAdapter
    
    private var selectedIndex = 0

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadWidgetFragment {
            return DownloadWidgetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(DownloadThemeViewModel::class.java)
        theme = arguments?.getSerializable("theme") as? KeyboardTheme
            ?: throw IllegalArgumentException("Theme required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbCreate.visibility = View.VISIBLE

        initWidgetItems()
        setupRecyclerView()
        setupActions()
    }

    private fun initWidgetItems() {
        widgetItems.clear()
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_2x2",
                name = "Clock Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetPreviewUrl(requireContext(), theme.path),
                isSelected = true
            )
        )
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_4x2",
                name = "Weather Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetPreviewUrl(requireContext(), theme.path),
                isSelected = false
            )
        )
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_4x4",
                name = "Calendar Widget 4x4",
                size = "4x4",
                providerClass = Widget4x4Provider::class.java,
                previewUrl = ResourceConfig.getWidgetPreviewUrl(requireContext(), theme.path),
                isSelected = false
            )
        )
    }

    private fun setupRecyclerView() {
        binding.pbCreate.visibility = View.GONE
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columns = (screenWidthDp / 180).toInt().coerceAtLeast(2)
        binding.recyclerView.layoutManager = GridLayoutManager(context, columns)
        
        adapter = DownloadWidgetItemAdapter(widgetItems) { index ->
            selectedIndex = index
            for (i in widgetItems.indices) {
                widgetItems[i].isSelected = (i == index)
            }
            adapter.notifyDataSetChanged()
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupActions() {


        // Always show install button
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = "Add Widget"

        binding.actionView.clInstall.setOnClickListener {
            installSelectedWidget()
        }
    }

    private fun installSelectedWidget() {
        val selectedItem = widgetItems.getOrNull(selectedIndex) ?: return
        val widgetType = when (selectedIndex) {
            0 -> "clock"
            1 -> "weather"
            2 -> "calendar"
            else -> "clock"
        }
        val sheet = SelectWidgetBottomSheet()
        sheet.setParams(theme, widgetType, selectedItem.size)
        sheet.show(childFragmentManager, "select_widget")
    }

    private inner class DownloadWidgetItemAdapter(
        private val list: List<ThemeWidgetItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<DownloadWidgetItemAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: com.app.personalization.databinding.ItemDownloadWidgetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.app.personalization.databinding.ItemDownloadWidgetLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            
            // Adjust card height based on screen configuration dynamically
            val displayMetrics = parent.context.resources.displayMetrics
            val itemWidth = displayMetrics.widthPixels / 2 - 24
            binding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (itemWidth * 1.2).toInt()
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val binding = holder.binding

            // Load widget preview (fallback to local asset preview if theme is preset)
            val cdnUrl = com.app.personalization.data.ResourceConfig.getWidgetPreviewUrl(context, theme.path)
            val localFallbackPath = "file:///android_asset/theme_decorates/${theme.path}/popup_background.png"
            Glide.with(context)
                .load(cdnUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    Glide.with(context)
                        .load(localFallbackPath)
                        .placeholder(R.drawable.bg_default_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.bg_default_placeholder)
                )
                .into(binding.ivPreview)

            val ivChecked = binding.root.findViewWithTag<ImageView>("binding_1")
            if (ivChecked != null) {
                if (item.isSelected) {
                    ivChecked.visibility = View.VISIBLE
                    ivChecked.setImageResource(R.drawable.ic_radio_checked)
                } else {
                    ivChecked.visibility = View.GONE
                }
            }

            binding.llContainer.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount(): Int = list.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
