package com.openknights.core.model

data class User (
    val id: String, // 학번,
    val name: String,
    val introduction: String,
    val imageUrl: String,
    val email: String,
    val roles: List<String> = emptyList() // 역할 추가
)