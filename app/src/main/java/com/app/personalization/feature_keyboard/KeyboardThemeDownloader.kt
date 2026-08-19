package com.app.personalization.feature_keyboard

import android.content.Context
import com.app.personalization.feature_keyboard.data.entity.KeyboardTheme
import com.app.personalization.core.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object KeyboardThemeDownloader {

    fun isDownloaded(context: Context, themeId: String): Boolean {
        val prefs = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("keyboard_theme_downloaded_$themeId", false)
    }

    suspend fun downloadTheme(
        context: Context,
        theme: KeyboardTheme,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val themeName = theme.path
        if (themeName.isEmpty()) return@withContext false

        val localThemeDir = File(context.filesDir, "keyboard_themes/$themeName")
        if (!localThemeDir.exists()) {
            localThemeDir.mkdirs()
        }
        val keyDir = File(localThemeDir, "key")
        if (!keyDir.exists()) {
            keyDir.mkdirs()
        }

        // List of files to download
        val files = listOf(
            "config.json" to File(localThemeDir, "config.json"),
            "keyboard_background.png" to File(localThemeDir, "keyboard_background.png"),
            "popup_background.png" to File(localThemeDir, "popup_background.png"),
            "key/key.png" to File(keyDir, "key.png"),
            "key/space.png" to File(keyDir, "space.png"),
            "key/return.png" to File(keyDir, "return.png"),
            "key/shift.png" to File(keyDir, "shift.png"),
            "key/backspace.png" to File(keyDir, "backspace.png"),
            "key/emoji.png" to File(keyDir, "emoji.png")
        )

        val folderTheme = ResourceConfig.getKeyboardFolderByName(theme.name, theme.path)
        val prefix = "assets_keyboard/themes/${theme.path.removePrefix("theme_decorates/").removePrefix("assets_keyboard/themes/").removePrefix("feature_keyboard/themes/")}"

        var downloadedCount = 0
        for ((subPath, destFile) in files) {
            // 1. Try assets_keyboard/themes/
            var success = copyFromAssets(context, "assets_keyboard/themes/$folderTheme/$subPath", destFile)
            if (!success) {
                // 2. Try with prefix
                success = copyFromAssets(context, "$prefix/$subPath", destFile)
            }
            if (!success) {
                // 3. Try with assets_theme/
                success = copyFromAssets(context, "assets_theme/${theme.path}/$subPath", destFile)
            }

            if (success) {
                downloadedCount++
            } else {
                if (subPath == "config.json" && !destFile.exists()) {
                    return@withContext false
                }
            }
            val progress = ((downloadedCount.toFloat() / files.size.toFloat()) * 100).toInt()
            withContext(Dispatchers.Main) {
                onProgress(progress)
            }
        }

        // Save status in SharedPreferences
        val prefs = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("keyboard_theme_downloaded_${theme.id}", true).apply()

        return@withContext true
    }

    private fun copyFromAssets(context: Context, assetPath: String, destFile: File): Boolean {
        return try {
            context.assets.open(assetPath).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
