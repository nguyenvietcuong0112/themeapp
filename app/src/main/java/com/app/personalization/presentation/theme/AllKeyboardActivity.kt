package com.app.personalization.presentation.theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.personalization.R
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.ResourceConfig
import com.app.personalization.databinding.ActivityAllKeyboardBinding

class AllKeyboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllKeyboardBinding
    private lateinit var viewModel: ThemeViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var keyboardAdapter: KeyboardThemeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        binding.toolbar.backButton.setOnClickListener {
            finish()
        }
        binding.toolbar.tvTitle.text = "Keyboard Themes"
        binding.toolbar.tvReset.visibility = View.GONE

        viewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Categories list
        categoryAdapter = ThemeCategoryAdapter { category ->
            viewModel.selectCategory(category.id)
        }
        binding.categoriesRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerview.adapter = categoryAdapter

        // Keyboards grid (2 columns)
        keyboardAdapter = KeyboardThemeAdapter { theme ->
            val intent = Intent(this, KeyboardThemeDetailActivity::class.java).apply {
                putExtra("selected_theme", theme)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = keyboardAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { list ->
            categoryAdapter.submitList(list)
            binding.categoriesRecyclerview.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.themes.observe(this) { list ->
            val cloudThemes = list.filter { it.rawType != "diy" }
            keyboardAdapter.submitList(cloudThemes)
            binding.pbLoading.visibility = View.GONE
        }
    }

    private inner class KeyboardThemeAdapter(
        private val onThemeClick: (KeyboardTheme) -> Unit
    ) : RecyclerView.Adapter<KeyboardThemeAdapter.ViewHolder>() {

        private var items = listOf<KeyboardTheme>()

        fun submitList(list: List<KeyboardTheme>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_keyboard_theme_grid, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onThemeClick)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val ivPreviewCustom: ImageView = view.findViewById(R.id.ivPreviewCustom)
            private val ivFavorite: View = view.findViewById(R.id.ivFavorite)
            private val ivDelete: View = view.findViewById(R.id.ivDelete)
            private val tvName: TextView = view.findViewById(R.id.tvName)
            private val cardView: View = view.findViewById(R.id.cardView)

            fun bind(theme: KeyboardTheme, onClick: (KeyboardTheme) -> Unit) {
                tvName.text = theme.name
                tvName.visibility = View.VISIBLE
                ivFavorite.visibility = View.GONE
                ivDelete.visibility = View.GONE
                ivPreviewCustom.visibility = View.GONE
                ivPreview.visibility = View.VISIBLE

                val previewUrl = ResourceConfig.getKeyboardPreviewUrl(theme.name, theme.path)

                Glide.with(itemView.context)
                    .load(previewUrl)
                    .placeholder(R.drawable.bg_default_placeholder)
                    .error(R.drawable.bg_default_placeholder)
                    .into(ivPreview)

                cardView.setOnClickListener {
                    onClick(theme)
                }
            }
        }
    }
}
