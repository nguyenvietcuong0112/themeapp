package com.app.personalization.presentation.theme

import android.content.Context
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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
        val baseUrl = "${ResourceConfig.S3_URL}/widgetkeyboard/theme_decorates/$folderTheme"

        var downloadedCount = 0
        for ((subPath, destFile) in files) {
            val fileUrl = "$baseUrl/$subPath"
            val success = downloadFile(fileUrl, destFile)
            if (success) {
                downloadedCount++
            } else {
                // If config.json or keyboard_background.png fails, it's a fatal failure.
                // Key backgrounds (like return, emoji) might be optional, so we only fail on critical components.
                if (subPath == "config.json" || subPath == "keyboard_background.png") {
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

    private fun downloadFile(urlStr: String, destFile: File): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return false
            }

            connection.inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            connection?.disconnect()
        }
    }
}
