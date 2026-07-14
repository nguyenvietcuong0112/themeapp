package com.app.personalization.data

/**
 * Bộ phân giải đường dẫn CDN (CDN Path Resolver) để sinh URL động cho các tài nguyên
 * như hình nền, biểu tượng, tiện ích và các tài nguyên DIY tự thiết kế.
 */
object CdnPathResolver {
    
    // Domain CDN cấu hình động (mặc định trỏ về domain chính của hệ thống)
    var cdnDomain: String = com.app.personalization.cscthemeapp.widget.model.model.Constants.S3_URL

    /**
     * Lấy URL ảnh xem thử bộ Theme.
     * Cấu trúc: [CDN_DOMAIN]/[theme_folder]/bg_preview.png
     */
    fun getThemePreviewUrl(themeFolder: String): String {
        return "${cdnDomain.removeSuffix("/")}/${themeFolder.removePrefix("/")}/bg_preview.png"
    }

    /**
     * Lấy URL ảnh gốc chất lượng cao của bộ Theme.
     * Cấu trúc: [CDN_DOMAIN]/[theme_folder]/bg_preview_original.png
     * Hỗ trợ hậu tố cho máy tính bảng (_tablet) hoặc màn hình gập (_fold).
     */
    fun getThemeOriginalUrl(themeFolder: String, deviceType: String? = null): String {
        val suffix = when (deviceType?.lowercase()) {
            "tablet" -> "_tablet"
            "fold" -> "_fold"
            else -> ""
        }
        return "${cdnDomain.removeSuffix("/")}/${themeFolder.removePrefix("/")}/bg_preview_original$suffix.png"
    }

    /**
     * Lấy URL ảnh nền xem trước (Wallpaper Preview).
     * Cấu trúc: [CDN_DOMAIN]/previews/wallpapers/[theme_folder]/bg_wallpaper.png
     */
    fun getWallpaperPreviewUrl(themeFolder: String): String {
        return "${cdnDomain.removeSuffix("/")}/previews/wallpapers/${themeFolder.removePrefix("/")}/bg_wallpaper.png"
    }

    /**
     * Lấy URL ảnh nền gốc chất lượng cao (Full-HD).
     * Cấu trúc: [CDN_DOMAIN]/[theme_folder]/wallpapers/[image_name].png
     */
    fun getWallpaperFullUrl(themeFolder: String, imageName: String): String {
        val cleanImageName = if (imageName.endsWith(".png")) imageName else "$imageName.png"
        return "${cdnDomain.removeSuffix("/")}/${themeFolder.removePrefix("/")}/wallpapers/$cleanImageName"
    }

    /**
     * Lấy URL ảnh xem trước của bộ Icon (Icon Pack Preview).
     * Cấu trúc: [CDN_DOMAIN]/previews/icons/[theme_folder]/bg_icon.png
     */
    fun getIconPackPreviewUrl(themeFolder: String): String {
        return "${cdnDomain.removeSuffix("/")}/previews/icons/${themeFolder.removePrefix("/")}/bg_icon.png"
    }

    /**
     * Lấy URL ảnh lẻ của từng app (Facebook, Tiktok, Chrome...) trong bộ Icon.
     * Cấu trúc: [CDN_DOMAIN]/[theme_folder]/icons/ic_[icon_id].png
     */
    fun getSingleIconUrl(themeFolder: String, iconId: String): String {
        val cleanIconId = iconId.removePrefix("ic_").removeSuffix(".png")
        return "${cdnDomain.removeSuffix("/")}/${themeFolder.removePrefix("/")}/icons/ic_$cleanIconId.png"
    }

    /**
     * Lấy URL file cấu hình JSON của Widget Template.
     * Cấu trúc: [CDN_DOMAIN]/templates/[template_folder]/config.json
     */
    fun getWidgetConfigUrl(templateFolder: String): String {
        return "${cdnDomain.removeSuffix("/")}/templates/${templateFolder.removePrefix("/")}/config.json"
    }

    /**
     * Lấy URL ảnh xem trước của Widget.
     * Cấu trúc: [CDN_DOMAIN]/previews/widgets/{theme_folder}/{widget_size}.png
     */
    fun getWidgetPreviewUrl(themeFolder: String, widgetSize: String = "medium"): String {
        val cleanFolder = themeFolder.removePrefix("/")
        val cleanSize = widgetSize.lowercase().removeSuffix(".png")
        return "${cdnDomain.removeSuffix("/")}/previews/widgets/$cleanFolder/$cleanSize.png"
    }

    /**
     * Lấy URL ảnh nền họa tiết dùng cho thiết kế DIY.
     * Cấu trúc: [CDN_DOMAIN]/templates/background/[category_folder]/[image_name].png
     */
    fun getDiyBackgroundUrl(categoryFolder: String, imageName: String): String {
        val cleanImageName = if (imageName.endsWith(".png")) imageName else "$imageName.png"
        return "${cdnDomain.removeSuffix("/")}/templates/background/${categoryFolder.removePrefix("/")}/$cleanImageName"
    }

    /**
     * Lấy URL nhãn dán SVG/PNG dùng cho thiết kế DIY.
     * Cấu trúc: [CDN_DOMAIN]/templates/stickers/[category_folder]/[image_name]
     */
    fun getDiyStickerUrl(categoryFolder: String, imageName: String): String {
        return "${cdnDomain.removeSuffix("/")}/templates/stickers/${categoryFolder.removePrefix("/")}/$imageName"
    }
}
