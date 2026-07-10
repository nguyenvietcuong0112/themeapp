package com.app.personalization.presentation.theme

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.ResourceConfig
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentDownloadWidgetBinding
import com.app.personalization.presentation.widget.Widget2x2Provider
import com.app.personalization.presentation.widget.Widget4x2Provider
import com.app.personalization.presentation.widget.Widget4x4Provider
import com.bumptech.glide.Glide
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
                previewUrl = ResourceConfig.getWidgetPreviewUrl(theme.path),
                isSelected = true
            )
        )
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_4x2",
                name = "Weather Widget 4x2",
                size = "4x2",
                providerClass = Widget4x2Provider::class.java,
                previewUrl = ResourceConfig.getWidgetPreviewUrl(theme.path),
                isSelected = false
            )
        )
        widgetItems.add(
            ThemeWidgetItem(
                id = "${theme.id}_widget_4x4",
                name = "Calendar Widget 4x4",
                size = "4x4",
                providerClass = Widget4x4Provider::class.java,
                previewUrl = ResourceConfig.getWidgetPreviewUrl(theme.path),
                isSelected = false
            )
        )
    }

    private fun setupRecyclerView() {
        binding.pbCreate.visibility = View.GONE
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        
        adapter = DownloadWidgetItemAdapter(widgetItems) { index ->
            selectedIndex = index
            for (i in widgetItems.indices) {
                widgetItems[i].isSelected = (i == index)
            }
            adapter.notifyDataSetChanged()
            updateLockUnlockState()
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupActions() {
        // Ads logic disabled
        binding.actionView.llPlayVideo.root.visibility = View.GONE
        binding.actionView.llUnlockWithAds.visibility = View.GONE

        // Cost setup
        binding.actionView.llAddCoin.visibility = View.VISIBLE
        binding.actionView.tvCoin.text = "100"

        updateLockUnlockState()

        binding.actionView.llAddCoin.setOnClickListener {
            unlockSelectedWidget()
        }

        binding.actionView.llGetAll.setOnClickListener {
            Toast.makeText(context, "Unlock premium from store to get all!", Toast.LENGTH_SHORT).show()
        }

        binding.actionView.clInstall.setOnClickListener {
            installSelectedWidget()
        }
    }

    private fun updateLockUnlockState() {
        val selectedItem = widgetItems.getOrNull(selectedIndex) ?: return
        
        // Custom check if this widget is unlocked (free widgets or unlock state from viewmodel)
        val isUnlocked = viewModel.isWidgetUnlocked(selectedItem.id, theme.isPremium.not())
        
        if (isUnlocked) {
            binding.actionView.llAction.visibility = View.GONE
            binding.actionView.clInstall.visibility = View.VISIBLE
        } else {
            binding.actionView.llAction.visibility = View.VISIBLE
            binding.actionView.clInstall.visibility = View.GONE
        }
    }

    private fun unlockSelectedWidget() {
        val selectedItem = widgetItems.getOrNull(selectedIndex) ?: return
        if (viewModel.deductCoins(100)) {
            viewModel.unlockWidget(selectedItem.id)
            updateLockUnlockState()
            Toast.makeText(context, "${selectedItem.name} unlocked successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Not enough coins! Please earn or buy more.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installSelectedWidget() {
        val selectedItem = widgetItems.getOrNull(selectedIndex) ?: return
        val context = requireContext()
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val myProvider = ComponentName(context, selectedItem.providerClass)
                
                // Fire system widget pin intent request
                val pinned = appWidgetManager.requestPinAppWidget(myProvider, null, null)
                if (pinned) {
                    Toast.makeText(context, "Requesting launcher to pin ${selectedItem.name}...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to initiate widget pinning", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Pinning widget is not supported by your launcher", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Widget pinning is supported on Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class DownloadWidgetItemAdapter(
        private val list: List<ThemeWidgetItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<DownloadWidgetItemAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val ivChecked: ImageView = view.findViewWithTag("binding_1")
            val llContainer: View = view.findViewById(R.id.llContainer)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_download_widget_layout, parent, false)
            
            // Adjust card height based on screen configuration dynamically
            val displayMetrics = parent.context.resources.displayMetrics
            val itemWidth = displayMetrics.widthPixels / 2 - 24
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (itemWidth * 1.2).toInt()
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context

            // Load widget preview (fallback to local asset preview if theme is preset)
            val cdnUrl = com.app.personalization.data.ResourceConfig.getWidgetPreviewUrl("theme_decorates/${theme.path}")
            val localFallbackPath = "file:///android_asset/theme_decorates/${theme.path}/popup_background.png"
            Glide.with(context)
                .load(cdnUrl)
                .placeholder(R.drawable.bg_widget)
                .error(
                    Glide.with(context)
                        .load(localFallbackPath)
                        .placeholder(R.drawable.bg_widget)
                        .error(R.drawable.bg_widget)
                )
                .into(holder.ivPreview)

            if (item.isSelected) {
                holder.ivChecked.visibility = View.VISIBLE
                holder.ivChecked.setImageResource(R.drawable.ic_radio_checked)
            } else {
                holder.ivChecked.visibility = View.GONE
            }

            holder.llContainer.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount(): Int = list.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(theme: KeyboardTheme): DownloadWidgetFragment {
            return DownloadWidgetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("theme", theme)
                }
            }
        }
    }
}
