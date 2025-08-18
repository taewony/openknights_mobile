package com.openknights.feature.user.uistate

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface UserScreenState {
    data object Loading : UserScreenState
    data class Success(val users: ImmutableList<UserUiState>) : UserScreenState
    data class Error(val message: String) : UserScreenState
}
