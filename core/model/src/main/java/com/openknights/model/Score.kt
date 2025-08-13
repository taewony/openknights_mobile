package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val usability: Int = 0,
    val techStack: Int = 0,
    val creativity: Int = 0,
    val completeness: Int = 0,
)