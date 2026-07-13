package com.app.personalization.data

import android.content.Context

object FileUtils {
    fun loadJsonFromAsset(context: Context, filePath: String): String {
        return context.assets.open(filePath).bufferedReader().use { it.readText() }
    }
}
