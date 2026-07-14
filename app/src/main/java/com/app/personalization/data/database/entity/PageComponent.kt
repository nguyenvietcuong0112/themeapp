package com.app.personalization.data.database.entity

import kotlinx.serialization.Serializable

/**
 * Thực thể thành phần vật thể con (PageComponent) trên trang vẽ.
 */
@Serializable
data class PageComponent(
    val id: String,
    val componentType: String, // "STICKER", "TEXT", "SHAPE"
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotationAngle: Float,
    val zIndex: Int,
    val alpha: Float,
    
    // Thuộc tính của StickerComponent
    val stickerPath: String? = null,
    
    // Thuộc tính của TextComponent
    val text: String? = null,
    val fontPath: String? = null,
    val textColor: Int? = null,
    val alignment: String? = null, // "LEFT", "CENTER", "RIGHT"
    
    // Thuộc tính của ShapeComponent
    val vectorPath: String? = null,
    val strokeColor: Int? = null
) : java.io.Serializable
