package com.openknights.feature.user.uistate

import androidx.compose.runtime.Immutable

@Immutable
data class UserUiState(
    val uid: String,
    val name: String,
    val introduction: String,
    val profileImageUrl: String?,
    val isCurrentUser: Boolean
)