package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val usability: Int,
    val techStack: Int,
    val creativity: Int,
    val completeness: Int,
)