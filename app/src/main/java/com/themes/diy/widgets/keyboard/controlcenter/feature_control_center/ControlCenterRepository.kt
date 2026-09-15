package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

class ControlCenterRepository(private val context: Context) {

    private val cacheFileName = "control_center_cache.json"

    suspend fun getCategories(): List<ControlCategory> = withContext(Dispatchers.IO) {
        getCategoriesFast()
    }

    fun getCategoriesFast(): List<ControlCategory> {
        inMemoryCategories?.let { return it }

        synchronized(cacheLock) {
            inMemoryCategories?.let { return it }

            var jsonStr: String? = null

            // 1. Try disk cache if present
            try {
                val diskFile = File(context.filesDir, cacheFileName)
                if (diskFile.exists() && diskFile.length() > 0) {
                    jsonStr = diskFile.readText()
                }
            } catch (_: Exception) {}

            // 2. Fallback to bundled asset assets_control_center/control_center.json
            if (jsonStr.isNullOrEmpty()) {
                try {
                    val stream = context.assets.open("assets_control_center/control_center.json")
                    jsonStr = stream.bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val parsed = parseJsonToCategories(jsonStr)
            val result = if (parsed.isNotEmpty()) {
                parsed.toMutableList().also { scanLocalAssets(it) }
            } else {
                mutableListOf<ControlCategory>().also { scanLocalAssets(it) }
            }

            if (result.isNotEmpty()) {
                inMemoryCategories = result
            }
            return result
        }
    }

    suspend fun refreshFromCdn(): List<ControlCategory>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("${ResourceConfig.ASSET_BASE_URL}/assets_control_center/control_center.json")
            val connection = url.openConnection()
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val jsonStr = connection.getInputStream().bufferedReader().use { it.readText() }

            if (!jsonStr.isNullOrBlank()) {
                val parsed = parseJsonToCategories(jsonStr)
                if (parsed.isNotEmpty()) {
                    try {
                        File(context.filesDir, cacheFileName).writeText(jsonStr)
                    } catch (_: Exception) {}

                    val result = parsed.toMutableList().also { scanLocalAssets(it) }
                    synchronized(cacheLock) {
                        inMemoryCategories = result
                    }
                    return@withContext result
                }
            }
        } catch (_: Exception) {
            // Silently ignore network timeouts in background
        }
        null
    }

    private fun parseJsonToCategories(jsonStr: String?): List<ControlCategory> {
        if (jsonStr.isNullOrEmpty()) return emptyList()
        val resultCategories = mutableListOf<ControlCategory>()
        try {
            val rootJson = JSONObject(jsonStr)
            val catArray = rootJson.optJSONArray("categories") ?: return emptyList()

            for (i in 0 until catArray.length()) {
                val catObj = catArray.getJSONObject(i)
                val catSlug = catObj.optString("slug", "category_$i")
                val catName = catObj.optString("name", "Category $i")
                val themeArray = catObj.optJSONArray("themes")

                val themeList = mutableListOf<ControlTheme>()
                if (themeArray != null) {
                    for (j in 0 until themeArray.length()) {
                        val tObj = themeArray.getJSONObject(j)
                        val rawId = tObj.optString("id", "${catSlug}_$j")
                        val rawName = tObj.optString("name", "Theme $j")
                        val rawFolderPath = tObj.optString("folderPath", "assets_control_center/control_themes/$catSlug/$rawId")

                        val (resolvedId, resolvedName, resolvedFolderPath) = resolveThemeMetadata(rawId, rawName, rawFolderPath)
                        val thumbUrl = "${ResourceConfig.ASSET_BASE_URL}/$resolvedFolderPath/thumb.webp"

                        themeList.add(
                            ControlTheme(
                                key = resolvedId,
                                name = resolvedName,
                                slug = resolvedId,
                                category = catName,
                                categorySlug = catSlug,
                                folderPath = resolvedFolderPath,
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultCategories
    }

    private fun scanLocalAssets(resultCategories: MutableList<ControlCategory>) {
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
    }

    companion object {
        @Volatile
        private var inMemoryCategories: List<ControlCategory>? = null
        private val cacheLock = Any()

        fun resolveThemeMetadata(rawId: String, rawName: String, rawFolderPath: String): Triple<String, String, String> {
            val cleanId = rawId.lowercase().trim()
            val cleanFolder = rawFolderPath.removePrefix("file:///android_asset/")
                .removePrefix("android_asset/")
                .removePrefix("/")

            return when {
                cleanId == "purple_glitch_art_kuromi_girl" || cleanFolder.contains("purple_glitch_art_kuromi_girl") -> {
                    Triple("purple_glitch_art_kuromi_girl", "Kuromi Purple", "assets_control_center/control_themes/cute/purple_glitch_art_kuromi_girl")
                }
                cleanId == "transparent_shimmering_butterfly" || cleanFolder.contains("transparent_shimmering_butterfly") -> {
                    Triple("transparent_shimmering_butterfly", "Shimmer Butterfly", "assets_control_center/control_themes/dark/transparent_shimmering_butterfly")
                }
                cleanId == "spooky_halloween" || cleanFolder.contains("spooky_halloween") -> {
                    Triple("halloween", "Halloween", "assets_control_center/control_themes/halloween/halloween")
                }
                cleanId == "classic_ios" || cleanFolder.contains("classic_ios") -> {
                    Triple("default_purple_flower", "Purple Flower", "assets_control_center/control_themes/default/default_purple_flower")
                }
                cleanId == "capybara" && cleanFolder.contains("cute/capybara") -> {
                    Triple("capybara", rawName, "assets_control_center/control_themes/anime/capybara")
                }
                cleanId == "cute_3d_stitch" && cleanFolder.contains("cute/cute_3d_stitch") -> {
                    Triple("cute_3d_stitch", rawName, "assets_control_center/control_themes/anime/cute_3d_stitch")
                }
                else -> {
                    Triple(rawId, rawName, cleanFolder)
                }
            }
        }

        fun resolveControlThemeFolderPath(rawPath: String): String {
            val cleanFolder = rawPath.removePrefix("file:///android_asset/")
                .removePrefix("android_asset/")
                .removePrefix("/")
            return resolveThemeMetadata(cleanFolder.substringAfterLast("/"), "", cleanFolder).third
        }
    }
}
