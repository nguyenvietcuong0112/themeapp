package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.ThemeWidget
import com.app.personalization.databinding.FragmentChangeCreateThemeWidgetBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChangeCreateThemeWidgetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentChangeCreateThemeWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: CreateThemeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeCreateThemeWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[CreateThemeViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Select Widget"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Hide category recycler
        binding.categoryRecyclerView.visibility = View.GONE

        // Setup Widget Grid (2 columns)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        loadWidgets()
    }

    private fun loadWidgets() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbList = com.app.personalization.data.database.ThemeDatabase.getDatabase(requireContext()).widgetDao().getAllWidgets()
            
            val widgets = if (dbList.isNotEmpty()) {
                dbList
            } else {
                // Fallback list of 10 default widgets if DB is empty
                (1..10).map {
                    ThemeWidget(
                        id = UUID.randomUUID(),
                        themeId = UUID.randomUUID(),
                        templatePath = "theme_$it",
                        size = "MEDIUM",
                        type = "CLOCK"
                    )
                }
            }

            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                binding.recyclerView.adapter = WidgetGridAdapter(widgets) { selectedWidget ->
                    // Get widget preview image URL using CdnPathResolver
                    val widgetUrl = com.app.personalization.data.CdnPathResolver.getWidgetPreviewUrl(selectedWidget.templatePath, selectedWidget.size)
                    sharedViewModel.selectWidget(widgetUrl)
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
    private class WidgetGridAdapter(
        private val list: List<ThemeWidget>,
        private val onSelected: (ThemeWidget) -> Unit
    ) : RecyclerView.Adapter<WidgetGridAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_theme,
                parent,
                false
            )
            // Make grid items fit correctly
            val density = parent.context.resources.displayMetrics.density
            val screenWidth = parent.context.resources.displayMetrics.widthPixels
            val itemWidth = (screenWidth - (32 * density).toInt()) / 2
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
            private val tvName: TextView = view.findViewById(R.id.tvName)

            fun bind(item: ThemeWidget, onSelected: (ThemeWidget) -> Unit) {
                tvName.text = "Widget ${item.type}"

                // Load widget preview via CdnPathResolver
                val widgetUrl = com.app.personalization.data.CdnPathResolver.getWidgetPreviewUrl(item.templatePath, item.size)
                Glide.with(itemView.context)
                    .load(widgetUrl)
                    .centerInside()
                    .into(ivPreview)

                itemView.setOnClickListener {
                    onSelected(item)
                }
            }
        }
    }
}
