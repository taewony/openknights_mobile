package com.openknights.core.model

data class Project(
    val id: String,
    val contestTerm: String, // Contest.term 참조 "YYYY-1st" or "YYYY-2nd"
    val phase: ProjectPhase,
    val teamName: String,
    val goal: String,

)