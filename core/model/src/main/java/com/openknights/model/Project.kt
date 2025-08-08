package com.openknights.model

data class Project(
    val id: Long,
    val name: String?,
    val term: String?,
    val phase: Phase?,
    val teamName: String?,
    val leaderName: String?,
    val leaderId: String?,
    val members: List<String>?,
    val language: String?,
    val description: String?,
    val mentor: String?,
    val note: String?,
    val preTotal: Int?,
    val preScore: Score?,
    val finalTotal: Int?,
    val finalScore: Score?
)
