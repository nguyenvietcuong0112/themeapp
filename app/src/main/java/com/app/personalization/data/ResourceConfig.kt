package com.app.personalization.data

import android.content.Context
import com.app.personalization.cscthemeapp.widget.model.model.Constants

object ResourceConfig {
    const val CDN_DOMAIN = Constants.S3_URL
    const val S3_URL = Constants.S3_URL

    private var themePathToFolderMap: Map<String, String>? = null

    @Synchronized
    fun getThemeFolderByPath(context: Context, themePath: String): String {
        if (themePath.startsWith("theme_")) return themePath
        val cleanPath = if (themePath.startsWith("theme_decorates/")) {
            themePath.removePrefix("theme_decorates/")
        } else {
            themePath
        }
        
        var map = themePathToFolderMap
        if (map == null) {
            val newMap = mutableMapOf<String, String>()
            try {
                val jsonStr = FileUtils.loadJsonFromAsset(context, "themes/json/theme_data_decorate.json")
                val decorateCategories = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .decodeFromString<List<DecorateCategory>>(jsonStr)
                
                var globalIndex = 1
                for (decorCat in decorateCategories) {
                    for (decorTheme in decorCat.themes) {
                        newMap[decorTheme.themePath] = "theme_$globalIndex"
                        globalIndex++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            map = newMap
            themePathToFolderMap = map
        }
        return map[cleanPath] ?: "theme_1"
    }

    /**
     * 1. Keyboard Theme
     */
    fun getKeyboardPreviewUrl(folderTheme: String): String {
        if (folderTheme.isEmpty()) return ""
        return "$S3_URL/widgetkeyboard/theme_decorates/$folderTheme/preview.png"
    }

    fun getKeyboardBackgroundUrl(folderTheme: String): String {
        if (folderTheme.isEmpty()) return ""
        return "$S3_URL/widgetkeyboard/theme_decorates/$folderTheme/keyboard_background.png"
    }

    /**
     * 2. Wallpapers
     */
    fun getWallpaperThumbnailUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty()) return ""
        if (themeFolder.startsWith("theme_")) {
            return "$S3_URL/previews/wallpapers/$themeFolder/bg_wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName
        return "$S3_URL/previews/wallpapers/$themeFolder/$name.png"
    }

    fun getWallpaperFullUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty()) return ""
        if (themeFolder.startsWith("theme_")) {
            return "$S3_URL/$themeFolder/wallpapers/bg_wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName
        return "$S3_URL/$themeFolder/wallpapers/$name.png"
    }

    /**
     * 3. DIY Editor
     */
    fun getExclusiveFontUrl(fontName: String): String {
        if (fontName.isEmpty()) return ""
        return "$S3_URL/templates/font/$fontName.ttf"
    }

    fun getStickerUrl(category: String, imageName: String): String {
        if (category.isEmpty() || imageName.isEmpty()) return ""
        return "$S3_URL/templates/$category/$imageName"
    }

    fun getBackgroundCanvasUrl(category: String, imageName: String): String {
        if (category.isEmpty() || imageName.isEmpty()) return ""
        return "$S3_URL/templates/$category/$imageName"
    }

    /**
     * DIY Templates
     */
    fun getDiyConfigUrl(templateFolder: String): String {
        if (templateFolder.isEmpty()) return ""
        return "$S3_URL/templates/$templateFolder/config.json"
    }

    fun getDiyPreviewUrl(templateFolder: String, isAnimated: Boolean = false): String {
        if (templateFolder.isEmpty()) return ""
        val suffix = if (isAnimated) "preview.gif" else "preview.png"
        return "$S3_URL/templates/$templateFolder/$suffix"
    }

    fun getDiyBackgroundLayerUrl(templateFolder: String): String {
        if (templateFolder.isEmpty()) return ""
        return "$S3_URL/templates/$templateFolder/bg_layer.png"
    }

    /**
     * 4. Charging Anim
     */
    fun getChargingPreviewUrl(animFolder: String): String {
        if (animFolder.isEmpty()) return ""
        val clean = if (animFolder.startsWith("charging/")) animFolder else "charging/$animFolder"
        return "$S3_URL/$clean/bg_preview.png"
    }

    fun getChargingVideoUrl(animFolder: String, isFold: Boolean = false): String {
        if (animFolder.isEmpty()) return ""
        val clean = if (animFolder.startsWith("charging/")) animFolder else "charging/$animFolder"
        val fileSuffix = if (isFold) "video_fold.mp4" else "video.mp4"
        return "$S3_URL/$clean/$fileSuffix"
    }

    /**
     * 5. App Icons
     */
    fun getLauncherIconUrl(context: Context, themeFolder: String, iconId: String): String {
        if (themeFolder.isEmpty() || iconId.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        val iconClean = iconId.lowercase()
        return "$S3_URL/$folder/icons/ic_$iconClean.png"
    }

    fun getIconCategoryPreviewUrl(folder: String): String {
        if (folder.isEmpty()) return ""
        return "$S3_URL/previews/icons/$folder/bg_icon.png"
    }

    /**
     * 6. System Widgets
     */
    fun getWidgetPreviewUrl(context: Context, themeFolder: String): String {
        if (themeFolder.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/$folder/bg_preview.png"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/$folder/widgets/$widgetType/$fileName"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, folderChild: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || folderChild.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/$folder/widgets/$widgetType/$folderChild/$fileName"
    }
}
