package com.openknights.feature.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.openknights.data.repository.UserRepository
import com.openknights.feature.user.uistate.UserScreenState
import com.openknights.feature.user.uistate.UserUiState
import com.openknights.model.User
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "UserViewModel"

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UserScreenState>(UserScreenState.Loading)
    val uiState: StateFlow<UserScreenState> = _uiState

    fun loadUsers() {
        viewModelScope.launch {
            Log.d(TAG, "loadUsers called")
            _uiState.value = UserScreenState.Loading
            try {
                val users = userRepository.getUsers()
                if (users.isNotEmpty()) {
                    // 현재 로그인한 사용자 ID 가져오기 (실제 앱에서는 AuthRepository 등을 통해 주입받아야 함)
                    val currentUserId = Firebase.auth.currentUser?.uid

                    // 데이터 모델(User)을 UI 상태 모델(UserUiState)로 변환
                    val userUiStates = users.map { user ->
                        UserUiState(
                            uid = user.uid,
                            name = user.name,
                            introduction = user.introduction,
                            profileImageUrl = user.imageUrl, // 필드명 확인 필요
                            isCurrentUser = user.uid == currentUserId
                        )
                    }.toImmutableList()

                    Log.d(TAG, "Users found. Updating UI state to Success with ${userUiStates.size} users.")
                    _uiState.value = UserScreenState.Success(userUiStates)
                } else {
                    Log.w(TAG, "No users found in repository. Updating UI state to Error.")
                    _uiState.value = UserScreenState.Error("No users found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "An error occurred while loading users.", e)
                _uiState.value = UserScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveUsersToFirestore(users: List<User>) {
        viewModelScope.launch {
            val db = Firebase.firestore
            users.forEach { user ->
                try {
                    db.collection("users").document(user.uid).set(user).await()
                    Log.d(TAG, "User ${user.uid} saved to Firestore successfully.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving user ${user.uid} to Firestore: ${e.message}", e)
                }
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
