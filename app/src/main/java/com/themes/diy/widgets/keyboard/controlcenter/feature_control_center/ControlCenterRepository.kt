package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class ControlCenterRepository(private val context: Context) {

    private var cachedCategories: List<ControlCategory>? = null

    suspend fun getCategories(): List<ControlCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }

        val resultCategories = mutableListOf<ControlCategory>()

        // 1. Read exclusively from dedicated assets_control_center/control_center.json (CDN or local assets)
        var jsonStr: String? = null
        try {
            // Try downloading latest control_center.json from CDN
            val url = URL("${ResourceConfig.ASSET_BASE_URL}/assets_control_center/control_center.json")
            val connection = url.openConnection()
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            jsonStr = connection.getInputStream().bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            // Fallback to local asset assets_control_center/control_center.json
            try {
                val stream = context.assets.open("assets_control_center/control_center.json")
                jsonStr = stream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!jsonStr.isNullOrEmpty()) {
            try {
                val rootJson = JSONObject(jsonStr)
                val catArray = rootJson.optJSONArray("categories")

                if (catArray != null) {
                    for (i in 0 until catArray.length()) {
                        val catObj = catArray.getJSONObject(i)
                        val catSlug = catObj.optString("slug", "category_$i")
                        val catName = catObj.optString("name", "Category $i")
                        val themeArray = catObj.optJSONArray("themes")

                        val themeList = mutableListOf<ControlTheme>()
                        if (themeArray != null) {
                            for (j in 0 until themeArray.length()) {
                                val tObj = themeArray.getJSONObject(j)
                                val id = tObj.optString("id", "${catSlug}_$j")
                                val name = tObj.optString("name", "Theme $j")
                                val folderPath = tObj.optString("folderPath", "assets_control_center/control_themes/$catSlug/$id")
                                val rawThumbUrl = tObj.optString("thumbUrl")

                                val thumbUrl = if (rawThumbUrl.startsWith("http")) {
                                    rawThumbUrl
                                } else {
                                    "${ResourceConfig.ASSET_BASE_URL}/$folderPath/thumb.webp"
                                }

                                themeList.add(
                                    ControlTheme(
                                        key = id,
                                        name = name,
                                        slug = id,
                                        category = catName,
                                        categorySlug = catSlug,
                                        folderPath = folderPath,
                                        thumbPath = thumbUrl,
                                        previewPath = thumbUrl,
                                        isHot = tObj.optBoolean("isHot", false),
                                        isNew = tObj.optBoolean("isNew", false),
                                        downloads = tObj.optLong("downloads", 1000000L)
                                    )
                                )
                            }
                        }

                        if (themeList.isNotEmpty()) {
                            resultCategories.add(
                                ControlCategory(
                                    slug = catSlug,
                                    name = catName,
                                    themes = themeList
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Scan local asset folders in assets_control_center/control_themes if present
        try {
            val assetManager = context.assets
            val rootPath = "${ResourceConfig.CONTROL_CENTER}/control_themes"
            val categoryDirs = assetManager.list(rootPath) ?: emptyArray()

            for (catSlug in categoryDirs) {
                val catPath = "$rootPath/$catSlug"
                val themeDirs = assetManager.list(catPath) ?: emptyArray()

                val categoryName = catSlug.replaceFirstChar { it.uppercase() }
                val localCategory = resultCategories.find { it.slug == catSlug }
                    ?: ControlCategory(slug = catSlug, name = categoryName, themes = mutableListOf()).also {
                        resultCategories.add(it)
                    }

                val currentList = localCategory.themes as? MutableList<ControlTheme> ?: localCategory.themes.toMutableList()

                for (themeSlug in themeDirs) {
                    val themeFolder = "$catPath/$themeSlug"
                    if (currentList.none { it.slug == themeSlug }) {
                        var name = themeSlug.replace("_", " ").replaceFirstChar { it.uppercase() }
                        var key = themeSlug

                        try {
                            val metaStream = assetManager.open("$themeFolder/metadata.json")
                            val metaJsonStr = metaStream.bufferedReader().use { it.readText() }
                            val json = JSONObject(metaJsonStr)
                            name = json.optString("name", name)
                            key = json.optString("key", key)
                        } catch (_: Exception) {}

                        val thumbFile = "file:///android_asset/$themeFolder/thumb.webp"

                        currentList.add(
                            ControlTheme(
                                key = key,
                                name = name,
                                slug = themeSlug,
                                category = categoryName,
                                categorySlug = catSlug,
                                folderPath = themeFolder,
                                thumbPath = thumbFile,
                                previewPath = thumbFile,
                                isHot = true,
                                isNew = false,
                                downloads = 7654321L
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedCategories = resultCategories
        resultCategories
    }
}
