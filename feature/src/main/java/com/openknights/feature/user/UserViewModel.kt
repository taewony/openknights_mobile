package com.openknights.feature.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openknights.data.repository.UserRepository
import com.openknights.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "UserViewModel"

// Success 상태가 단일 User가 아닌 List<User>를 갖도록 수정
sealed interface UserUiState {
    data object Initial : UserUiState
    data object Loading : UserUiState
    data class Success(val users: List<User>) : UserUiState
    data class Error(val message: String) : UserUiState
}

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Initial)
    val uiState: StateFlow<UserUiState> = _uiState

    // 특정 사용자를 찾는 대신, 모든 사용자를 불러오는 함수로 변경
    fun loadUsers() {
        viewModelScope.launch {
            Log.d(TAG, "loadUsers called")
            _uiState.value = UserUiState.Loading
            try {
                val users = userRepository.getUsers()
                if (users.isNotEmpty()) {
                    Log.d(TAG, "Users found in repository. Updating UI state to Success with ${users.size} users.")
                    _uiState.value = UserUiState.Success(users)
                } else {
                    Log.w(TAG, "No users found in repository. Updating UI state to Error.")
                    _uiState.value = UserUiState.Error("No users found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "An error occurred while loading users.", e)
                _uiState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}


class UserViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
