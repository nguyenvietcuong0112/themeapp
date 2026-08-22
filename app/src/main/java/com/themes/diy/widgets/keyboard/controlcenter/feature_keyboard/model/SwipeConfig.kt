package com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwipeConfig(
    @SerialName("customSwipe") val customSwipe: String = ""
) : java.io.Serializable
