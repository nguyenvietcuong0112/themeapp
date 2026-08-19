package com.app.personalization.feature_control_center

import android.content.Context
import com.app.personalization.core.data.ResourceConfig
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
                } catch (e: Exception) {
                    // Ignore, fallback to defaults
                }

                // Check thumbnail and preview
                val thumbFile = "file:///android_asset/$themeFolder/thumb.webp"
                val previewFile = "file:///android_asset/$themeFolder/preview.jpg"

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
                        isNew = isNew
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
