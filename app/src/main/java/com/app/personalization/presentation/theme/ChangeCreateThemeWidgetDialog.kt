package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeWidget
import com.app.personalization.databinding.FragmentChangeCreateThemeWidgetBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangeCreateThemeWidgetDialog : BottomSheetDialogFragment() {

    interface OnChangeAppListener {
        fun onSelect(widget: WidgetThemeWidget)
    }

    private var _binding: FragmentChangeCreateThemeWidgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChangeCreateThemeWidgetViewModel
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
        _binding = FragmentChangeCreateThemeWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ChangeCreateThemeWidgetViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Widgets"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Setup Category list (horizontal)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.visibility = View.VISIBLE

        // Setup List (1 column)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        observeViewModel()
        viewModel.loadWidgets()
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            val selected = viewModel.selectedCategory.value ?: "All"
            binding.categoryRecyclerView.adapter = CreateThemeCategoryAdapter(cats, selected) { cat ->
                viewModel.filterWidgets(cat)
            }
        }

        viewModel.widgets.observe(viewLifecycleOwner) { list ->
            binding.pbCreate.visibility = View.GONE
            binding.recyclerView.adapter = CreateThemeWidgetItemAdapter(list) { item ->
                listener?.onSelect(item)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter
    private class CreateThemeWidgetItemAdapter(
        private val list: List<WidgetThemeWidget>,
        private val onSelected: (WidgetThemeWidget) -> Unit
    ) : RecyclerView.Adapter<CreateThemeWidgetItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_widget_theme,
                parent,
                false
            )
            val density = parent.context.resources.displayMetrics.density
            val screenWidth = parent.context.resources.displayMetrics.widthPixels
            val itemWidth = screenWidth - (32 * density).toInt()
            val itemHeight = (itemWidth * 9) / 16
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
            private val cardView: View? = view.findViewById(R.id.cardView)

            fun bind(item: WidgetThemeWidget, onSelected: (WidgetThemeWidget) -> Unit) {
                tvName.text = item.name

                val type = when (item.category.lowercase()) {
                    "calendar" -> "today"
                    "weather" -> "weather"
                    "image" -> "image"
                    else -> "clocks"
                }
                
                val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(itemView.context, item.folder)
                
                // Construct URL as requested:
                // Nếu folderChild null: [BaseURL]/themes/[folder]/widgets/[type]/bg_preview_medium.png
                val widgetUrl = "${com.app.personalization.data.ResourceConfig.S3_URL}/themes/$resolvedFolder/widgets/$type/bg_preview_medium.png"

                Glide.with(itemView.context)
                    .load(widgetUrl)
                    .centerInside()
                    .placeholder(R.drawable.bg_default_placeholder)
                    .error(
                        // Fallback to general medium widget preview if type preview doesn't exist
                        com.app.personalization.data.CdnPathResolver.getWidgetPreviewUrl(resolvedFolder, "medium")
                    )
                    .into(ivPreview)

                val clickTarget = cardView ?: itemView
                clickTarget.setOnClickListener {
                    onSelected(item)
                }
            }
        }
    }
}
