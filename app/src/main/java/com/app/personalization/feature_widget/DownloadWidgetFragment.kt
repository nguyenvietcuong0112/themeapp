package com.app.personalization.feature_widget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.core.data.ResourceConfig
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentDownloadWidgetBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        lifecycleScope.launch {
            val mappedFolder = withContext(Dispatchers.IO) {
                try {
                    val uuid = java.util.UUID.fromString(theme.id)
                    val diyWidget = com.app.personalization.feature_theme.data.ThemeDatabase.getDatabase(requireContext()).widgetDao().getWidgetsByTheme(uuid)
                    if (diyWidget.isNotEmpty()) {
                        diyWidget[0].templatePath
                    } else {
                        ResourceConfig.getThemeFolderByPath(requireContext(), theme.path)
                    }
                } catch (e: Exception) {
                    ResourceConfig.getThemeFolderByPath(requireContext(), theme.path)
                }
            }

            initWidgetItems(mappedFolder)
            setupRecyclerView(mappedFolder)
            setupActions()
        }
    }

    private fun initWidgetItems(mappedFolder: String) {
        widgetItems.clear()

        // 1. Weather Widget 4x2 (weather)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_weather_4x2",
                name = "Weather Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/weather/bg_preview_medium.png",
                isSelected = true
            )
        )

        // 2. Clock Widget 2x2 (clocks)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_clocks_2x2",
                name = "Clock Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/clocks/bg_preview_large.png",
                isSelected = false
            )
        )

        // 3. Calendar Widget 2x2 (today)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_today_2x2",
                name = "Calendar Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/today/bg_preview_large.png",
                isSelected = false
            )
        )

        // 4. Image Widget 2x2 (image)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_image_2x2",
                name = "Image Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/image/bg_preview_large.png",
                isSelected = false
            )
        )

        // 5. Clock Widget 4x2 (clocks)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_clocks_4x2",
                name = "Clock Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/clocks/bg_preview_medium.png",
                isSelected = false
            )
        )

        // 6. Calendar Widget 4x2 (today)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_today_4x2",
                name = "Calendar Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = "${ResourceConfig.ASSET_BASE_URL}/assets_theme/$mappedFolder/widgets/today/bg_preview_medium.png",
                isSelected = false
            )
        )
    }

    private fun setupRecyclerView(mappedFolder: String) {
        binding.pbCreate.visibility = View.GONE
        val context = requireContext()
        val spacing = (12 * resources.displayMetrics.density).toInt()

        // 2-column grid layout
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = widgetItems.getOrNull(position)
                return if (item?.size == "4x2") 2 else 1
            }
        }
        binding.recyclerView.layoutManager = layoutManager

        while (binding.recyclerView.itemDecorationCount > 0) {
            binding.recyclerView.removeItemDecorationAt(0)
        }

        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position < 0) return
                val item = widgetItems.getOrNull(position)
                val isFullWidth = item?.size == "4x2"

                outRect.bottom = spacing

                if (isFullWidth) {
                    outRect.left = 0
                    outRect.right = 0
                } else {
                    val lp = view.layoutParams as? GridLayoutManager.LayoutParams
                    val spanIndex = lp?.spanIndex ?: (position % 2)
                    if (spanIndex == 0) {
                        outRect.left = 0
                        outRect.right = spacing / 2
                    } else {
                        outRect.left = spacing / 2
                        outRect.right = 0
                    }
                }
            }
        })

        adapter = DownloadWidgetItemAdapter(widgetItems, mappedFolder) { index ->
            selectedIndex = index
            for (i in widgetItems.indices) {
                widgetItems[i].isSelected = (i == index)
            }
            adapter.notifyDataSetChanged()
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupActions() {
        binding.btnAction.setOnClickListener {
            installSelectedWidget()
        }
    }

    private fun installSelectedWidget() {
        val sheet = SelectWidgetBottomSheet()
        sheet.setParams(
            theme = theme,
            widgetList = widgetItems,
            initialIndex = selectedIndex
        )
        sheet.show(childFragmentManager, "select_widget")
    }

    private inner class DownloadWidgetItemAdapter(
        private val list: List<ThemeWidgetItem>,
        private val mappedFolder: String,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<DownloadWidgetItemAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: com.app.personalization.databinding.ItemDownloadWidgetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.app.personalization.databinding.ItemDownloadWidgetLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            val binding = holder.binding

            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt() // 16dp each side
            val spacing = (12 * density).toInt()

            val parentWidth = (displayMetrics.widthPixels - horizontalPadding)

            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT

            if (item.size == "4x2") {
                // Wide: 2 spans, image ratio is 818:395
                val itemHeight = (parentWidth * 395 / 818)
                lp.height = itemHeight
            } else {
                // Square: 1 span, 1:1 ratio
                val itemWidth = (parentWidth - spacing) / 2
                lp.height = itemWidth
            }
            holder.itemView.layoutParams = lp

            // Load widget preview
            val cdnUrl = item.previewUrl
            binding.ivPreview.scaleType = ImageView.ScaleType.FIT_XY

            Glide.with(context)
                .load(cdnUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    Glide.with(context)
                        .load(ResourceConfig.getWidgetPreviewUrl(mappedFolder, item.size))
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
