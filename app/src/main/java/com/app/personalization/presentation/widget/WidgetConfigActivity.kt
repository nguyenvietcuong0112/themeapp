package com.app.personalization.presentation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.DecorateCategory
import com.app.personalization.data.FileUtils
import com.app.personalization.data.ResourceConfig
import com.app.personalization.data.database.entity.WidgetConfig
import com.app.personalization.di.ServiceLocator
import com.app.personalization.presentation.theme.CreateThemeCategoryAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.UUID

class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvWidgets: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var categoriesData = listOf<DecorateCategory>()
    private var activeCategoryName: String = "Trending"
    private var widgetItems = mutableListOf<WidgetTemplateItem>()
    private lateinit var widgetAdapter: WidgetConfigAdapter

    data class WidgetTemplateItem(
        val id: String,
        val name: String,
        val size: String,
        val widgetType: String,
        val previewUrl: String,
        val themePath: String,
        val themeName: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        setContentView(R.layout.activity_create_widget)

        progressBar = findViewById(R.id.progressBar)
        rvCategories = findViewById(R.id.categoryRecyclerView)
        rvWidgets = findViewById(R.id.recyclerView)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        setupRecyclerViews()
        loadCategoriesAndWidgets()
    }

    private fun setupRecyclerViews() {
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val columns = 3
        val gridLayoutManager = GridLayoutManager(this, columns)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = widgetItems.getOrNull(position)
                return if (item?.size == "4x2") 2 else 1
            }
        }
        rvWidgets.layoutManager = gridLayoutManager

        widgetAdapter = WidgetConfigAdapter(widgetItems) { item ->
            onWidgetClicked(item)
        }
        rvWidgets.adapter = widgetAdapter
    }

    private fun loadCategoriesAndWidgets() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonCategories = try {
                val urlConnection = URL("${ResourceConfig.S3_URL}/themes/json/theme_data_decorate.json?t=${System.currentTimeMillis()}").openConnection()
                urlConnection.connectTimeout = 3000
                urlConnection.readTimeout = 3000
                val jsonStr = urlConnection.getInputStream().bufferedReader().use { it.readText() }
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<DecorateCategory>>(jsonStr)
            } catch (e: Exception) {
                try {
                    val jsonStr = FileUtils.loadJsonFromAsset(this@WidgetConfigActivity, "themes/json/theme_data_decorate.json")
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<List<DecorateCategory>>(jsonStr)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    emptyList()
                }
            }

            // Exclude Aesthetic category from gallery just like Theme Tab, and sort Trending to top
            val filteredCategories = jsonCategories
                .filter { !it.category.equals("Aesthetic", ignoreCase = true) }
                .sortedByDescending { it.category.equals("Trending", ignoreCase = true) }
            categoriesData = filteredCategories

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (categoriesData.isNotEmpty()) {
                    activeCategoryName = "Trending"
                    updateCategoryAdapter()
                    buildWidgetItemsForCategory("Trending")
                }
            }
        }
    }

    private fun updateCategoryAdapter() {
        val cats = categoriesData.map { it.name }
        val activeName = categoriesData.firstOrNull { it.category == activeCategoryName }?.name ?: ""
        
        rvCategories.adapter = CreateThemeCategoryAdapter(cats, activeName) { selectedName ->
            val foundCat = categoriesData.firstOrNull { it.name == selectedName }
            if (foundCat != null) {
                activeCategoryName = foundCat.category
                buildWidgetItemsForCategory(activeCategoryName)
            }
        }
    }

    private fun buildWidgetItemsForCategory(category: String) {
        widgetItems.clear()
        val catData = categoriesData.firstOrNull { it.category == category }
        if (catData != null) {
            addWidgetItemsFromCategory(catData)
        }
        widgetAdapter.notifyDataSetChanged()
    }

    private fun addWidgetItemsFromCategory(catData: DecorateCategory) {
        for (theme in catData.themes) {
            val path = theme.themePath
            val name = theme.themeName

            // 1. Calendar Widget (4x2)
            widgetItems.add(
                WidgetTemplateItem(
                    id = "${path}_calendar_4x2",
                    name = "$name Calendar",
                    size = "4x2",
                    widgetType = "calendar",
                    previewUrl = "${ResourceConfig.S3_URL}/themes/$path/widgets/today/bg_preview_medium.png",
                    themePath = path,
                    themeName = name
                )
            )

            // 2. Clock Widget (2x2)
            widgetItems.add(
                WidgetTemplateItem(
                    id = "${path}_clocks_2x2",
                    name = "$name Clock",
                    size = "2x2",
                    widgetType = "clock",
                    previewUrl = "${ResourceConfig.S3_URL}/themes/$path/widgets/clocks/bg_preview_large.png",
                    themePath = path,
                    themeName = name
                )
            )

            // 3. Weather Widget (2x2)
            widgetItems.add(
                WidgetTemplateItem(
                    id = "${path}_weather_2x2",
                    name = "$name Weather",
                    size = "2x2",
                    widgetType = "weather",
                    previewUrl = "${ResourceConfig.S3_URL}/themes/$path/widgets/weather/bg_preview_large.png",
                    themePath = path,
                    themeName = name
                )
            )

            // 4. Date Widget (2x2)
            widgetItems.add(
                WidgetTemplateItem(
                    id = "${path}_date_2x2",
                    name = "$name Date",
                    size = "2x2",
                    widgetType = "date",
                    previewUrl = "${ResourceConfig.S3_URL}/themes/$path/widgets/today/bg_preview_large.png",
                    themePath = path,
                    themeName = name
                )
            )

            // 5. Image Widget (2x2)
            widgetItems.add(
                WidgetTemplateItem(
                    id = "${path}_image_2x2",
                    name = "$name Image",
                    size = "2x2",
                    widgetType = "image",
                    previewUrl = "${ResourceConfig.S3_URL}/themes/$path/widgets/image/bg_preview_large.png",
                    themePath = path,
                    themeName = name
                )
            )
        }
    }

    private fun onWidgetClicked(item: WidgetTemplateItem) {
        val sheet = SelectWidgetBottomSheet()
        val uuidString = UUID.nameUUIDFromBytes(item.themePath.toByteArray()).toString()
        val dummyTheme = com.app.personalization.data.database.entity.KeyboardTheme(
            id = uuidString,
            name = item.themeName,
            path = item.themePath,
            rawType = "widget_theme"
        )
        sheet.setParams(dummyTheme, item.widgetType, item.size, item.previewUrl, widgetId)
        sheet.show(supportFragmentManager, "select_widget")
    }

    private fun applyWidgetToId(item: WidgetTemplateItem) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fileName = when (item.size) {
                    "2x2" -> "bg_medium.png"
                    "4x2" -> "bg_medium.png"
                    "4x4" -> "bg_large.png"
                    else -> "bg_medium.png"
                }
                val cdnUrl = "${ResourceConfig.S3_URL}/themes/${item.themePath}/widgets/$fileName"
                
                val bitmap = Glide.with(applicationContext)
                    .asBitmap()
                    .load(cdnUrl)
                    .submit()
                    .get()

                val localName = "widget_bg_${item.id}_${item.widgetType}_${item.size}.png"
                openFileOutput(localName, MODE_PRIVATE).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }

                val file = getFileStreamPath(localName)
                val config = WidgetConfig(
                    widgetId = widgetId,
                    bgType = "IMAGE",
                    solidColor = 0,
                    imageUri = Uri.fromFile(file).toString(),
                    textColor = Color.WHITE,
                    fontStyle = "normal",
                    gradientStartColor = 0,
                    gradientEndColor = 0
                )

                getSharedPreferences("widget_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("widget_type_$widgetId", item.widgetType)
                    .apply()
                ServiceLocator.getWidgetConfigDao(this@WidgetConfigActivity).saveConfig(config)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigActivity)
                    Widget2x2Provider().updateWidget(this@WidgetConfigActivity, appWidgetManager, widgetId)
                    Widget4x2Provider().updateWidget(this@WidgetConfigActivity, appWidgetManager, widgetId)
                    Widget4x4Provider().updateWidget(this@WidgetConfigActivity, appWidgetManager, widgetId)

                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    }
                    setResult(Activity.RESULT_OK, resultValue)
                    Toast.makeText(this@WidgetConfigActivity, "Widget applied successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@WidgetConfigActivity, "Failed to apply widget style", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class WidgetConfigAdapter(
        private val items: List<WidgetTemplateItem>,
        private val onItemClick: (WidgetTemplateItem) -> Unit
    ) : RecyclerView.Adapter<WidgetConfigAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val llContainer: View = view.findViewById(R.id.llContainer) ?: view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_config_widget,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val density = displayMetrics.density
            val horizontalPadding = (64 * density).toInt()
            val gridWidth = screenWidth - horizontalPadding
            val itemHeight = gridWidth / 3

            val margin = (8 * density).toInt()
            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight
            )
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = itemHeight
            lp.setMargins(margin, margin, margin, margin)
            holder.itemView.layoutParams = lp

            Glide.with(context)
                .load(item.previewUrl)
                .placeholder(R.drawable.bg_default_placeholder)
                .error(R.drawable.bg_default_placeholder)
                .into(holder.ivPreview)

            holder.llContainer.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
