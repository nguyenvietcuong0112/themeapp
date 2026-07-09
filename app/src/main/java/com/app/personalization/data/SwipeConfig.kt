package com.app.personalization.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwipeConfig(
    @SerialName("customSwipe") val customSwipe: String = ""
) : java.io.Serializable
