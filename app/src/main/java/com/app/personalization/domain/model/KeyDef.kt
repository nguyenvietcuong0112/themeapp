package com.app.personalization.domain.model

data class KeyDef(
    val label: String,
    val popup: List<String> = emptyList(),
    val isFunctional: Boolean = false,
    val functionalType: String? = null,
    val code: Int = 0,
    val keyWidthPercent: Float = 0.1f
)
