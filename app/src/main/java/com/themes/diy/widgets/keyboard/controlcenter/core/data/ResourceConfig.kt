package com.themes.diy.widgets.keyboard.controlcenter.core.data

import android.content.Context

object ResourceConfig {
    const val ASSET_BASE_URL = "https://h03-themeapp-assets.pages.dev"
    const val CONTROL_CENTER = "assets_control_center"


    private var themePathToFolderMap: Map<String, String>? = null

    @Synchronized
    fun getThemeFolderByPath(context: Context, themePath: String): String {
        if (themePath.startsWith("theme_decorates/")) {
            return themePath.removePrefix("theme_decorates/")
        }
        return themePath
    }

    fun getKeyboardFolderByName(themeName: String, fallbackPath: String): String {
        val originalPath = when (themeName) {
            "Blue Sky" -> "Aesthetic/blue-sky"
            "Buffter Violet" -> "Aesthetic/buffter-violet"
            "Purple Galaxy" -> "Aesthetic/purple-galaxy"
            "Purple Vortex" -> "Aesthetic/purple-vortex"
            "Sparkle Horse" -> "Trending/sparkle-horse"
            "Tropical Town" -> "Aesthetic/tropical-town"
            "Universe Fun" -> "Aesthetic/universe fun"
            "Autumn" -> "Animal/autumn"
            "Butterflies" -> "Animal/butterflies"
            "Cat & Drops" -> "Animal/cat-&-drops"
            "Cat Family" -> "Animal/cat-family"
            "Crystal Bubbles" -> "Animal/crystal-bubbles"
            "Fancy Horse" -> "Animal/fancy-horse"
            "Hungry Squirrel" -> "Animal/hungry-squirrel"
            "Lion King" -> "Animal/lion-king"
            "Christmas Season" -> "Christmas/Christmas season"
            "Cold Day" -> "Christmas/cold-day"
            "Merry Christmas" -> "Christmas/merry-christmas"
            "Santa Claus" -> "Christmas/santa-claus"
            "X-mas" -> "Christmas/x-mas"
            "Boom" -> "Fun/boom"
            "Free Day" -> "Fun/free-day"
            "Fun2" -> "Fun/fun2"
            "Fun3" -> "Fun/fun3"
            "Gaming" -> "Fun/gaming"
            "Night Mood" -> "Trending/night-mood"
            "Orange Bubbles" -> "Fun/orange-bubbles"
            "Presents" -> "Fun/presents"
            "Red Girl" -> "Fun/red-girl"
            "Skeleton" -> "Fun/skeleton"
            "Treasure" -> "Fun/treasure"
            "Diamond" -> "Glitter/diamond"
            "Diamond Heart" -> "Glitter/diamond-heart"
            "Fancy Lights" -> "Trending/fancy-lights"
            "Fire Heart" -> "Glitter/fire-heart"
            "Loyalty" -> "Glitter/loyalty"
            "Modern Life" -> "Glitter/modern-life"
            "Sparkling Bubbles" -> "Glitter/sparkling-bubbles"
            "Sweet Beats" -> "Glitter/sweet-beats"
            "Blossoms" -> "Holiday/blossoms"
            "Halloween" -> "Holiday/halloween"
            "Love" -> "Holiday/love"
            "Summer" -> "Holiday/summer"
            "Summer of Love" -> "Holiday/summer-of-love"
            "Wedding Day" -> "Holiday/wedding-day"
            "Winter Bubble" -> "Holiday/winter bubble"
            "Astronaut" -> "Hot/astronaut"
            "Buffter" -> "Hot/Buffter"
            "Golden Season" -> "Hot/golden-season"
            "Horse Full Color" -> "Hot/Horse Full Color"
            "Noel" -> "Neon/Noel"
            "Sakura" -> "Neon/Sakura"
            "TreasureNeon" -> "Neon/TreasureNeon"
            "Cute Cats" -> "Kawaii/cute-cats"
            "Cute Lions" -> "Kawaii/cute-lions"
            "Cute Sheep" -> "Kawaii/cute-sheep"
            "Dog Tracks" -> "Kawaii/dog-tracks"
            "Flying Pig" -> "Kawaii/flying-pig"
            "Little Girl" -> "Trending/little-girl"
            "Mars" -> "Kawaii/mars"
            "Penguin Family" -> "Kawaii/penguin-family"
            "Pinky Bear" -> "Kawaii/pinky-bear"
            "Purple Dog" -> "Kawaii/purple-dog"
            "Sweets" -> "Kawaii/sweets"
            "Under The Sea" -> "Trending/under-the-sea"
            "Coconut" -> "Nature/coconut"
            "Desert" -> "Nature/desert"
            "Flower" -> "Nature/flower"
            "Wood" -> "Nature/wood"
            "Glitter Feathers" -> "Neon/glitter-feathers"
            "Green Microchip" -> "Neon/green-microchip"
            "Violet City" -> "Neon/violet-city"
            "Balloons" -> "Romantic/balloons"
            "Crystal Heart" -> "Romantic/crystal-heart"
            "Galaxy" -> "Trending/galaxy"
            "Glass Love" -> "Romantic/glass love"
            "Glitter Keyboard" -> "Romantic/glitter-keyboard"
            "Happy Valentine" -> "Romantic/happy-valentine"
            "Love and Letters" -> "Romantic/love and letters"
            "Pink Rose" -> "Romantic/pink-rose"
            "Bubble Soap" -> "Simple/bubble-soap"
            "Christmas Tree" -> "Simple/christmas-tree"
            "Crypto" -> "Simple/crypto"
            "Cute Pet" -> "Simple/Cute pet"
            "Fire Horse" -> "Simple/fire-horse"
            "Go Green" -> "Simple/go-green"
            "Heart and Soul" -> "Simple/heart-and-soul"
            "Ocean Eyes" -> "Simple/ocean-eyes"
            "Simple Bubble" -> "Simple/simple bubble"
            "Smoke of Love" -> "Simple/smoke of love"
            "Tech Keyboard" -> "Simple/tech-keyboard"
            "Feel The Beats" -> "Trending/feel-the-beats"
            "Planet" -> "Trending/planet"
            "Secret Garden" -> "Trending/secret-garden"
            "Classic Light" -> "Business/Classic Light"
            "Edge Blue" -> "Business/Edge Blue"
            "Elegant Marble" -> "Business/Elegant Marble"
            "Glossy Glass" -> "Business/Glossy Glass"
            "Material Light" -> "Business/Material Light"
            "Minimal Dark Pink" -> "Business/Minimal Dark Pink"
            "Minimal Light Blue" -> "Business/Minimal Light Blue"
            "Minimal Light Red" -> "Business/Minimal Light Red"
            "Neon" -> "Business/Neon"
            "Pitch" -> "Business/Pitch"
            "Blur Glass" -> "Dark Mode/Blur Glass"
            "Classic Dark" -> "Dark Mode/Classic Dark"
            "Forest" -> "Dark Mode/Forest"
            "Glass Elegance" -> "Dark Mode/Glass Elegance"
            "Highlight Pink" -> "Dark Mode/Highlight Pink"
            "Minimal Dark" -> "Dark Mode/Minimal Dark"
            "Neptune Blue" -> "Dark Mode/Neptune Blue"
            "Snowy Sky" -> "Dark Mode/Snowy Sky"
            "Titanium Luster" -> "Dark Mode/Titanium Luster"
            "Vivid Lime" -> "Dark Mode/Vivid Lime"
            else -> null
        }
        if (originalPath != null) return originalPath
        var clean = fallbackPath
        if (clean.startsWith("category/")) {
            clean = clean.removePrefix("category/")
        }
        return clean
    }

    /**
     * 1. Keyboard Theme
     */
    fun getKeyboardPreviewUrl(themeName: String, fallbackPath: String): String {
        val folderTheme = getKeyboardFolderByName(themeName, fallbackPath)
        return "$ASSET_BASE_URL/assets_keyboard/themes/$folderTheme/preview.png"
    }

    fun getKeyboardBackgroundUrl(themeName: String, fallbackPath: String): String {
        val folderTheme = getKeyboardFolderByName(themeName, fallbackPath)
        return "$ASSET_BASE_URL/assets_keyboard/themes/$folderTheme/keyboard_background.png"
    }

    fun getKeyboardKeyUrl(themePath: String): String {
        val clean = themePath.removePrefix("/")
        return "$ASSET_BASE_URL/assets_keyboard/themes/$clean/key/key.png"
    }

    fun getKeyboardPopupBgUrl(themePath: String, bgName: String = "popup_background.png"): String {
        val clean = themePath.removePrefix("/")
        return "$ASSET_BASE_URL/assets_keyboard/themes/$clean/$bgName"
    }

    fun getLenGifUrl(lenName: String): String = "$ASSET_BASE_URL/assets_theme/lens/$lenName/len.gif"

    fun getKeyboardStickerUrl(assetPath: String): String = "$ASSET_BASE_URL/${assetPath.removePrefix("/")}"

    /**
     * 2. Theme Previews & Wallpapers
     */
    fun getThemePreviewUrl(themeFolder: String): String {
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$clean/bg_preview.png"
        }
        return "$ASSET_BASE_URL/assets_theme/$clean/bg_preview.png"
    }

    fun getThemeOriginalUrl(themeFolder: String, deviceType: String? = null): String {
        val suffix = when (deviceType?.lowercase()) {
            "tablet" -> "_tablet"
            "fold" -> "_fold"
            else -> ""
        }
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$clean/bg_preview$suffix.png"
        }
        return "$ASSET_BASE_URL/assets_theme/$clean/bg_preview_original$suffix.png"
    }

    fun getWallpaperPreviewUrl(themeFolder: String): String {
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$clean"
        }
        return "$ASSET_BASE_URL/assets_theme/$clean/wallpapers/bg_wallpaper.png"
    }

    fun getWallpaperThumbnailUrl(themeFolder: String, imageName: String = ""): String {
        if (themeFolder.isEmpty()) return ""
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$clean"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName.removeSuffix(".png")
        return "$ASSET_BASE_URL/assets_theme/$clean/wallpapers/$name.png"
    }

    fun getWallpaperFullUrl(themeFolder: String, imageName: String = "bg_wallpaper"): String {
        if (themeFolder.isEmpty()) return ""
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            if (clean.endsWith(".png") || clean.endsWith(".jpg")) {
                return "$ASSET_BASE_URL/$clean"
            }
            return "$ASSET_BASE_URL/$clean/wallpaper.png"
        }
        val cleanImageName = if (imageName.endsWith(".png")) imageName else "$imageName.png"
        return "$ASSET_BASE_URL/assets_theme/$clean/wallpapers/$cleanImageName"
    }

    /**
     * 3. DIY Editor & Templates
     */
    fun getExclusiveFontUrl(fontName: String): String {
        if (fontName.isEmpty()) return ""
        val clean = fontName.removeSuffix(".ttf")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/font/$clean.ttf"
    }

    fun getDiyStickerUrl(categoryFolder: String, imageName: String): String {
        if (categoryFolder.isEmpty() || imageName.isEmpty()) return ""
        val cleanCat = categoryFolder.removePrefix("/")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/stickers/$cleanCat/$imageName"
    }

    fun getDiyBackgroundUrl(categoryFolder: String, imageName: String): String {
        if (categoryFolder.isEmpty() || imageName.isEmpty()) return ""
        val cleanCat = categoryFolder.removePrefix("/")
        val cleanImg = if (imageName.endsWith(".png")) imageName else "$imageName.png"
        return "$ASSET_BASE_URL/assets_wallpaper/templates/background/$cleanCat/$cleanImg"
    }

    fun getStickerUrl(category: String, imageName: String): String = getDiyStickerUrl(category, imageName)

    fun getBackgroundCanvasUrl(category: String, imageName: String): String = getDiyBackgroundUrl(category, imageName)

    fun getDiyConfigUrl(templateFolder: String): String {
        if (templateFolder.isEmpty()) return ""
        val clean = templateFolder.removePrefix("/")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/designs/$clean/config.json"
    }

    fun getDiyPreviewUrl(templateFolder: String, isAnimated: Boolean = false): String {
        if (templateFolder.isEmpty()) return ""
        val clean = templateFolder.removePrefix("/")
        val suffix = if (isAnimated) "preview.gif" else "preview.png"
        return "$ASSET_BASE_URL/assets_wallpaper/templates/designs/$clean/$suffix"
    }

    fun getDiyBackgroundLayerUrl(templateFolder: String): String {
        if (templateFolder.isEmpty()) return ""
        val clean = templateFolder.removePrefix("/")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/designs/$clean/bg_layer.png"
    }

    /**
     * 4. Charging Animations
     */
    fun getChargingPreviewUrl(animFolder: String): String {
        if (animFolder.isEmpty()) return ""
        val clean = animFolder.removePrefix("charging/").removePrefix("assets_charging/charging/").removePrefix("feature_charging/charging/")
        return "$ASSET_BASE_URL/assets_charging/charging/$clean/bg_preview.png"
    }

    fun getChargingVideoUrl(animFolder: String, isFold: Boolean = false): String {
        if (animFolder.isEmpty()) return ""
        val clean = animFolder.removePrefix("charging/").removePrefix("assets_charging/charging/").removePrefix("feature_charging/charging/")
        val fileSuffix = if (isFold) "video_fold.mp4" else "video.mp4"
        return "$ASSET_BASE_URL/assets_charging/charging/$clean/$fileSuffix"
    }

    /**
     * 5. App Icons
     */
    fun getIconPackPreviewUrl(themeFolder: String): String {
        if (themeFolder.isEmpty()) return ""
        val clean = themeFolder.removePrefix("/")
        if (clean.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$clean/bg_icon.png"
        }
        return "$ASSET_BASE_URL/assets_theme/$clean/icons/bg_icon.png"
    }

    fun getIconCategoryPreviewUrl(folder: String): String = getIconPackPreviewUrl(folder)

    fun getSingleIconUrl(themeFolder: String, iconId: String): String {
        if (themeFolder.isEmpty() || iconId.isEmpty()) return ""
        val cleanFolder = themeFolder.removePrefix("/")
        val cleanIconId = iconId.removePrefix("ic_").removeSuffix(".png").lowercase()
        if (cleanFolder.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$cleanFolder/ic_$cleanIconId.png"
        }
        return "$ASSET_BASE_URL/assets_theme/$cleanFolder/icons/ic_$cleanIconId.png"
    }

    fun getLauncherIconUrl(context: Context, themeFolder: String, iconId: String): String = getSingleIconUrl(themeFolder, iconId)

    /**
     * 6. System Widgets
     */
    fun getWidgetPreviewUrl(themeFolder: String, widgetSize: String = "medium"): String {
        val cleanFolder = themeFolder.removePrefix("/")
        val cleanSize = widgetSize.lowercase().removeSuffix(".png")
        if (cleanFolder.startsWith("assets_collection/")) {
            return "$ASSET_BASE_URL/$cleanFolder/bg_$cleanSize.png"
        }
        return "$ASSET_BASE_URL/assets_theme/$cleanFolder/widgets/bg_$cleanSize.png"
    }

    fun getWidgetPreviewUrl(context: Context, themeFolder: String): String = getThemePreviewUrl(themeFolder)

    fun getWidgetConfigUrl(templateFolder: String): String {
        val clean = templateFolder.removePrefix("/")
        return "$ASSET_BASE_URL/assets_wallpaper/templates/designs/$clean/config.json"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$ASSET_BASE_URL/assets_theme/$folder/widgets/$widgetType/$fileName"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, folderChild: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || folderChild.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$ASSET_BASE_URL/assets_theme/$folder/widgets/$widgetType/$folderChild/$fileName"
    }
}
