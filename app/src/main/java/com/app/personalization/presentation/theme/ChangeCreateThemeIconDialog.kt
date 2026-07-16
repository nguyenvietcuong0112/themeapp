package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeIcon
import com.app.personalization.databinding.FragmentChangeCreateThemeIconBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangeCreateThemeIconDialog : BottomSheetDialogFragment() {

    interface OnChangeAppListener {
        fun onSelect(icon: WidgetThemeIcon)
    }

    private var _binding: FragmentChangeCreateThemeIconBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChangeCreateThemeIconViewModel
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
        _binding = FragmentChangeCreateThemeIconBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ChangeCreateThemeIconViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Icons"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Setup Category list (horizontal)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.visibility = View.VISIBLE

        // Setup Grid list ( cuon doc)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        observeViewModel()
        viewModel.loadIcons()
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { cats ->
            val selected = viewModel.selectedCategory.value ?: "All"
            binding.categoryRecyclerView.adapter = CreateThemeCategoryAdapter(cats, selected) { cat ->
                viewModel.filterIcons(cat)
            }
        }

        viewModel.icons.observe(viewLifecycleOwner) { list ->
            binding.pbCreate.visibility = View.GONE
            binding.recyclerView.adapter = ThemeIconItemAdapter(list) { item ->
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
    private class ThemeIconItemAdapter(
        private val list: List<WidgetThemeIcon>,
        private val onSelected: (WidgetThemeIcon) -> Unit
    ) : RecyclerView.Adapter<ThemeIconItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_my_icon_pack_layout,
                parent,
                false
            )
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
            private val tvInstall: TextView = view.findViewById(R.id.tvInstall)

            fun bind(item: WidgetThemeIcon, onSelected: (WidgetThemeIcon) -> Unit) {
                tvName.text = item.name
                tvInstall.text = "Select"

                // Load the icon pack preview image (bg_icon.png) from CDN
                val resolvedFolder = com.app.personalization.data.ResourceConfig.getThemeFolderByPath(itemView.context, item.folder)
                val previewUrl = com.app.personalization.data.CdnPathResolver.getIconPackPreviewUrl(resolvedFolder)
                Glide.with(itemView.context)
                    .load(previewUrl)
                    .centerInside()
                    .into(ivPreview)

                itemView.setOnClickListener {
                    onSelected(item)
                }
                tvInstall.setOnClickListener {
                    onSelected(item)
                }
            }
        }
    }
}
