# Firebase 연동 설계

## 1. 소개

이 문서는 OpenKnights 앱에 Firebase를 연동하기 위한 전체적인 설계와 기술 가이드를 제공합니다. Firebase Authentication, Firestore, Storage를 중심으로 각 기능의 구현 단계, 데이터 모델, 보안 규칙 등을 상세히 다룹니다.

---

## 2. Firebase 프로젝트 설정 및 의존성 추가

### 2.1. Firebase 프로젝트 준비

1.  **Firebase 콘솔 설정**:
    *   Firebase 콘솔에서 새 프로젝트를 생성하고 Android 앱을 추가합니다.
    *   앱 패키지 이름(`com.openknights.app`)을 등록합니다.
2.  **`google-services.json` 추가**:
    *   다운로드한 `google-services.json` 파일을 `openknights/app/` 디렉터리에 배치합니다.
3.  **Firebase 서비스 활성화**:
    *   **Authentication**: '이메일/비밀번호' 로그인 공급자를 활성화합니다.
    *   **Firestore Database**: Firestore 데이터베이스를 생성합니다. (테스트 모드 또는 프로덕션 모드 선택)
    *   **Storage**: Firebase Storage를 활성화합니다.

### 2.2. Gradle 의존성 설정

`gradle/libs.versions.toml` 파일과 `build.gradle.kts`에 Firebase 관련 라이브러리를 추가합니다.

**`libs.versions.toml`**
```toml
[versions]
firebaseBom = "34.1.0"
coroutines-play-services = "1.8.1"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore-ktx = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-storage-ktx = { group = "com.google.firebase", name = "firebase-storage-ktx" }

# Task.await() 확장 함수를 사용하기 위한 의존성
coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines-play-services" }
```

**`app/build.gradle.kts`**
```kotlin
dependencies {
    // Firebase BoM (Bill of Materials) - 버전 통합 관리
    implementation(platform(libs.firebase.bom))

    // 필요한 Firebase SDK 추가
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // Coroutines 지원 라이브러리
    implementation(libs.coroutines.play.services)
}
```

### 2.3. Firebase 앱 초기화

`Application` 클래스에서 Firebase SDK를 초기화합니다.

**`OpenKnightsApplication.kt`**
```kotlin
import com.google.firebase.FirebaseApp

class OpenKnightsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
```

**`AndroidManifest.xml`**
```xml
<application
    android:name=".OpenKnightsApplication"
    ...
>
    ...
</application>
```

---

## 3. Firebase Authentication: 사용자 인증

### 3.1. 기능 목표

Firebase Authentication을 사용하여 사용자 회원가입, 로그인, 로그아웃 기능을 구현하고, 앱 내비게이션과 통합하여 로그인 상태에 따라 UI를 동적으로 제어합니다.

### 3.2. 구현

*   **`feature:auth` 모듈**: 인증 관련 UI(`RegisterScreen`, `LoginScreen`)와 비즈니스 로직(`AuthViewModel`)을 담당합니다.
*   **`AuthViewModel`**: `FirebaseAuth.getInstance()`를 통해 인증 인스턴스를 관리하며, 회원가입, 로그인, 로그아웃 함수를 구현합니다. `AuthStateListener`를 통해 로그인 상태 변경을 감지하고 UI에 상태(`isLoggedIn`, `currentUserEmail`)를 제공합니다.
*   **`app` 모듈 통합**: 앱 시작 시 `isLoggedIn` 상태를 확인하여 로그인 화면 또는 메인 화면으로 안내합니다. 메뉴에서도 로그인 상태에 따라 '로그인' 또는 '로그아웃' 옵션을 동적으로 보여줍니다.

---

## 4. Firebase Firestore: 데이터 관리

### 4.1. 데이터 접근 정책 및 보안 규칙

Firestore의 데이터 접근은 **보안 규칙(Security Rules)**에 의해 제어됩니다. 모든 데이터 요청은 이 규칙의 허가를 받아야 합니다.

**주요 규칙 시나리오:**

1.  **인증된 사용자만 접근 허용 (가장 일반적)**
    ```rules
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        // users 컬렉션: 자신의 문서만 읽고 쓸 수 있음
        match /users/{userId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
        // projects, contests 등 다른 컬렉션: 로그인한 모든 사용자가 읽기 가능
        match /projects/{projectId} {
          allow read: if request.auth != null;
          allow write: if false; // 쓰기는 서버나 특정 역할만 가능하도록 제한
        }
      }
    }
    ```
    *   `request.auth != null`: 사용자가 로그인 상태인지 확인합니다.
    *   `request.auth.uid == userId`: 요청자의 UID와 문서의 ID가 일치하는지 확인하여 본인만 접근하도록 제한합니다.

2.  **로그인 없이 일부 데이터 읽기 허용 (Guest Access)**
    ```rules
    // 공지사항(notices) 컬렉션은 누구나 읽기 가능
    match /notices/{noticeId} {
      allow read: if true;
      allow write: if false; // 쓰기는 관리자만
    }
    ```

**현재 규칙 확인 방법**:
*   Firebase 콘솔의 **Firestore Database > 규칙(Rules)** 탭에서 직접 확인합니다.
*   프로젝트 내 `firestore.rules` 파일을 확인합니다.

### 4.2. 사용자 정보 저장 및 조회

**목표**: 회원가입 시 사용자의 프로필 정보를 Firestore `users` 컬렉션에 저장하고, `UserScreen`에서 모든 사용자 목록을 조회하여 표시합니다.

**구현**: 

1.  **`core:model`**: `User` 데이터 클래스에 `uid`, `email`, `name` 등 Firestore에 저장할 필드를 정의합니다.
2.  **`data:user` 모듈**: `UserRepository`를 구현하여 Firestore와 통신합니다.
    *   `addUserProfile(user: User)`: 회원가입 성공 후 `AuthViewModel`에서 호출되어 `users` 컬렉션에 사용자 정보를 저장합니다. 문서 ID는 사용자의 `uid`를 사용합니다.
    *   `getAllUsers(): Flow<List<User>>`: `users` 컬렉션의 모든 문서를 실시간으로 구독(snapshot listener)하거나 한 번만 가져와 `UserScreen`에 제공합니다.
3.  **`feature:user` 모듈**: `UserViewModel`이 `UserRepository`를 통해 사용자 목록을 가져와 UI에 표시합니다.

### 4.3. 초기 데이터(Fake Data) 저장

개발 및 테스트를 위해 로컬의 JSON 데이터를 Firestore에 직접 저장할 수 있습니다. 디버그 빌드에서만 활성화되는 버튼 등을 통해 이 기능을 구현할 수 있습니다.

**예시 코드 (`ViewModel`)**:
```kotlin
fun saveUsersToFirestore(users: List<User>) {
    val db = Firebase.firestore
    viewModelScope.launch {
        users.forEach {
            try {
                db.collection("users").document(it.uid).set(it).await()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}
```

---

## 5. Firebase Storage: 파일 관리

### 5.1. 기능 목표

Firebase Storage를 사용하여 사용자가 프로필 이미지를 업로드하고, 업로드된 이미지를 다른 사용자들이 볼 수 있도록 다운로드하여 화면에 표시합니다.

### 5.2. 구현

1.  **Storage 보안 규칙 설정**:
    *   Firebase 콘솔에서 Storage 보안 규칙을 설정하여 인증된 사용자만 자신의 프로필 이미지를 업로드/수정할 수 있도록 제한합니다.
    ```rules
    service firebase.storage {
      match /b/{bucket}/o {
        // 프로필 이미지: `images/users/{userId}/profile.jpg` 경로
        match /images/users/{userId}/{fileName} {
          allow read: if true; // 누구나 프로필 이미지 읽기 가능
          allow write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```

2.  **`data:user` 모듈 (`UserRepository`)**: 
    *   `uploadProfileImage(userId: String, imageUri: Uri): Flow<String>`: 이미지를 Storage에 업로드하고, 성공 시 다운로드 URL을 반환합니다.
    *   `updateUserProfileImageUrl(userId: String, imageUrl: String)`: `uploadProfileImage` 성공 후 반환된 URL을 Firestore의 해당 사용자 문서에 업데이트합니다.

3.  **`feature:user` 모듈**: 
    *   `UserScreen`에서 갤러리 이미지 선택(`ActivityResultLauncher`), 이미지 업로드 요청(`ViewModel` 호출) UI를 구현합니다.
    *   `User` 객체의 `profileImageUrl` 필드와 이미지 로딩 라이브러리(Coil, Glide 등)를 사용하여 Storage에 저장된 이미지를 화면에 표시합니다.


---

# Firebase 연동 설계 문서

## 1단계: 현재 구조에 안전하게 Firebase 연동하기 (Android 전용)

### 1.1 Firebase 로그인/로그아웃 기능 구현

**목표**: Firebase Authentication을 활용하여 사용자 회원가입, 로그인, 로그아웃 기능을 구현하고, 앱의 UI와 내비게이션에 통합합니다. 또한, 로그인된 사용자의 기본 정보를 `UserScreen`에 표시합니다.

**주요 구성 요소**:

*   **`:openknights:feature:auth` 모듈**: 인증 관련 UI (회원가입, 로그인 화면) 및 비즈니스 로직 (AuthViewModel)을 캡슐화합니다.
*   **`:openknights:app` 모듈**: 앱의 메인 내비게이션을 관리하며, 인증 모듈의 화면을 통합하고 로그인 상태에 따라 UI를 동적으로 변경합니다.
*   **Firebase Authentication SDK**: 실제 사용자 인증 처리를 담당합니다.
*   **`:openknights:feature:user` 모듈**: `UserScreen`의 UI와 `UserViewModel`을 포함하여 사용자 데이터를 표시합니다.

**구현 단계**:

1.  **Firebase 프로젝트 설정 (사용자 사전 작업)**:
    *   Firebase 콘솔에서 새로운 프로젝트를 생성하고, Android 앱을 추가합니다.
    *   앱의 패키지 이름(`com.openknights.app`)을 등록하고, `google-services.json` 파일을 다운로드하여 `openknights/app/` 디렉토리 내에 배치합니다.
    *   Firebase Authentication 서비스에서 "이메일/비밀번호" 로그인 공급자를 활성화합니다.

2.  **`:openknights:feature:auth` 모듈 생성**:
    *   `openknights/feature/auth` 경로에 새로운 Android 라이브러리 모듈을 생성합니다.
    *   `build.gradle.kts` 파일에 다음 의존성을 추가합니다:
        *   `implementation(project(":openknights:core:designsystem"))`
        *   Compose UI 관련 라이브러리 (`androidx.compose.bom`, `androidx.ui`, `androidx.material3`, `androidx.lifecycle.viewmodel.compose`)
        *   Firebase 관련 라이브러리 (`platform(libs.firebase.bom)`, `api(libs.firebase.auth.ktx)`)
    *   루트 `settings.gradle.kts` 파일에 `:openknights:feature:auth` 모듈을 포함시킵니다.
    *   `gradle/libs.versions.toml` 파일에 `firebaseBom` 버전과 `firebase-auth-ktx` 라이브러리 별칭을 추가하여 버전 카탈로그를 통해 관리하도록 설정합니다.

3.  **`AuthViewModel.kt` 구현**:
    *   `openknights/feature/auth/src/main/java/com/openknights/feature/auth/AuthViewModel.kt` 파일에 `ViewModel`을 생성합니다.
    *   `FirebaseAuth.getInstance()`를 통해 Firebase 인증 인스턴스를 가져옵니다.
    *   사용자 등록 (`createUserWithEmailAndPassword`), 로그인 (`signInWithEmailAndPassword`), 로그아웃 (`signOut`) 함수를 구현합니다.
    *   인증 상태 (`AuthUiState` - 로딩, 성공, 에러 메시지) 및 로그인 여부 (`isLoggedIn: StateFlow<Boolean>`), 현재 로그인된 사용자 이메일 (`currentUserEmail: StateFlow<String?>`)을 `StateFlow`로 노출하여 UI에서 관찰할 수 있도록 합니다.
    *   `FirebaseAuth.AuthStateListener`를 사용하여 로그인 상태 변경을 감지하고 `isLoggedIn` 및 `currentUserEmail`을 업데이트합니다.

4.  **인증 UI 화면 구현**:
    *   **`RegisterScreen.kt`**: `openknights/feature/auth/src/main/java/com/openknights/feature/auth/RegisterScreen.kt` 파일에 이메일, 비밀번호 입력 필드와 회원가입 버튼을 포함하는 Composable 화면을 구현합니다. 회원가입 성공 시 로그인 화면으로 이동합니다.
    *   **`LoginScreen.kt`**: `openknights/feature/auth/src/main/java/com/openknights/feature/auth/LoginScreen.kt` 파일에 이메일, 비밀번호 입력 필드, 로그인 버튼, 그리고 회원가입 화면으로 이동하는 텍스트 버튼을 포함하는 Composable 화면을 구현합니다. 로그인 성공 시 메인 화면으로 이동합니다.

5.  **`:openknights:app` 모듈 통합**:
    *   `openknights/app/build.gradle.kts` 파일에 `implementation(project(":openknights:feature:auth"))` 의존성을 추가합니다.
    *   `openknights/app/src/main/java/com/openknights/app/OpenKnightsApp.kt` 파일에서:
        *   `ScreenEntry` sealed interface에 `RegisterScreenEntry`와 `LoginScreenEntry`를 추가합니다.
        *   앱 시작 시 `AuthViewModel`의 `isLoggedIn` 상태를 확인하여 초기 내비게이션 스택을 동적으로 설정합니다 (로그인 상태가 아니면 `LoginScreenEntry`, 로그인 상태면 `ContestListScreenEntry`).
        *   햄버거 메뉴의 "사용자 등록" 및 "로그인/로그아웃" `DropdownMenuItem`의 `onClick` 로직을 수정하여 해당 인증 화면으로 이동하도록 합니다. "로그인"과 "로그아웃" 메뉴는 `isLoggedIn` 상태에 따라 상호 배타적으로 표시됩니다.
        *   `NavDisplay`의 `entryProvider`에 `RegisterScreenEntry`와 `LoginScreenEntry`에 대한 `when` 분기를 추가하여 해당 화면 Composable을 호출합니다.
        *   `NoticeScreenEntry` 호출 시 `AuthViewModel`의 `currentUserEmail`과 `signOut()` 함수를 파라미터로 전달하여 `NoticeScreen` 내에서 로그아웃 기능을 사용할 수 있도록 합니다.
        *   알림 아이콘의 `enabled` 상태와 `tint` 색상을 `isLoggedIn` 값에 따라 동적으로 변경하여 로그인 상태를 시각적으로 나타냅니다.

6.  **Firebase 초기화**:
    *   `openknights/app/src/main/java/com/openknights/app/OpenKnightsApplication.kt` 파일의 `onCreate()` 메서드에서 `FirebaseApp.initializeApp(this)`를 호출하여 Firebase SDK를 초기화합니다。
    *   `AndroidManifest.xml`의 `<application>` 태그에 `android:name=".OpenKnightsApplication"`을 추가하여 이 `Application` 클래스를 사용하도록 지정합니다.
    *   `AndroidManifest.xml`의 `<application>` 태그에 `android:enableOnBackInvokedCallback="true"`를 추가하여 Android 13+ 뒤로 가기 동작 경고를 해결합니다.

**발생했던 문제 및 해결**:

*   **`Unresolved reference 'hiltViewModel'`**: `app_18_fake_store` 모듈에서 발생한 문제로, `hilt-navigation-compose` 의존성 추가로 해결되었습니다. (이 기능은 `openknights` 모듈에는 직접 적용되지 않았습니다.)
*   **`Unresolved reference 'google'` (in `AuthViewModel.kt`)**: `feature:auth` 모듈의 `build.gradle.kts`에 `implementation(platform(libs.firebase.bom))`과 `api(libs.firebase.auth.ktx)`가 올바르게 선언되었는지 확인하고, `settings.gradle.kts`에 `google()` 저장소가 포함되어 있는지 확인하여 해결되었습니다.
*   **`Smart cast impossible`**: `uiState.error`와 같은 `State` 위임 속성의 널 검사 후 스마트 캐스트가 되지 않는 문제로, `?.let { ... }` 또는 널이 아님이 보장된 로컬 변수에 할당하여 해결되었습니다.
*   **`isLoggedIn` 값 미반영 / 초기 화면 문제**: `AuthViewModel`에 `AuthStateListener`를 추가하여 `isLoggedIn` 및 `currentUserEmail`을 실시간으로 업데이트하고, `OpenKnightsApp`에서 `isLoggedIn` 값에 따라 초기 `backStack`을 설정하도록 변경하여 해결되었습니다.
*   **`currentUser` / `signOut` Unresolved reference**: `AuthViewModel`에 `currentUserEmail: StateFlow<String?>` 속성과 `signOut()` 함수를 추가하여 해결되었습니다. (이전 단계에서 `currentUserEmail` 추가는 완료되었고, `signOut` 함수 추가는 이번 단계에서 진행되었습니다.)
*   **`FirebaseApp` Unresolved reference**: `:openknights:app` 모듈의 `build.gradle.kts`에 `implementation("com.google.firebase:firebase-analytics")` 의존성을 활성화하여 해결되었습니다.
*   **`OnBackInvokedCallback` 경고**: `AndroidManifest.xml`에 `android:enableOnBackInvokedCallback="true"` 속성을 추가하여 해결되었습니다.

---

### 1.2 Firebase Authentication에 등록된 모든 사용자의 기본 정보(이메일)를 가져와 UserScreen에 표시하는 기능 (Firestore 연동 필수)

**목표**: Firebase Authentication에 등록된 모든 사용자의 기본 정보(이메일)를 가져와 `UserScreen`에 `LazyColumn`으로 표시합니다. 이 기능은 **Firebase Firestore 연동을 통해 구현**됩니다.

**주요 구성 요소**:

*   **Firebase Authentication SDK**: 사용자 인증 및 UID 획득에 사용됩니다.
*   **Firebase Firestore SDK**: 모든 사용자의 프로필 정보를 저장하고 조회하는 데 사용됩니다.
*   **`:openknights:core:model` 모듈**: 사용자 프로필을 위한 데이터 모델을 정의합니다.
*   **`:openknights:data:user` 모듈 (신규)**: Firestore와 통신하여 사용자 데이터를 가져오는 데이터 계층 (UserRepository)을 구현합니다.
*   **`:openknights:feature:user` 모듈**: `UserScreen`의 UI와 `UserViewModel`을 포함하여 사용자 데이터를 표시합니다.

**구현 단계**:

1.  **Firebase Firestore 설정 (사용자/관리자 사전 작업)**:
    *   Firebase 콘솔에서 Firestore Database를 생성합니다. (Test mode 또는 Production mode 선택)
    *   **관리자 작업**: Firestore에 `users`라는 이름의 컬렉션(Collection)을 생성합니다. 이 컬렉션은 각 사용자의 프로필 문서(Document)를 저장할 공간이 됩니다. 각 문서의 ID는 해당 사용자의 Firebase Authentication UID와 동일하게 설정하는 것을 권장합니다.
    *   `google-services.json` 파일이 `openknights/app/`에 올바르게 위치하는지 다시 확인합니다.

2.  **`openknights/app/build.gradle.kts` 수정**:
    *   Firebase Firestore SDK 의존성을 추가합니다.
        ```gradle
        implementation(libs.firebase.firestore.ktx)
        ```

3.  **`gradle/libs.versions.toml` 수정**:
    *   `[versions]` 섹션에 `firebaseFirestore = "버전"`을 추가합니다. (예: `firebaseFirestore = "24.11.4"`)
    *   `[libraries]` 섹션에 `firebase-firestore-ktx = { group = "com.google.firebase", name = "firebase-firestore-ktx", version.ref = "firebaseFirestore" }`를 추가합니다.

4.  **`:openknights:core:model` 모듈 수정**:
    *   `openknights/core/model/src/main/java/com/openknights/core/model/User.kt` 파일에 사용자 프로필을 나타내는 `data class User(...)`를 정의합니다. (예: `uid: String`, `email: String`, `name: String? = null`, `profileImageUrl: String? = null` 등)
        *   **참고**: `uid` 필드는 Firestore 문서 ID와 일치하도록 필수적으로 포함하는 것이 좋습니다.

5.  **`:openknights:data:user` 모듈 생성**:
    *   `openknights/data/user` 경로에 새로운 Android 라이브러리 모듈을 생성합니다.
    *   `build.gradle.kts` 파일에 다음 의존성을 추가합니다:
        *   `implementation(project(":openknights:core:model"))`
        *   Firebase Firestore SDK (`implementation(libs.firebase.firestore.ktx)`)
        *   Kotlin Coroutines (`implementation(libs.kotlinx.coroutines.play.services)`) (Firestore에서 Flow를 사용하기 위함)
    *   루트 `settings.gradle.kts` 파일에 `:openknights:data:user` 모듈을 포함시킵니다.
    *   `openknights/data/user/src/main/java/com/openknights/data/user/UserRepository.kt` 파일에 `UserRepository` 인터페이스와 구현체를 정의합니다.
        *   `getAllUsers(): Flow<List<User>>`: Firestore의 `users` 컬렉션에서 모든 사용자 프로필을 가져오는 함수
        *   `addUserProfile(user: User)`: 새로운 사용자가 회원가입할 때 Firestore에 프로필을 저장하는 함수 (AuthViewModel에서 호출)

6.  **`:openknights:feature:auth` 모듈 수정 (선택 사항)**:
    *   `AuthViewModel.kt`의 `registerUser` 함수에서 사용자 회원가입 성공 후, `UserRepository.addUserProfile()`을 호출하여 Firestore에 사용자 프로필을 저장하는 로직을 추가합니다.

7.  **`:openknights:feature:user` 모듈 수정**:
    *   `openknights/feature/user/build.gradle.kts` 파일에 `implementation(project(":openknights:data:user"))` 의존성을 추가합니다.
    *   `openknights/feature/user/src/main/java/com/openknights/feature/user/UserViewModel.kt` 파일에 `UserViewModel`을 수정합니다.
        *   생성자를 통해 `UserRepository`를 주입받습니다.
        *   `UserRepository.getAllUsers()`를 호출하여 모든 사용자 목록을 가져와 `StateFlow<List<User>>` 등으로 UI에 노출합니다.
    *   `openknights/feature/user/src/main/java/com/openknights/feature/user/UserScreen.kt` 파일에서 `UserViewModel`을 사용하여 `LazyColumn`으로 모든 사용자 정보를 표시하도록 UI를 업데이트합니다.

8.  **`:openknights:app` 모듈 통합**: (선택 사항, `UserViewModel`이 `UserRepository`를 직접 생성하는 경우)
    *   `openknights/app/build.gradle.kts` 파일에 `implementation(project(":openknights:data:user"))` 의존성을 추가합니다.
    *   `OpenKnightsApp.kt`에서 `UserScreenEntry`를 호출할 때 `UserViewModel`을 생성하고 필요한 의존성을 전달합니다. (또는 `UserScreen` 내부에서 `viewModel()`을 사용하고, `UserViewModel`이 `UserRepository`를 직접 주입받도록 할 수도 있습니다.)

**참고**: 이 기능은 Firebase Authentication의 보안 정책상 클라이언트 SDK만으로는 직접 구현할 수 없으며, **Firestore와 같은 데이터베이스를 통한 사용자 프로필 저장 및 조회가 필수적**입니다.
