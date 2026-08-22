package com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data

import android.content.Context
import com.themes.diy.widgets.keyboard.controlcenter.core.data.FileUtils
import com.themes.diy.widgets.keyboard.controlcenter.core.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CollectionRepository(private val context: Context) {

    private val downloadedDao by lazy { ServiceLocator.getDownloadedCollectionDao(context) }
    private var cachedCollections: Map<String, List<CollectionItem>>? = null

    suspend fun getDiscoveryItems(category: String): List<CollectionItem> = withContext(Dispatchers.IO) {
        val allCollections = loadCollectionsFromAssets()
        val key = when (category.lowercase()) {
            "theme" -> "theme"
            "icons", "icon" -> "icons"
            "control center", "control" -> "control_center"
            "widget", "widgets" -> "widget"
            "wallpaper", "wallpapers" -> "wallpaper"
            else -> category.lowercase()
        }
        val items = allCollections[key] ?: emptyList()
        android.util.Log.d("CollectionDebug", "getDiscoveryItems for category='$category' -> key='$key', found ${items.size} items")
        items
    }

    suspend fun getAllDiscoveryCollections(): Map<String, List<CollectionItem>> = withContext(Dispatchers.IO) {
        loadCollectionsFromAssets()
    }

    fun getDownloadedItemsFlow(category: String): Flow<List<DownloadedCollectionItem>> {
        val normalizedCategory = normalizeCategoryName(category)
        return downloadedDao.getDownloadedByCategoryFlow(normalizedCategory)
    }

    suspend fun getDownloadedItems(category: String): List<DownloadedCollectionItem> = withContext(Dispatchers.IO) {
        val normalizedCategory = normalizeCategoryName(category)
        downloadedDao.getDownloadedByCategory(normalizedCategory)
    }

    suspend fun getAllDownloadedItems(): List<DownloadedCollectionItem> = withContext(Dispatchers.IO) {
        downloadedDao.getAllDownloaded()
    }

    suspend fun markAsDownloaded(
        id: String,
        name: String,
        category: String,
        targetPath: String,
        previewPath: String,
        downloads: Long = 7654321,
        rawType: String = "",
        extra: String? = null
    ) = withContext(Dispatchers.IO) {
        val normalizedCategory = normalizeCategoryName(category)
        val item = DownloadedCollectionItem(
            id = id,
            name = name,
            category = normalizedCategory,
            targetPath = targetPath,
            previewPath = previewPath,
            downloads = downloads,
            rawType = rawType,
            extra = extra,
            appliedTimestamp = System.currentTimeMillis()
        )
        downloadedDao.insertDownloaded(item)
    }

    private fun normalizeCategoryName(category: String): String {
        return when (category.trim().lowercase()) {
            "theme" -> "Theme"
            "icons", "icon" -> "Icons"
            "control center", "control" -> "Control center"
            "widget", "widgets" -> "Widget"
            "wallpaper", "wallpapers" -> "Wallpaper"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    private fun loadCollectionsFromAssets(): Map<String, List<CollectionItem>> {
        cachedCollections?.let { return it }

        val resultMap = mutableMapOf<String, List<CollectionItem>>()
        try {
            val jsonStr = FileUtils.loadJsonFromAsset(context, "assets_collection/collections.json")
            val root = JSONObject(jsonStr)

            val keys = listOf("theme", "icons", "control_center", "widget", "wallpaper")
            for (key in keys) {
                val array = root.optJSONArray(key) ?: continue
                val items = mutableListOf<CollectionItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        CollectionItem(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            category = obj.optString("category"),
                            targetPath = obj.optString("targetPath"),
                            previewPath = obj.optString("previewPath"),
                            downloads = obj.optLong("downloads", 7654321),
                            rawType = obj.optString("rawType", ""),
                            extra = if (obj.has("extra")) obj.optString("extra") else null
                        )
                    )
                }
                resultMap[key] = items
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedCollections = resultMap
        return resultMap
    }

    companion object {
        val TABS = listOf("Theme", "Icons", "Control center", "Widget", "Wallpaper")
    }
}
