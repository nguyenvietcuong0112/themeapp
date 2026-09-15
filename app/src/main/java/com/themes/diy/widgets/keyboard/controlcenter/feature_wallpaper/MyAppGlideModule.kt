package com.themes.diy.widgets.keyboard.controlcenter.feature_wallpaper

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.WARN)

        // 300 MB Disk Cache for CDN images
        val diskCacheSizeBytes = 1024 * 1024 * 300L
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, "themeapp_image_cache", diskCacheSizeBytes))

        // 50 MB Memory Cache
        val memoryCacheSizeBytes = 1024 * 1024 * 50L
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))

        // Optimized default request options: RGB_565 (faster decode, 50% less RAM), cache both original and resized
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        )
    }
}
