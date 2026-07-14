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
import com.app.personalization.data.database.entity.ThemeIconPack
import com.app.personalization.databinding.FragmentChangeCreateThemeIconBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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
            val dbList = com.app.personalization.data.database.ThemeDatabase.getDatabase(requireContext()).iconDao().getAllIconPacks()
            
            val iconPacks = if (dbList.isNotEmpty()) {
                dbList
            } else {
                // Fallback list of 10 default packs if DB is empty
                (1..10).map {
                    ThemeIconPack(
                        id = UUID.randomUUID(),
                        name = "Aesthetic Theme $it",
                        folder = "theme_$it",
                        themeId = UUID.randomUUID()
                    )
                }
            }

            withContext(Dispatchers.Main) {
                binding.pbCreate.visibility = View.GONE
                binding.recyclerView.adapter = IconPackAdapter(iconPacks) { selectedPack ->
                    // Build list of 8 icon urls
                    val iconsList = listOf(
                        "facebook", "instagram", "messenger", "tiktok",
                        "chrome", "gmail", "camera", "settings"
                    )
                    val urls = iconsList.map { com.app.personalization.data.CdnPathResolver.getSingleIconUrl(selectedPack.folder, it) }
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
        private val list: List<ThemeIconPack>,
        private val onSelected: (ThemeIconPack) -> Unit
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

            fun bind(item: ThemeIconPack, onSelected: (ThemeIconPack) -> Unit) {
                tvName.text = item.name
                tvInstall.text = "Select"

                // Load preview icon of the pack (Facebook) using CdnPathResolver
                val previewUrl = com.app.personalization.data.CdnPathResolver.getSingleIconUrl(item.folder, "facebook")
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
