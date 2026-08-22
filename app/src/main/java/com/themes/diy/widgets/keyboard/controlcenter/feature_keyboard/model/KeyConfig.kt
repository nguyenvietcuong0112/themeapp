package com.themes.diy.widgets.keyboard.controlcenter.feature_keyboard.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyConfig(
    val customKey: String = "",
    val customStyle: CustomKeyStyle? = null
) : java.io.Serializable
