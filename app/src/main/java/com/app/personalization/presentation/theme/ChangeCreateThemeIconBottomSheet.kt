package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.entity.WidgetThemeIcon
import com.app.personalization.databinding.FragmentChangeCreateThemeIconBinding
import com.app.personalization.di.ServiceLocator
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCreateThemeIconBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentChangeCreateThemeIconBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: CreateThemeViewModel

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

        sharedViewModel = ViewModelProvider(requireActivity())[CreateThemeViewModel::class.java]

        // Setup Toolbar
        binding.toolbar.titleTextView.text = "Select Icon Pack"
        binding.toolbar.ivClose.setOnClickListener { dismiss() }
        binding.toolbar.ivBack.visibility = View.GONE

        // Hide category recycler
        binding.categoryRecyclerView.visibility = View.GONE

        // Setup Icon List
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.pbCreate.visibility = View.VISIBLE

        binding.viewClick.setOnClickListener { dismiss() }

        loadIconPacks()
    }

    private fun loadIconPacks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dbList = ServiceLocator.getIconPackDao(requireContext()).getAllIcons()
            
            val iconPacks = if (dbList.isNotEmpty()) {
                dbList
            } else {
                // Fallback list of 10 default packs if DB is empty
                (1..10).map {
                    WidgetThemeIcon(
                        id = "default_$it",
                        name = "Aesthetic Theme $it",
                        folder = "theme_$it",
                        category = "Aesthetic"
                    )
                }
            }

            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                binding.recyclerView.adapter = IconPackAdapter(iconPacks) { selectedPack ->
                    // Build list of 8 icon urls
                    val iconsList = listOf(
                        "ic_facebook.png", "ic_instagram.png", "ic_messenger.png", "ic_tiktok.png",
                        "ic_chrome.png", "ic_gmail.png", "ic_camera.png", "ic_settings.png"
                    )
                    val urls = iconsList.map { "https://csc-themeapp-widget.pages.dev/${selectedPack.folder}/icons/$it" }
                    sharedViewModel.selectIconPack(urls)
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
    private class IconPackAdapter(
        private val list: List<WidgetThemeIcon>,
        private val onSelected: (WidgetThemeIcon) -> Unit
    ) : RecyclerView.Adapter<IconPackAdapter.ViewHolder>() {

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

                // Load preview icon of the pack (Facebook)
                val previewUrl = "https://csc-themeapp-widget.pages.dev/${item.folder}/icons/ic_facebook.png"
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
