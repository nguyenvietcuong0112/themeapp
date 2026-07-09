package com.app.personalization.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomKeyStyle(
    var backgroundColor: String = "", // Hex color (e.g. #FFFFFF)
    var cornerRadius: Float = 0.0f,
    var borderWidth: Float = 0.0f,
    var borderColor: String = "",
    var blur: Float = 1.0f            // Alpha transparency (0.0 to 1.0)
) : java.io.Serializable
