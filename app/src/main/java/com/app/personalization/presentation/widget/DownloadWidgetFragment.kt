package com.app.personalization.presentation.widget

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
import com.app.personalization.data.ResourceConfig
import com.app.personalization.data.database.entity.KeyboardTheme
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
                    val diyWidget = com.app.personalization.data.database.ThemeDatabase.getDatabase(requireContext()).widgetDao().getWidgetsByTheme(uuid)
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

        // 1. Calendar Widget 2x2 (today)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_today_2x2",
                name = "Calendar Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$mappedFolder/widgets/today/bg_preview_medium.png",
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
                previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$mappedFolder/widgets/clocks/bg_preview_medium.png",
                isSelected = false
            )
        )

        // 3. Weather Widget 4x2 (weather)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_weather_4x2",
                name = "Weather Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$mappedFolder/widgets/weather/bg_preview_medium.png",
                isSelected = false
            )
        )

        // 4. Today Widget 2x2 (today)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_today2_2x2",
                name = "Today Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$mappedFolder/widgets/today/bg_preview_medium.png",
                isSelected = false
            )
        )

        // 5. Image Widget 2x2 (image)
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_image_2x2",
                name = "Image Widget 2x2",
                size = "2x2",
                providerClass = Widget2x2Provider::class.java,
                previewUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$mappedFolder/widgets/image/bg_preview_medium.png",
                isSelected = false
            )
        )
    }

    private fun setupRecyclerView(mappedFolder: String) {
        binding.pbCreate.visibility = View.GONE
        val context = requireContext()
        
        // 2-column grid layout matching the screenshot design
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = widgetItems.getOrNull(position)
                // 4x2 widgets take full width (2 spans), others take 1 span
                return if (item?.size == "4x2") 2 else 1
            }
        }
        
        binding.recyclerView.layoutManager = layoutManager
        
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
        binding.actionView.clInstall.visibility = View.VISIBLE
        binding.actionView.tvInstall.text = "Add Widget"

        binding.actionView.clInstall.setOnClickListener {
            installSelectedWidget()
        }
    }

    private fun installSelectedWidget() {
        val selectedItem = widgetItems.getOrNull(selectedIndex) ?: return
        val typeId = selectedItem.id.substringAfter("_widget_").substringBefore("_2x2").substringBefore("_4x2")
        val widgetType = when (typeId) {
            "today", "today2" -> "calendar"
            "clocks" -> "clock"
            "weather" -> "weather"
            "image" -> "image"
            else -> "clock"
        }
        val sheet = SelectWidgetBottomSheet()
        sheet.setParams(theme, widgetType, selectedItem.size, selectedItem.previewUrl)
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

            // Set layout params dynamically based on size to fit the image perfectly
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val density = displayMetrics.density
            val horizontalPadding = (32 * density).toInt()

            val margin = (8 * density).toInt()
            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Set margins
            lp.setMargins(margin, margin, margin, margin)

            if (item.size == "4x2") {
                // Wide: full width minus paddings and margins
                val itemWidth = screenWidth - horizontalPadding - (margin * 2)
                val itemHeight = (itemWidth * 9) / 16
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = itemHeight
            } else {
                // Square: half width minus paddings and margins
                val itemWidth = (screenWidth - horizontalPadding - (margin * 4)) / 2
                val itemHeight = itemWidth
                lp.width = itemWidth
                lp.height = itemHeight
            }
            holder.itemView.layoutParams = lp

            // Load widget preview
            val cdnUrl = item.previewUrl

            binding.ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(context)
                .load(cdnUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    // Reverted back to original simple format fallback
                    Glide.with(context)
                        .load(com.app.personalization.data.CdnPathResolver.getWidgetPreviewUrl(mappedFolder, item.size))
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
