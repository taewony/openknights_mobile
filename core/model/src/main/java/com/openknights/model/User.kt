package com.openknights.model

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val uid: String = "", // Firebase Auth UID, 기본 식별자
    val studentId: String = "", // 학번,
    val name: String = "",
    val introduction: String = "",
    val imageUrl: String = "",
    val roles: List<Role> = emptyList(),
    val projects: List<String> = emptyList(), // 프로젝트 ID 목록
    val id: Long = 0L // 기존 id는 더미 데이터로 처리
)