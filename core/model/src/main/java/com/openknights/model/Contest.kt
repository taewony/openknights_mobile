package com.openknights.model

data class Contest(
    val id: Long,
    val term: String,
    val description: String,
    val staff: List<String>,
    val phase: Phase?
)