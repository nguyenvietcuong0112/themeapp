package com.app.personalization.data

import android.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PopupConfig(
    @SerialName("useImage") val useImage: Boolean = false,
    @SerialName("textColor") val textColorString: String = ""
) : java.io.Serializable {

    val textColor: Int
        get() {
            if (textColorString.isEmpty()) {
                return Color.BLACK
            }
            return try {
                Color.parseColor(textColorString)
            } catch (e: Exception) {
                Color.BLACK
            }
        }
}
