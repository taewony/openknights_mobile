package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Long = 0L,
    val name: String = "",
    val term: String = "",
    val phase: Phase = Phase.PLANED,
    val teamName: String = "",
    val leaderName: String = "",
    val leaderId: String = "",
    val members: List<String> = emptyList(),
    val language: String = "",
    val description: String = "",
    val mentor: String = "",
    val note: String = "",
    val preTotal: Int = 0,
    val preScore: Score = Score(0, 0, 0, 0),
    val finalTotal: Int = 0,
    val finalScore: Score = Score(0, 0, 0, 0)
)
