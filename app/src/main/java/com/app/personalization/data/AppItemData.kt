package com.app.personalization.data

data class DefaultAppInfo(
    val id: String,
    val name: String,
    val packageName: String
)

object AppItemData {
    val APPS = listOf(
        DefaultAppInfo("facebook", "Facebook", "com.facebook.katana"),
        DefaultAppInfo("instagram", "Instagram", "com.instagram.android"),
        DefaultAppInfo("tiktok", "TikTok", "com.zhiliaoapp.musically"),
        DefaultAppInfo("youtube", "YouTube", "com.google.android.youtube"),
        DefaultAppInfo("messenger", "Messenger", "com.facebook.orca"),
        DefaultAppInfo("gmail", "Gmail", "com.google.android.gm"),
        DefaultAppInfo("chrome", "Chrome", "com.android.chrome"),
        DefaultAppInfo("phone", "Phone", "com.android.phone"),
        DefaultAppInfo("camera", "Camera", "com.android.camera"),
        DefaultAppInfo("settings", "Settings", "com.android.settings"),
        DefaultAppInfo("whatsapp", "WhatsApp", "com.whatsapp"),
        DefaultAppInfo("telegram", "Telegram", "org.telegram.messenger"),
        DefaultAppInfo("twitter", "Twitter", "com.twitter.android"),
        DefaultAppInfo("snapchat", "Snapchat", "com.snapchat.android"),
        DefaultAppInfo("spotify", "Spotify", "com.spotify.music"),
        DefaultAppInfo("maps", "Maps", "com.google.android.apps.maps")
    )
}
