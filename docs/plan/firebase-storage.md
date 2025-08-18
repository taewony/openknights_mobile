# Firebase Storage 연동 및 기능 추가 계획
- draft at: 2025-08-18

## 1. Git 브랜치 전략

1.  **`firebase-store` 브랜치 생성**: 현재 `main` 브랜치(Firestore 연동 완료 시점)를 기반으로 `firebase-store` 브랜치를 생성하여 현재 상태를 백업합니다.
    ```bash
    git branch firebase-store
    ```
2.  **`firebase-storage` 피처 브랜치 생성**: 신규 기능 개발을 위한 `firebase-storage` 브랜치를 생성하고 해당 브랜치로 이동합니다.
    ```bash
    git checkout -b firebase-storage
    ```

## 2. 신규 기능 개발 (firebase-storage 브랜치)

### 2.1. 의존성 및 데이터 모델 설계

1.  **라이브러리 의존성 추가**: `core:data` 또는 `app` 모듈의 `build.gradle.kts` 파일에 **Firebase Storage**와 **Coil** 라이브러리를 추가합니다.
    ```kotlin
    // build.gradle.kts
    implementation(libs.firebase.storage)
    implementation(libs.coil.compose) // for AsyncImage
    ```
2.  **데이터 모델 수정 (`core/model`)**: Firestore와 직접 통신하는 `User.kt`에 `profileImageUrl` 필드만 추가합니다. (UI 상태인 `isCurrentUser`는 포함하지 않습니다.)

3.  **UI 상태 모델 정의 (`feature` 또는 `core/ui`)**: UI에서 사용될 `UserUiState` 클래스를 새로 정의합니다. 이 클래스는 `User`의 모든 필드와 UI 상태인 `isCurrentUser`를 포함합니다.

4.  **Mapper 로직 구현 (ViewModel)**: Repository에서 `User` 목록을 가져온 후, ViewModel에서 `User`를 `UserUiState`로 변환하는 Mapper를 구현합니다. 이 과정에서 현재 로그인된 사용자인지 비교하여 `isCurrentUser` 값을 설정합니다.

### 2.2. 프로필 표시 및 편집 화면 구현

1.  **`UserCard` 컴포저블 구현**: `docs/09_feature_user_design.md`의 개선안에 따라 `core/ui` 모듈에 `UserCard.kt`를 구현합니다.
    *   `AsyncImage`를 사용해 프로필 이미지를 표시합니다. (Placeholder: `Icons.Default.Person`)
    *   `isCurrentUser`가 `true`일 때만 카드 전체가 클릭 가능하도록 구현합니다.

2.  **유저 목록 화면 (`UserListScreen`) 개선**:
    *   기존의 유저 목록 아이템을 새로 구현한 `UserCard`로 교체합니다.
    *   `UserCard`의 `onCardClick` 이벤트가 발생하면, 해당 유저의 ID를 가지고 **프로필 편집 화면(`ProfileEditScreen`)**으로 이동하는 네비게이션 로직을 구현합니다.

3.  **프로필 편집 화면 (`ProfileEditScreen`) 신규 생성**:
    *   **UI**: 이름과 자기소개를 수정할 수 있는 `TextField`, 프로필 이미지를 변경할 수 있는 `Button` 또는 `Image`, 변경사항을 저장하는 `Button`을 포함하는 새로운 화면을 만듭니다.
    *   **로직**: 
        *   화면 진입 시 전달받은 사용자 ID로 기존 프로필 정보를 불러와 표시합니다.
        *   이미지 변경 버튼 클릭 시 `ActivityResultLauncher`를 사용해 갤러리에서 이미지를 선택합니다.
   1. `ProfileEditScreen.kt` 파일 생성: 프로필 편집 화면의 임시(placeholder) 파일을 먼저 생성합니다.
   2. 네비게이션 연결: OpenKnightsApp.kt에서 UsersScreen의 카드 클릭 시, 새로 만든 ProfileEditScreen으로
      이동하도록 네비게이션 로직을 수정합니다.
   3. `UsersScreen.kt` 개선: UserCard 디자인을 09_feature_user_design.md에 설계된 내용대로 개선합니다.
   
### 2.3. 프로필 정보 업데이트 로직 구현

1.  **ViewModel 로직 추가**: `ProfileEditViewModel`을 생성하여 `ProfileEditScreen`의 상태(이름, 소개, 이미지 URI 등)를 관리합니다.
2.  **Repository 로직 추가**: `UserRepository`에 프로필 정보를 업데이트하는 `updateUserProfile` 함수를 구현합니다.
    *   새로운 프로필 이미지가 있다면, Firebase Storage에 이미지를 먼저 업로드합니다.
    *   업로드 성공 시 받은 이미지 URL과 변경된 이름/소개 텍스트를 함께 Firestore의 `users` 문서에 업데이트합니다.
	
－ ProfileEditScreen.kt의 기능을 구현하겠습니다. 이 작업은 ViewModel, Repository, 그리고 UI
  컴포넌트의 수정 및 생성을 포함하는 큰 작업입니다.

  － 다음과 같은 단계로 진행하겠습니다.

   1. `UserRepository` 및 `UserRepositoryImpl` 수정: 사용자 프로필 업데이트 및 이미지 업로드 기능을 추가합니다.
   2. `ProfileEditViewModel.kt` 생성: 프로필 편집 화면의 로직과 상태를 관리하는 ViewModel을 구현합니다.
   3. `ProfileEditScreen.kt` 구현: UI 컴포넌트들을 배치하고 ViewModel과 연결합니다.


－ ProfileEditScreen.kt의 기능을 구현하겠습니다. 이 작업은 ViewModel, Repository, 그리고 UI
  컴포넌트의 수정 및 생성을 포함하는 큰 작업입니다.

  다음과 같은 단계로 진행하겠습니다.

   1. `UserRepository` 및 `UserRepositoryImpl` 수정: 사용자 프로필 업데이트 및 이미지 업로드 기능을 추가합니다.
   2. `ProfileEditViewModel.kt` 생성: 프로필 편집 화면의 로직과 상태를 관리하는 ViewModel을 구현합니다.
   3. `ProfileEditScreen.kt` 구현: UI 컴포넌트들을 배치하고 ViewModel과 연결합니다.
   
✦ Android 에뮬레이터의 갤러리에 사진을 넣는 방법은 여러 가지가 있습니다. 가장 쉽고 흔히 사용되는 방법들을
  알려드리겠습니다.

   1. 드래그 앤 드롭 (가장 쉬움):
       * PC에 있는 이미지 파일(JPG, PNG 등)을 실행 중인 에뮬레이터 화면 위로 직접 드래그하여 놓습니다.
       * 에뮬레이터가 자동으로 이미지를 인식하고 갤러리(Photos 앱)에 저장합니다. 보통 상단에 "Saving image..."
          같은 알림이 뜹니다.

   2. Android Studio Device File Explorer 사용:
       * Android Studio에서 View > Tool Windows > Device File Explorer를 엽니다.
       * 에뮬레이터의 파일 시스템에서 sdcard > DCIM > Camera 또는 sdcard > Pictures 경로로 이동합니다.
       * 툴바에 있는 "Upload" (위쪽 화살표 아이콘) 버튼을 클릭하여 PC의 이미지 파일을 선택하고 업로드합니다.

   3. ADB (Android Debug Bridge) 명령어 사용:
       * 터미널이나 명령 프롬프트를 엽니다.
       * 다음 명령어를 사용하여 PC의 파일을 에뮬레이터로 푸시합니다.
   1         adb push [PC의 이미지 파일 경로] /sdcard/DCIM/Camera/
          예시: adb push C:\Users\YourUser\Pictures\my_test_photo.jpg /sdcard/DCIM/Camera/
		  
## 3. feature 브랜치를 main에 merge
```
git checkout main
git merge firebase-storage
```


## 4. 로컬 및 원격 브랜치 확인 및 동기화

### 4.1. 브랜치 확인 방법

- **로컬 브랜치 확인**: 현재 PC(Local)에 있는 브랜치 목록을 봅니다. `*`는 현재 선택된 브랜치를 의미합니다.
  ```bash
  git branch
  ```

- **원격 브랜치 확인**: 원격 저장소(Remote)에 있는 브랜치 목록을 봅니다.
  ```bash
  git branch -r
  ```

- **모든 브랜치 확인**: 로컬과 원격의 모든 브랜치 목록을 함께 봅니다.
  ```bash
  git branch -a
  ```

### 4.2. 원격 저장소에 로컬 변경사항 반영 (Push)

로컬에서 생성하고 작업한 브랜치는 원격 저장소에 자동으로 반영되지 않습니다. `git push` 명령어를 사용해 직접 올려야 합니다. 아래 명령어들은 `3. 브랜치 병합 및 푸시` 단계에서 실행하며, 각 브랜치의 로컬 변경 이력을 원격 저장소에 반영합니다.

- **`firebase-store` 브랜치 푸시**
  ```bash
  git push origin firebase-store
  ```

- **`firebase-storage` 브랜치 푸시**
  ```bash
  git push origin firebase-storage
  ```

- **`main` 브랜치 푸시**
  ```bash
  git push origin main
  ```