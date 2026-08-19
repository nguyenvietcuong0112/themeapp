package com.app.personalization.feature_control_center

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.app.personalization.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlCenterFragment : Fragment() {

    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var rvControlThemes: RecyclerView
    private lateinit var adapter: ControlCenterAdapter
    private val repository by lazy { ControlCenterRepository(requireContext()) }
    private var allCategories: List<ControlCategory> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_control_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        rvControlThemes = view.findViewById(R.id.rvControlThemes)

        rvControlThemes.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = ControlCenterAdapter(emptyList()) { theme ->
            lifecycleScope.launch(Dispatchers.IO) {
                val repo = com.app.personalization.feature_collections.data.CollectionRepository(requireContext())
                repo.markAsDownloaded(
                    id = "control_${theme.slug}",
                    name = theme.name,
                    category = "Control center",
                    targetPath = theme.folderPath,
                    previewPath = theme.thumbPath,
                    rawType = "control_center"
                )
            }
            Toast.makeText(requireContext(), "Selected Control Center: ${theme.name}", Toast.LENGTH_SHORT).show()
        }
        rvControlThemes.adapter = adapter

        loadControlThemes()
    }

    private fun loadControlThemes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val categories = repository.getCategories()
            allCategories = categories

            withContext(Dispatchers.Main) {
                setupCategoryChips(categories)
                val allThemes = categories.flatMap { it.themes }
                adapter.updateData(allThemes)
            }
        }
    }

    private fun setupCategoryChips(categories: List<ControlCategory>) {
        chipGroupCategories.removeAllViews()

        // "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                adapter.updateData(allCategories.flatMap { it.themes })
            }
        }
        chipGroupCategories.addView(allChip)

        // Specific category chips
        for (cat in categories) {
            val chip = Chip(requireContext()).apply {
                text = cat.name
                isCheckable = true
                setOnClickListener {
                    adapter.updateData(cat.themes)
                }
            }
            chipGroupCategories.addView(chip)
        }
    }

    companion object {
        fun newInstance() = ControlCenterFragment()
    }
}
