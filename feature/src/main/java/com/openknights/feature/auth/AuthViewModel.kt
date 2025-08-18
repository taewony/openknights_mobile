package com.openknights.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

import com.openknights.model.User
import com.openknights.data.repository.UserRepository
import com.openknights.data.repository.UserRepositoryImpl

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application, private val auth: FirebaseAuth) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val userRepository: UserRepository = UserRepositoryImpl(application, firestore, storage)



    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _isLoggedIn = MutableStateFlow(false) // Default to not logged in for fake data
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    init {
        auth.addAuthStateListener {
            val loggedIn = it.currentUser != null
            _isLoggedIn.value = loggedIn
            _currentUserEmail.value = it.currentUser?.email
            Log.d("AuthViewModel", "Auth state changed: isLoggedIn = $loggedIn, currentUser = ${it.currentUser?.email}")
        }
        Log.d("AuthViewModel", "Initial auth state: isLoggedIn = ${_isLoggedIn.value}, currentUser = ${auth.currentUser?.email}")

        // Firebase Storage 접근 테스트 (임시 코드)
        // viewModelScope.launch {
        //     userRepository.testStorageAccess()
        // }
    }

    override fun onCleared() {
        super.onCleared()
        // Note: Removing the listener directly here is not straightforward without a reference.
        // For simplicity, we'll rely on the ViewModel's lifecycle to manage this.
        // In a more complex scenario, you might store the listener in a variable
        // and remove it explicitly.
        Log.d("AuthViewModel", "AuthViewModel cleared, Firebase listener management simplified.")
    }

    fun registerUser(email: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true) // 로딩 상태 시작

        viewModelScope.launch {
            try {
                // 1. await()을 사용해 인증이 끝날 때까지 대기
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found.")

                // 2. DB에 프로필 저장이 끝날 때까지 대기
                                val user = User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", name = firebaseUser.email?.split('@')?.firstOrNull() ?: "")
                userRepository.addUserProfile(user)

                // 3. 모든 작업이 성공하면 UI 상태를 '성공'으로 변경
                _uiState.value = AuthUiState(isLoading = false, success = true)
                Log.d("AuthViewModel", "User registered and profile saved: $email")

            } catch (e: Exception) {
                // 4. 인증, DB 작업 등 모든 단계의 에러를 한 곳에서 처리
                val errorMessage = if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    "이미 사용 중인 이메일입니다. 다른 이메일을 사용하거나 로그인해주세요."
                } else {
                    e.message ?: "알 수 없는 오류가 발생했습니다."
                }
                _uiState.value = AuthUiState(isLoading = false, error = errorMessage)
                Log.e("AuthViewModel", "Registration failed", e)
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                        Log.d("AuthViewModel", "User logged in: $email")
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = task.exception?.message ?: "Login failed.")
                        Log.e("AuthViewModel", "Login failed: ${task.exception?.message}")
                    }
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    fun signOut() {
        auth.signOut()
        Log.d("AuthViewModel", "User signed out.")
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)
