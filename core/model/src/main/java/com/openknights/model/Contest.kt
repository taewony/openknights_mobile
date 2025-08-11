package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class Contest(
    val id: Long = 0,
    val term: String = "",
    val description: String = "",
    val staff: List<String> = emptyList(),
    val phase: Phase? = null
)