package com.openknights.feature.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openknights.model.Role
import com.openknights.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed interface to represent different UI states
sealed interface UserUiState {
    object Initial : UserUiState
    object Loading : UserUiState
    data class Success(val user: User) : UserUiState
    data class Error(val message: String) : UserUiState
}

class UserViewModel : ViewModel() {

    // Backing property to hold the UI state
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Initial)
    // Publicly exposed StateFlow for observing UI state
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // Function to fetch or update user data
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                // Simulate network request or database query
                val user = User(
                    id = userId.toLong(),
                    studentId = "20240001",
                    name = "John Doe",
                    introduction = "Hello, I'm a developer.",
                    imageUrl = "",
                    roles = listOf(Role.TEAM_MEMBER),
                    projects = listOf("project1", "project2")
                )
                _uiState.value = UserUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error("Failed to load user data: ${e.message}")
            }
        }
    }
}