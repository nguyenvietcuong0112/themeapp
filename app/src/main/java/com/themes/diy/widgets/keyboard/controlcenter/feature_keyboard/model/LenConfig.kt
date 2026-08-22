package com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.model

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LenConfig(
    @SerialName("lenName") var lenName: String = "",
    @SerialName("blurry") var blurry: String = ""
) : java.io.Serializable {

    val uri: Uri?
        get() {
            if (lenName.isEmpty()) return null
            return Uri.parse(com.themes.diy.widgets.keyboard.controlcenter.core.data.ResourceConfig.getLenGifUrl(lenName))
        }

    val blur: Float?
        get() {
            if (blurry.isEmpty()) return null
            return blurry.toFloatOrNull()
        }
}
