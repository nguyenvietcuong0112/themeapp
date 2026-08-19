package com.app.personalization.feature_wallpaper.data.entity

import kotlinx.serialization.Serializable

/**
 * Thực thể trang vẽ chi tiết (DesignPage) chứa danh sách các PageComponent con.
 */
@Serializable
data class DesignPage(
    val id: String,
    val pageComponents: ArrayList<PageComponent> = arrayListOf()
) : java.io.Serializable
