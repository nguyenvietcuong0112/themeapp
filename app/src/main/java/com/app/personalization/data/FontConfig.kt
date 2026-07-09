package com.app.personalization.data

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class FontConfig(
    @SerialName("fontColor") var fontColorString: String = "",
    @SerialName("fontFamily") var fontFamily: String = ""
) : java.io.Serializable {

    val fontColor: Int
        get() {
            if (fontColorString.isEmpty()) {
                return Color.BLACK
            }
            return try {
                Color.parseColor(fontColorString)
            } catch (e: Exception) {
                Color.BLACK
            }
        }

    fun getTypeface(context: Context): Typeface? {
        try {
            val lowerCase = fontFamily.lowercase(Locale.ROOT)
            val formatName = lowerCase.replace("-", "_")
            if (formatName.isEmpty()) return null
            return Typeface.createFromAsset(context.resources.assets, "themes/fonts/$formatName.ttf")
        } catch (e: Exception) {
            return null
        }
    }
}
