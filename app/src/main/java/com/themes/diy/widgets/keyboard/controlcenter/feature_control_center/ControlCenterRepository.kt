package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class ControlCenterRepository(private val context: Context) {

    private var cachedCategories: List<ControlCategory>? = null

    suspend fun getCategories(): List<ControlCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }

        val assetManager = context.assets
        val rootPath = "${ResourceConfig.CONTROL_CENTER}/control_themes"
        val categoryDirs = try {
            assetManager.list(rootPath) ?: emptyArray()
        } catch (e: Exception) {
            emptyArray()
        }

        val categoriesList = mutableListOf<ControlCategory>()

        for (catSlug in categoryDirs) {
            val catPath = "$rootPath/$catSlug"
            val themeDirs = try {
                assetManager.list(catPath) ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }

            val themes = mutableListOf<ControlTheme>()
            for (themeSlug in themeDirs) {
                val themeFolder = "$catPath/$themeSlug"
                var name = themeSlug.replace("_", " ").replaceFirstChar { it.uppercase() }
                var categoryName = catSlug.replaceFirstChar { it.uppercase() }
                var key = themeSlug
                var isHot = false
                var isNew = false
                var downloads = 7654321L

                // Try reading metadata.json
                try {
                    val metaStream = assetManager.open("$themeFolder/metadata.json")
                    val jsonStr = metaStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)
                    name = json.optString("name", name)
                    categoryName = json.optString("category", categoryName)
                    key = json.optString("key", key)
                    isHot = json.optBoolean("isHot", false)
                    isNew = json.optBoolean("isNew", false)
                    downloads = json.optLong("downloads", 7654321L)
                } catch (e: Exception) {
                    // Ignore, fallback to defaults
                }

                // Check thumbnail and preview
                val thumbFile = "${ResourceConfig.ASSET_BASE_URL}/$themeFolder/thumb.webp"
                val previewFile = "${ResourceConfig.ASSET_BASE_URL}/$themeFolder/preview.jpg"

                themes.add(
                    ControlTheme(
                        key = key,
                        name = name,
                        slug = themeSlug,
                        category = categoryName,
                        categorySlug = catSlug,
                        folderPath = themeFolder,
                        thumbPath = thumbFile,
                        previewPath = previewFile,
                        isHot = isHot,
                        isNew = isNew,
                        downloads = downloads
                    )
                )
            }

            if (themes.isNotEmpty()) {
                val catName = themes.firstOrNull()?.category ?: catSlug.replaceFirstChar { it.uppercase() }
                categoriesList.add(
                    ControlCategory(
                        slug = catSlug,
                        name = catName,
                        themes = themes
                    )
                )
            }
        }

        cachedCategories = categoriesList
        categoriesList
    }
}
