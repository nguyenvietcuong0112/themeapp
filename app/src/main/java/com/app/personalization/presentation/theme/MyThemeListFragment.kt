package com.app.personalization.presentation.theme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.di.ServiceLocator
import com.app.personalization.presentation.main.ThemeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyThemeListFragment : Fragment() {

    private lateinit var rvThemes: RecyclerView
    private lateinit var adapter: ThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme, container, false)
        view.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
        view.findViewById<View>(R.id.actionView)?.visibility = View.GONE
        view.findViewById<View>(R.id.categoryRecyclerView)?.visibility = View.GONE
        view.findViewById<View>(R.id.tvAdd)?.visibility = View.GONE
        view.findViewById<View>(R.id.tvCreate)?.visibility = View.GONE
        view.findViewById<View>(R.id.pbCreate)?.visibility = View.GONE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvThemes = view.findViewById(R.id.recyclerView)
        rvThemes.layoutManager = GridLayoutManager(context, 2)

        adapter = ThemeAdapter(
            onThemeClick = { theme ->
                val intent = Intent(context, ThemePreviewActivity::class.java).apply {
                    putExtra("theme_id", theme.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { theme ->
                deleteTheme(theme)
            }
        )
        rvThemes.adapter = adapter

        loadDiyThemes()
    }

    override fun onResume() {
        super.onResume()
        loadDiyThemes()
    }

    private fun loadDiyThemes() {
        lifecycleScope.launch {
            val context = context ?: return@launch
            val diyThemes = withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.getThemeDao(context).getAllThemes()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            adapter.submitList(diyThemes)
        }
    }

    private fun deleteTheme(theme: com.app.personalization.data.database.entity.KeyboardTheme) {
        lifecycleScope.launch {
            val context = context ?: return@launch
            withContext(Dispatchers.IO) {
                try {
                    ServiceLocator.getThemeDao(context).deleteTheme(theme)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            loadDiyThemes()
        }
    }
}
