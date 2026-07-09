package com.app.personalization.data

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
            return Uri.parse("file:///android_asset/themes/lens/$lenName/len.gif")
        }

    val blur: Float?
        get() {
            if (blurry.isEmpty()) return null
            return blurry.toFloatOrNull()
        }
}
