package com.app.personalization.data

import android.content.Context
import com.app.personalization.cscthemeapp.widget.model.model.Constants

object ResourceConfig {
    const val CDN_DOMAIN = Constants.S3_URL
    const val S3_URL = Constants.S3_URL

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
        return "$S3_URL/widgetkeyboard/theme_decorates/$folderTheme/preview.png"
    }

    fun getKeyboardBackgroundUrl(themeName: String, fallbackPath: String): String {
        val folderTheme = getKeyboardFolderByName(themeName, fallbackPath)
        return "$S3_URL/widgetkeyboard/theme_decorates/$folderTheme/keyboard_background.png"
    }

    /**
     * 2. Wallpapers
     */
    fun getWallpaperThumbnailUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty()) return ""
        if (themeFolder.startsWith("theme_")) {
            return "$S3_URL/themes/previews/wallpapers/$themeFolder/bg_wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName
        return "$S3_URL/themes/previews/wallpapers/$themeFolder/$name.png"
    }

    fun getWallpaperFullUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty()) return ""
        if (themeFolder.startsWith("theme_")) {
            return "$S3_URL/themes/$themeFolder/wallpapers/bg_wallpaper.png"
        }
        val name = if (imageName.isEmpty()) "bg_wallpaper" else imageName
        return "$S3_URL/themes/$themeFolder/wallpapers/$name.png"
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
        return "$S3_URL/templates/designs/$templateFolder/config.json"
    }

    fun getDiyPreviewUrl(templateFolder: String, isAnimated: Boolean = false): String {
        if (templateFolder.isEmpty()) return ""
        val suffix = if (isAnimated) "preview.gif" else "preview.png"
        return "$S3_URL/templates/designs/$templateFolder/$suffix"
    }

    fun getDiyBackgroundLayerUrl(templateFolder: String): String {
        if (templateFolder.isEmpty()) return ""
        return "$S3_URL/templates/designs/$templateFolder/bg_layer.png"
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
        return "$S3_URL/themes/$folder/icons/ic_$iconClean.png"
    }

    fun getIconCategoryPreviewUrl(folder: String): String {
        if (folder.isEmpty()) return ""
        return "$S3_URL/themes/previews/icons/$folder/bg_icon.png"
    }

    /**
     * 6. System Widgets
     */
    fun getWidgetPreviewUrl(context: Context, themeFolder: String): String {
        if (themeFolder.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/themes/$folder/bg_preview.png"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/themes/$folder/widgets/$widgetType/$fileName"
    }

    fun getWidgetComponentUrl(context: Context, themeFolder: String, widgetType: String, folderChild: String, fileName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || folderChild.isEmpty() || fileName.isEmpty()) return ""
        val folder = getThemeFolderByPath(context, themeFolder)
        return "$S3_URL/themes/$folder/widgets/$widgetType/$folderChild/$fileName"
    }
}
