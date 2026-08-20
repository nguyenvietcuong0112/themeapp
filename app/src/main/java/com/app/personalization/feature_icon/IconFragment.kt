package com.app.personalization.feature_icon

import com.app.personalization.feature_theme.ThemeCategoryAdapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R

class IconFragment : Fragment() {

    private lateinit var viewModel: IconViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var iconPackAdapter: IconPackAdapter
    private lateinit var pbCreate: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[IconViewModel::class.java]

        setupCategories(view)
        setupIconGrid(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupCategories(view: View) {
        val rvCategories = view.findViewById<RecyclerView>(R.id.categoryRecyclerView) ?: return
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        categoryAdapter = ThemeCategoryAdapter { categoryTag ->
            viewModel.selectCategory(categoryTag.id)
        }
        rvCategories.adapter = categoryAdapter
    }

    private fun setupIconGrid(view: View) {
        val rvIcons = view.findViewById<RecyclerView>(R.id.recyclerView) ?: return
        pbCreate = view.findViewById(R.id.pbCreate)
        pbCreate.visibility = View.VISIBLE

        val context = requireContext()
        val columns = 1
        rvIcons.layoutManager = LinearLayoutManager(context)

        val margin = context.resources.getDimensionPixelSize(R.dimen.dp_8) * 2
        val displayWidth = context.resources.displayMetrics.widthPixels
        val availableWidth = displayWidth - margin

        iconPackAdapter = IconPackAdapter(
            parentWidth = availableWidth,
            columns = columns,
            onItemClick = { iconPack ->
                val intent = Intent(context, DownloadIconActivity::class.java).apply {
                    putExtra("theme_id", iconPack.id)
                    putExtra("theme_name", iconPack.name)
                    putExtra("theme_path", iconPack.folder)
                    putExtra("theme_type", "widget_theme")
                }
                startActivity(intent)
            }
        )
        rvIcons.adapter = iconPackAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.icons.observe(viewLifecycleOwner) { icons ->
            pbCreate.visibility = View.GONE
            iconPackAdapter.submitList(icons)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = IconFragment()
    }
}
