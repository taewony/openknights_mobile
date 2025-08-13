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


---

# Firebase 연동 가이드

이 문서는 OpenKnights 앱과 Firebase를 연동하기 위한 기술적인 가이드와 정책을 담고 있습니다.

---

## Firestore 데이터 접근 정책: 인증과 보안 규칙

Firebase Firestore 데이터베이스에 저장된 데이터에 접근하기 위한 정책을 정의합니다.

### 핵심 질문: 데이터를 가져오려면 로그인이 필요한가요?

결론부터 말하면, **"프로젝트에 설정된 Firestore 보안 규칙(Security Rules)에 따라 다릅니다."**

Firestore는 규칙을 어떻게 설정하느냐에 따라 로그인한 사용자만 접근하게 할 수도 있고, 로그인하지 않은 사용자(Guest)에게도 제한적으로 허용할 수 있습니다. 보안 규칙은 우리 데이터베이스의 "문지기" 역할을 하며, 모든 데이터 요청은 이 문지기의 허락을 받아야 합니다.

아래는 두 가지 대표적인 시나리오입니다.

### 시나리오 1: 로그인 필수 (Authenticated Access)

가장 일반적이고 안전한 방식입니다. 사용자의 개인 정보 등 민감한 데이터를 다룰 때 사용됩니다. 이 경우, 보안 규칙은 다음과 같이 설정됩니다.

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 'users' 컬렉션의 모든 문서에 대해
    match /users/{userId} {
      // 요청을 보낸 사용자가 로그인 상태일 때만 읽기/쓰기를 허용합니다.
      allow read, write: if request.auth != null;
    }
  }
}
```

*   **`request.auth != null`**: 이 규칙의 핵심입니다. Firestore로 데이터를 요청한 사용자가 유효한 인증 정보(`auth`)를 가지고 있을 때, 즉 **로그인한 상태일 때만** 접근을 허용한다는 의미입니다.
*   이 규칙 하에서 로그인을 하지 않고 데이터를 요청하면, Firestore는 `PERMISSION_DENIED` 오류를 반환하며 요청을 거부합니다.

### 시나리오 2: 로그인 없이 읽기 가능 (Guest Access)

공지사항, 공개 프로필, 랭킹 등 모든 사용자에게 공개해도 되는 데이터를 다룰 때 사용합니다.

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 'users' 컬렉션의 모든 문서에 대해
    match /users/{userId} {
      // 읽기(read)는 누구나 허용하지만,
      allow read: if true;
      // 쓰기(write)는 로그인한 사용자만 허용합니다.
      allow write: if request.auth != null;
    }
  }
}
```

*   **`allow read: if true;`**: 이 규칙은 **조건 없이 모든 읽기 요청을 허용**하겠다는 의미입니다. 따라서 로그인하지 않은 사용자도 `users` 컬렉션의 데이터를 읽을 수 있습니다.
*   **⚠️ 보안 경고:** 데이터를 쓰는 것(`write`)까지 `if true`로 설정하는 것은 매우 위험합니다. 악의적인 사용자가 데이터베이스를 마음대로 쓰고 지울 수 있게 되므로, 쓰기 권한은 항상 특정 조건(예: 로그인한 사용자)을 만족할 때만 부여해야 합니다.

### 🤷‍♂️ 우리 프로젝트는 어떤 방식인가요? (How to Check)

우리 프로젝트의 정책을 확인하는 방법은 두 가지입니다.

1.  **Firebase Console에서 확인:**
    *   Firebase 콘솔([https://console.firebase.google.com/](https://console.firebase.google.com/))에 접속합니다.
    *   프로젝트를 선택하고, **빌드 > Firestore Database** 메뉴로 이동합니다.
    *   상단의 **규칙(Rules)** 탭을 클릭하면 현재 적용된 보안 규칙을 직접 볼 수 있습니다.
```
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2025, 9, 10);
    }
```
주의: 이 규칙은 초기 개발 단계에서는 유용할 수 있지만, 프로덕션 환경에서는 매우 위험합니다. 인증 여부와 관계없이 누구나 데이터를 읽고, 수정하고, 삭제할 수 있기 때문입니다.
더 안전한 규칙을 사용하는 것을 강력히 권장합니다. 예를 들어, 인증된 사용자만 자신의 데이터를 수정할 수 있도록 하는 규칙은 다음과 같습니다.
```
   1 rules_version = '2';
   2 service cloud.firestore {
   3   match /databases/{database}/documents {
   4     // users 컬렉션의 문서는 해당 userId의 주인만 읽고 쓸 수 있음
   5     match /users/{userId} {
   6       allow read, write: if request.auth != null && request.auth.uid == userId;
   7     }
   8   }
   9 }
```
2.  **프로젝트 파일에서 확인:**
    *   프로젝트 루트 디렉토리에서 `firestore.rules` 라는 이름의 파일을 찾아 내용을 확인합니다.


---
## 앱에서 Firestore Store에 fake json data 직접 데이터 저장하기

`implementation(platform(libs.firebase.bom))` 은 Firebase BoM을 적용해 `firebase-auth`, `firebase-firestore`, `firebase-storage` 등 모든 **Firebase 공식 라이브러리**의 버전을 자동으로 맞춰줍니다. 단, BoM이 라이브러리를 “추가”해주진 않으므로 필요한 모듈은 직접 명시해야 합니다.
`Task.await()` 확장 함수를 쓰려면 Firebase 외부 라이브러리인 **`kotlinx-coroutines-play-services`** 를 별도로 추가해야 합니다.

```kotlin
@libs.versions.toml
[versions]
firebaseBom = "34.1.0"
coroutines-play-services = "1.10.2"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
firebase-common = { group = "com.google.firebase", name = "firebase-common" }
firebase-storage = { module = "com.google.firebase:firebase-storage" }
firebase-messaging = { module = "com.google.firebase:firebase-messaging" }

@build.gradle.kts
dependencies {
    // Firebase Integration
    implementation(platform(libs.firebase.bom)) // Firebase 버전 통합 관리
    implementation(libs.firebase.common) // Firebase 진입점(Firebase.*)
    implementation(libs.firebase.firestore) 
    implementation(libs.coroutines.play.services) // await() 지원
}
```


개발 및 테스트 목적으로, 앱이 실행될 때 코드를 통해 직접 Firestore에 초기 데이터를 추가할 수 있습니다. 예를 들어 디버그 빌드에서만 실행되는 버튼을 만들어 활용할 수 있습니다.

   1. `UserViewModel.kt`:
       * saveUsersToFirestore(users: List<User>) 함수를 추가했습니다. 이 함수는 각 User 객체를 user.uid를 문서
          ID로 사용하여 Firestore의 users 컬렉션에 저장합니다.
       * com.google.firebase.firestore.ktx.firestore, com.google.firebase.ktx.Firebase,
         kotlinx.coroutines.tasks.await 임포트를 추가했습니다.
   2. `UserScreen.kt`:
       * UserScreen 컴포저블에서 UserViewModel의 saveUsersToFirestore 함수를 호출하는 onSaveClick 람다를
         UserScreenContent로 전달하도록 수정했습니다.
       * UserScreenContent 컴포저블의 함수 시그니처에 onSaveClick: (List<User>) -> Unit 파라미터를
         추가했습니다.
       * 데이터 로딩 성공 시 "Save Users to Firestore" 버튼이 표시되도록 UserScreenContent에 버튼을
         추가했습니다. 이 버튼을 누르면 현재 표시된 사용자 목록이 Firestore에 저장됩니다.
		 
✦ 사용자께서 OpenKnightsApp를 다음과 같이 수정하기를 원하십니다.
   1. 앱 진입 시 Firebase에서 Contest 목록을 자동으로 로드합니다.
   2. 로드된 Contest 목록을 Scaffold 콘텐츠에 표시합니다.
   3. "User" 메뉴를 클릭하면 User 화면으로 이동합니다.
   4. "저장" 및 "로드" 버튼을 주석 처리합니다.

  이는 내비게이션 및 초기 데이터 로딩과 관련된 중요한 변경 사항입니다.

  계획은 다음과 같습니다.
   1. 앱의 주요 진입점 식별: OpenKnightsApp.kt 또는 MainActivity.kt일 가능성이 높습니다. OpenKnightsApp를
      검색하는 것부터 시작하겠습니다.
   2. 내비게이션 구현: Contest 및 User 화면 간 전환을 위해 내비게이션 구성 요소(예: Jetpack Compose
      Navigation)를 설정해야 합니다.
   3. `OpenKnightsApp` (또는 메인 액티비티 Composable) 수정:
       * NavController를 설정합니다.
       * "Contest" 및 "User" 화면에 대한 경로를 정의합니다.
       * 하단 내비게이션 바가 있는 Scaffold를 구현합니다.
       * 화면 간 전환을 위해 내비게이션 클릭을 처리합니다.
   4. Contest 화면 수정:
       * "Load Contests" 및 "Save Contests" 버튼을 제거합니다(주석 처리).
       * 화면이 구성될 때 loadContests()를 자동으로 트리거합니다(예: LaunchedEffect 사용).
   5. User 화면 수정:
       * User 화면이 존재하는지 확인하거나 기본적인 자리 표시자를 만듭니다.
       * 화면이 구성될 때 loadUsers()를 자동으로 트리거합니다.
       * 기존 "로드" 또는 "저장" 버튼을 주석 처리합니다.

  앱의 주요 진입점인 OpenKnightsApp를 찾는 것부터 시작하겠습니다. app 모듈에서 OpenKnightsApp.kt를
  검색하겠습니다.
 ╭──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
 │ ✔  FindFiles '**/*OpenKnightsApp.kt' within app                                                              │
 │                                                                                                              │
 │    Found 2 matching file(s)                                                                                  │
 ╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────╯
✦ OpenKnightsApp.kt 파일이 두 개 발견되었습니다. 좀 특이하지만, app/build.gradle.kts의 namespace =
  "com.openknights.app"와 일치하므로 com.openknights.app 아래에 있는 파일이 올바른 파일이라고 가정하겠습니다.

  D:\code\mobileApp\openknights_mobile\app\src\main\java\com\openknights\app\OpenKnightsApp.kt를 읽겠습니다.
 ╭──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
 │ ✔  ReadFile ...app\openknights\app\OpenKnightsApp.kt                                                         │
 ╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────╯
✦ OpenKnightsApp.kt의 내용을 확인했습니다. 이 파일은 UserScreen과 ContestScreen 간 전환을 위한 Scaffold 및
  NavigationBar가 있는 주 앱 컴포저블입니다.

  현재 내비게이션은 when (selectedItem) 블록으로 처리됩니다. 사용자 요청을 구현하려면 다음을 수행해야 합니다.

   1. Jetpack Compose Navigation 통합: when 문을 NavHost 및 NavController로 바꿉니다.
   2. ContestScreen 초기 데이터 로딩: ContestScreen을 수정하여 진입 시 데이터를 자동으로 로드하도록 합니다.
   3. UserScreen 초기 데이터 로딩: UserScreen도 데이터를 자동으로 로드하는지 확인합니다.
   4. 버튼 주석 처리: ContestScreen의 "로드" 및 "저장" 버튼과 UserScreen의 유사한 버튼을 주석 처리합니다.

  Jetpack Compose Navigation을 OpenKnightsApp.kt에 통합하는 것부터 시작하겠습니다.

  먼저 app/build.gradle.kts에 내비게이션 종속성을 추가해야 합니다. 내비게이션 별칭이 있는지 libs.versions.toml을
   확인하거나 새 별칭을 추가하겠습니다.

  내비게이션을 위해 libs.versions.toml을 확인하겠습니다.
 ╭──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
 │ ✔  ReadFile gradle\libs.versions.toml                                                                        │
 ╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────╯
✦ libs.versions.toml을 확인했습니다. Compose Navigation에 대한 기존 별칭이 없습니다.

  Compose Navigation에 대한 새 버전 및 라이브러리 항목을 추가하겠습니다. 일반적인 버전은 2.7.0 이상입니다. 일단
  2.7.0을 사용하겠습니다.

  `libs.versions.toml` 수정:
  [versions]에 추가:
  navigationCompose = "2.7.0"

  [libraries]에 추가:
  androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref =
  "navigationCompose" }

  libs.versions.toml을 수정한 후, app/build.gradle.kts에 종속성을 추가하겠습니다.

  먼저 libs.versions.toml을 수정하겠습니다.
