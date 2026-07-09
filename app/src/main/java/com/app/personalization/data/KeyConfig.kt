package com.app.personalization.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyConfig(
    val customKey: String = "",
    val customStyle: CustomKeyStyle? = null
) : java.io.Serializable
