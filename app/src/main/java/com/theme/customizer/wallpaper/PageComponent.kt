package com.theme.customizer.wallpaper

import java.io.Serializable
import java.util.UUID

/**
 * Kiểu thành phần thiết kế trong hình nền DIY.
 */
enum class ComponentType {
    WALLPAPER, // Hình nền (màu trơn, gradient hoặc ảnh)
    STICKER,   // Nhãn dán trang trí SVG/PNG
    TEXT,      // Chữ nghệ thuật
    FRAME      // Khung ảnh nghệ thuật
}

/**
 * Đại diện cho một thành phần con (Layer) trên khung vẽ DIY.
 */
data class PageComponent(
    val id: String = UUID.randomUUID().toString(),
    val type: ComponentType,
    var resPath: String = "",        // Đường dẫn tài nguyên (URL, Asset hoặc File)
    var textValue: String = "",      // Giá trị chữ nếu là chữ nghệ thuật
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var fontPath: String = "",
    var translationX: Float = 0f,
    var translationY: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var rotationAngle: Float = 0f,
    var zIndex: Int = 0              // Thứ tự hiển thị lớp
) : Serializable
