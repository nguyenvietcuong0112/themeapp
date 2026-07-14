package com.theme.customizer.keyboard

import java.io.Serializable

data class KeyConfig(
    val keyBgColor: String = "#1E1E2E",
    val keyTextColor: String = "#FFFFFF",
    val keyTextSizeSp: Float = 18f
) : Serializable

data class FontConfig(
    val fontName: String = "sans-serif",
    val isBold: Boolean = false
) : Serializable

data class KeyStyle(
    val cornerRadiusDp: Float = 6f,
    val showPopupOnPress: Boolean = true
) : Serializable

/**
 * Cấu trúc cấu hình giao diện bàn phím ThemeConfig.
 */
data class ThemeConfig(
    val keyConfig: KeyConfig = KeyConfig(),
    val fontConfig: FontConfig = FontConfig(),
    val keyStyle: KeyStyle = KeyStyle(),
    val backgroundPath: String = ""
) : Serializable
