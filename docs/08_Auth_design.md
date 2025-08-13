# 08_Auth_design.md: Firebase 인증 기능 구현 가이드

이 문서는 OpenKnights 모바일 앱에 Firebase Authentication을 사용하여 사용자 인증 기능을 구현하는 방법을 체계적으로 안내합니다. 기존 더미 인증 로직을 실제 Firebase 인증으로 대체한 현재 구현의 구조와 주요 코드 스니펫을 제공합니다.

## 1. Firebase 프로젝트 설정 및 의존성 추가

Firebase 인증 기능을 사용하기 위해 Firebase 프로젝트에 필요한 설정을 완료하고, 앱 모듈에 관련 의존성을 추가해야 합니다.

### 1.1 Firebase 콘솔 설정

1.  **Firebase 프로젝트 생성 또는 선택:** Firebase 콘솔(console.firebase.google.com)에 접속하여 기존 프로젝트를 선택하거나 새로운 프로젝트를 생성합니다.
2.  **Android 앱 추가:** 프로젝트 개요에서 'Android 앱 추가' 버튼을 클릭하고, 앱의 패키지 이름(예: `com.openknights.app`)과 디버그 서명 인증서 SHA-1 지문(이전 단계에서 확인한 SHA-1 키)을 등록합니다.
3.  **`google-services.json` 다운로드:** Firebase에서 제공하는 `google-services.json` 파일을 다운로드하여 `app` 모듈의 루트 디렉토리(예: `app/google-services.json`)에 배치합니다.
4.  **Authentication 활성화:** Firebase 콘솔에서 'Authentication' 섹션으로 이동하여 '시작하기'를 클릭합니다. '로그인 방법' 탭에서 '이메일/비밀번호'를 활성화합니다.

### 1.2 Gradle 의존성 추가

`app/build.gradle.kts` 파일에 Firebase Authentication 및 Google Play Services 관련 의존성을 추가합니다.

```kotlin
// app/build.gradle.kts
dependencies {
    // Firebase BoM (Platform)
    implementation(platform("com.google.firebase:firebase-bom:32.2.2")) // 최신 버전으로 업데이트 필요
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Firestore (User Profile 저장 시 필요)
    implementation("com.google.firebase:firebase-firestore-ktx")
}
```

## 2. `AuthViewModel` 구현 상세

`AuthViewModel`은 앱의 인증 관련 로직을 처리하는 ViewModel입니다. 사용자 등록, 로그인, 로그아웃 기능을 Firebase Authentication과 연동하며, UI 상태를 관리합니다.

### 2.1 `AuthViewModel.kt`

`feature/src/main/java/com/openknights/feature/auth/AuthViewModel.kt` 파일의 주요 구현 내용은 다음과 같습니다.

```kotlin
package com.openknights.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

import com.openknights.core.model.User
import com.openknights.core.data.user.UserRepository
import com.openknights.core.data.user.UserRepositoryImpl

class AuthViewModel() : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    private val authStateListener: FirebaseAuth.AuthStateListener

    init {
        authStateListener = FirebaseAuth.AuthStateListener {
            val loggedIn = it.currentUser != null
            _isLoggedIn.value = loggedIn
            _currentUserEmail.value = it.currentUser?.email
            Log.d("AuthViewModel", "Auth state changed: isLoggedIn = $loggedIn, currentUser = ${it.currentUser?.email}")
        }
        auth.addAuthStateListener(authStateListener)
        Log.d("AuthViewModel", "Initial auth state: isLoggedIn = ${_isLoggedIn.value}, currentUser = ${auth.currentUser?.email}")
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        Log.d("AuthViewModel", "AuthViewModel cleared, Firebase listener removed.")
    }

    fun registerUser(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            val user = User(uid = it.uid, email = it.email, name = it.displayName ?: "User")
                            userRepository.addUserProfile(user) // 사용자 프로필 저장
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                        Log.d("AuthViewModel", "User registered: $email")
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = task.exception?.message ?: "Registration failed.")
                        Log.e("AuthViewModel", "Registration failed: ${task.exception?.message}")
                    }
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
```

**주요 특징:**
*   `FirebaseAuth.getInstance()`를 통해 Firebase 인증 인스턴스를 가져옵니다.
*   `AuthStateListener`를 사용하여 Firebase 인증 상태 변화를 실시간으로 감지하고 `_isLoggedIn` 및 `_currentUserEmail` StateFlow를 업데이트합니다.
*   `registerUser` 및 `loginUser` 함수는 `FirebaseAuth`의 `createUserWithEmailAndPassword` 및 `signInWithEmailAndPassword` 메서드를 호출하여 실제 인증을 수행합니다.
*   인증 성공/실패에 따라 `_uiState`를 업데이트하여 UI에 로딩, 성공, 에러 상태를 전달합니다.
*   `registerUser` 성공 시 `userRepository.addUserProfile(user)`를 호출하여 Firestore에 사용자 프로필을 저장합니다.
*   `onCleared()`에서 `AuthStateListener`를 제거하여 메모리 누수를 방지합니다.

## 3. 로그인 및 회원가입 화면 (`LoginScreen.kt`, `RegisterScreen.kt`)

`LoginScreen.kt`와 `RegisterScreen.kt`는 사용자로부터 이메일과 비밀번호를 입력받아 `AuthViewModel`의 해당 인증 함수를 호출합니다. `AuthViewModel`의 `uiState`를 관찰하여 UI를 업데이트하고 사용자에게 피드백을 제공합니다.

### 3.1 `LoginScreen.kt`

`feature/src/main/java/com/openknights/feature/auth/LoginScreen.kt` 파일의 주요 구현 내용은 다음과 같습니다.

```kotlin
package com.openknights.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState.success) {
            onLoginSuccess()
            viewModel.resetState()
        } else if (uiState.error != null) {
            uiState.error?.let { errorMessage ->
                scope.launch { snackbarHostState.showSnackbar(errorMessage) }
            }
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("로그인") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = { viewModel.loginUser(email, password) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("로그인")
                }
            }
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("계정이 없으신가요? 회원가입")
            }
        }
    }
}
```

**주요 특징:**
*   `AuthViewModel`의 `uiState`를 `collectAsState()`로 관찰합니다.
*   `LaunchedEffect`를 사용하여 `uiState`의 변화에 따라 로그인 성공 시 `onLoginSuccess()`를 호출하거나, 에러 발생 시 `Snackbar`를 통해 에러 메시지를 표시합니다.
*   로그인 버튼은 `uiState.isLoading` 상태와 이메일/비밀번호 필드의 공백 여부에 따라 활성화/비활성화됩니다 (`email.isNotBlank() && password.isNotBlank()`).
*   로딩 중에는 버튼 내부에 `CircularProgressIndicator`를 표시하여 시각적인 피드백을 제공합니다.

### 3.2 `RegisterScreen.kt`

`feature/src/main/java/com/openknights/feature/auth/RegisterScreen.kt` 파일의 주요 구현 내용은 다음과 같습니다.

```kotlin
package com.openknights.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBack: () -> Unit, onRegisterSuccess: () -> Unit) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState.success) {
            onRegisterSuccess()
            viewModel.resetState()
        } else if (uiState.error != null) {
            uiState.error?.let { errorMessage ->
                val userFriendlyMessage = when {
                    errorMessage.contains("FirebaseAuthInvalidCredentialsException") -> "유효하지 않은 이메일 또는 비밀번호입니다."
                    errorMessage.contains("FirebaseAuthUserCollisionException") -> "이미 등록된 이메일입니다."
                    errorMessage.contains("network error") -> "네트워크 연결을 확인해주세요."
                    else -> "등록 실패: $errorMessage"
                }
                scope.launch { snackbarHostState.showSnackbar(userFriendlyMessage) }
            }
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("사용자 등록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = { viewModel.registerUser(email, password) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("등록")
                }
            }
        }
    }
}
```

**주요 특징:**
*   `LoginScreen.kt`와 유사하게 `AuthViewModel`의 `uiState`를 관찰하여 UI를 업데이트합니다.
*   등록 버튼은 `uiState.isLoading` 상태와 이메일/비밀번호 필드의 공백 여부에 따라 활성화/비활성화됩니다.
*   로딩 중에는 버튼 내부에 `CircularProgressIndicator`를 표시합니다.
*   `LaunchedEffect` 내에서 Firebase 에러 메시지를 파싱하여 사용자에게 더 친숙한 메시지를 제공합니다. (예: "이미 등록된 이메일입니다.")

## 4. `OpenKnightsApp.kt`에서의 연동

`OpenKnightsApp.kt`는 앱의 전반적인 내비게이션을 관리하며, `AuthViewModel`을 사용하여 인증 상태에 따라 적절한 화면을 표시합니다.

```kotlin
// OpenKnightsApp.kt (관련 부분 발췌 및 수정)

// ... 기존 import 문
import com.google.firebase.auth.FirebaseAuth
import com.openknights.feature.auth.AuthViewModel
// AuthViewModelFactory는 더 이상 사용되지 않습니다.

// ... 기존 코드

@Composable
fun OpenKnightsApp() {
    // ... 기존 backStack, currentEntry 등

    // AuthViewModel 인스턴스화 (factory 없이 직접 viewModel() 사용)
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsState() // 이메일 정보 사용

    // ... 기존 Scaffold

    // TopAppBar 내 DropdownMenu 수정 (로그인/로그아웃 메뉴)
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        // ... 기타 메뉴 아이템

        if (isLoggedIn) {
            DropdownMenuItem(
                text = { Text("로그아웃") },
                onClick = {
                    showMenu = false
                    authViewModel.signOut()
                    // 로그아웃 후 로그인 화면으로 이동하거나, 앱의 초기 상태로 리셋
                    backStack.clear()
                    backStack.add(LoginScreenEntry)
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("로그인") },
                onClick = {
                    showMenu = false
                    backStack.add(LoginScreenEntry)
                }
            )
            DropdownMenuItem(
                text = { Text("회원가입") },
                onClick = {
                    showMenu = false
                    backStack.add(RegisterScreenEntry)
                }
            )
        }
    }

    // ... 기존 Scaffold content (when 문)

    when (val entry = backStack.lastOrNull()) {
        // ... 기존 ContestsScreenEntry, ProjectsScreenEntry 등

        is RegisterScreenEntry -> RegisterScreen(
            onBack = { backStack.removeLastOrNull() },
            onRegisterSuccess = {
                backStack.removeLastOrNull() // Remove RegisterScreen
                backStack.add(LoginScreenEntry) // 회원가입 성공 후 로그인 화면으로 이동
            }
        )
        is LoginScreenEntry -> LoginScreen(
            onBack = { backStack.removeLastOrNull() },
            onLoginSuccess = {
                // 로그인 성공 시, Firebase Auth 상태 리스너가 isLoggedIn을 업데이트하므로 별도 signIn 호출 불필요
                backStack.clear()
                backStack.add(ContestsScreenEntry) // 로그인 성공 후 메인 화면으로 이동
            },
            onNavigateToRegister = { backStack.add(RegisterScreenEntry) }
        )
        // ... null 처리
    }
}
```

**주요 특징:**
*   `AuthViewModel`은 `viewModel()` 컴포저블 함수를 통해 직접 인스턴스화됩니다. (별도의 Factory는 사용하지 않습니다.)
*   `authViewModel.isLoggedIn` 및 `authViewModel.currentUserEmail`을 관찰하여 UI (예: `DropdownMenu`의 로그인/로그아웃 버튼)를 동적으로 변경합니다.
*   로그인 및 회원가입 성공 시 `backStack`을 조작하여 적절한 화면으로 내비게이션합니다.

## 결론

이 문서는 OpenKnights 모바일 앱의 Firebase Authentication 구현 구조를 설명했습니다. `AuthViewModel`이 인증 로직을 중앙에서 관리하고, `LoginScreen` 및 `RegisterScreen`이 사용자 인터페이스를 제공하며 `AuthViewModel`과 상호작용하는 방식입니다. 이 구조는 Firebase의 강력한 인증 기능을 활용하면서도 UI와 비즈니스 로직을 분리하여 앱의 유지보수성과 확장성을 높입니다.
