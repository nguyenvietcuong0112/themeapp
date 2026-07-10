package com.app.personalization.data

object ResourceConfig {
    const val CDN_DOMAIN = "https://cuongnguyen.themeapp"

    /**
     * 1. Keyboard Theme
     */
    fun getKeyboardPreviewUrl(folderTheme: String): String {
        if (folderTheme.isEmpty()) return ""
        return "$CDN_DOMAIN/widgetkeyboard/theme_decorates/$folderTheme/preview.png"
    }

    fun getKeyboardBackgroundUrl(folderTheme: String): String {
        if (folderTheme.isEmpty()) return ""
        return "$CDN_DOMAIN/widgetkeyboard/theme_decorates/$folderTheme/keyboard_background.png"
    }

    /**
     * 2. Wallpapers
     */
    fun getWallpaperThumbnailUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty() || imageName.isEmpty()) return ""
        return "$CDN_DOMAIN/previews/wallpapers/$themeFolder/$imageName.png"
    }

    fun getWallpaperFullUrl(themeFolder: String, imageName: String): String {
        if (themeFolder.isEmpty() || imageName.isEmpty()) return ""
        return "$CDN_DOMAIN/$themeFolder/wallpapers/$imageName.png"
    }

    /**
     * 3. DIY Editor
     */
    fun getExclusiveFontUrl(fontName: String): String {
        if (fontName.isEmpty()) return ""
        return "$CDN_DOMAIN/templates/font/$fontName.ttf"
    }

    fun getStickerUrl(stickerPath: String): String {
        if (stickerPath.isEmpty()) return ""
        val cleanPath = stickerPath.removePrefix("/")
        return "$CDN_DOMAIN/templates/$cleanPath"
    }

    fun getBackgroundCanvasUrl(bgPath: String): String {
        if (bgPath.isEmpty()) return ""
        val cleanPath = bgPath.removePrefix("/")
        return "$CDN_DOMAIN/templates/$cleanPath"
    }

    /**
     * 4. Charging Anim
     */
    fun getChargingVideoUrl(animFolder: String, isFold: Boolean = false): String {
        if (animFolder.isEmpty()) return ""
        val fileSuffix = if (isFold) "video_fold.mp4" else "video.mp4"
        return "$CDN_DOMAIN/$animFolder/$fileSuffix"
    }

    /**
     * 5. App Icons
     */
    fun getLauncherIconUrl(themeFolder: String, iconId: String): String {
        if (themeFolder.isEmpty() || iconId.isEmpty()) return ""
        return "$CDN_DOMAIN/$themeFolder/icons/ic_$iconId.png"
    }

    /**
     * 6. System Widgets
     */
    fun getWidgetPreviewUrl(themeFolder: String): String {
        if (themeFolder.isEmpty()) return ""
        return "$CDN_DOMAIN/$themeFolder/bg_preview.png"
    }

    fun getWidgetComponentUrl(themeFolder: String, widgetType: String, subFolder: String, imageName: String): String {
        if (themeFolder.isEmpty() || widgetType.isEmpty() || subFolder.isEmpty() || imageName.isEmpty()) return ""
        return "$CDN_DOMAIN/$themeFolder/widgets/$widgetType/$subFolder/$imageName.png"
    }
}
