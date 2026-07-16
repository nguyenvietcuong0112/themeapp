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
                try {
                    val urlConnection = java.net.URL("${com.app.personalization.data.ResourceConfig.S3_URL}/themes/json/theme_data_decorate.json?t=${System.currentTimeMillis()}").openConnection()
                    urlConnection.connectTimeout = 3000
                    urlConnection.readTimeout = 3000
                    val jsonStr = urlConnection.getInputStream().bufferedReader().use { it.readText() }
                    val categories = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                    categories.flatMap { cat ->
                        cat.themes.map { t ->
                            WidgetTheme(
                                id = "widget_theme_${t.themePath.replace("/", "_")}",
                                name = t.themeName,
                                folder = t.themePath
                            )
                        }
                    }
                } catch (e: Exception) {
                    try {
                        val jsonStr = com.app.personalization.data.FileUtils.loadJsonFromAsset(requireContext(), "themes/json/theme_data_decorate.json")
                        val categories = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<com.app.personalization.data.DecorateCategory>>(jsonStr)
                        categories.flatMap { cat ->
                            cat.themes.map { t ->
                                WidgetTheme(
                                    id = "widget_theme_${t.themePath.replace("/", "_")}",
                                    name = t.themeName,
                                    folder = t.themePath
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        emptyList()
                    }
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
