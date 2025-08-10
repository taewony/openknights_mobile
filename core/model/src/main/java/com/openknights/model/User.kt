package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Long,
    val studentId: String, // 학번,
    val name: String,
    val introduction: String,
    val imageUrl: String,
    val roles: List<Role>,
    val projects: List<String> = emptyList() // 프로젝트 ID 목록
)