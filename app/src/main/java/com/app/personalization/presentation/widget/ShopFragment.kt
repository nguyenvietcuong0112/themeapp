package com.app.personalization.presentation.widget

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
import com.app.personalization.presentation.widget.DownloadThemeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShopFragment : Fragment() {

    private lateinit var rvThemes: RecyclerView
    private lateinit var adapter: WidgetThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvThemes = view.findViewById(R.id.recyclerView)

        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columns = (screenWidthDp / 160).toInt().coerceAtLeast(2)
        rvThemes.layoutManager = GridLayoutManager(context, columns)

        lifecycleScope.launch(Dispatchers.IO) {
            val dbThemes = try {
                ServiceLocator.getWidgetThemeDao(context).getAllWidgetThemes()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val themeList = dbThemes.map { 
                WidgetTheme(
                    id = it.id,
                    name = it.name,
                    folder = it.folder
                )
            }.ifEmpty {
                (1..161).map { i ->
                    WidgetTheme(
                        id = "widget_theme_$i",
                        name = "Theme $i",
                        folder = "theme_$i"
                    )
                }
            }

            withContext(Dispatchers.Main) {
                adapter = WidgetThemeAdapter(themeList, displayMetrics.widthPixels, columns) { item ->
                    val intent = Intent(context, DownloadThemeActivity::class.java).apply {
                        putExtra("theme_id", item.id)
                        putExtra("theme_name", item.name)
                        putExtra("theme_path", item.folder)
                        putExtra("theme_type", "widget_theme")
                        putExtra("start_tab", 2) // Widget Tab directly (position 2)
                    }
                    startActivity(intent)
                }
                rvThemes.adapter = adapter
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ShopFragment()
    }
}
