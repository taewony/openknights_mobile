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
