# OpenKnights App (우송대학교 SW 경진대회 플랫폼)


**OpenKnights**는 우송대학교에서 열리는 다양한 SW 경진대회의 모든 것을 담는 공식 아카이브 및 운영 플랫폼입니다. 이 프로젝트는 `모바일앱실습` 수업의 일환으로 진행되며, `DroidKnights` 앱의 아키텍처를 참고하여 현대적인 안드로이드 기술 스택으로 구현합니다.

---

## 🏛️ 아키텍처 및 설계 원칙

본 프로젝트는 유지보수성과 확장성을 높이기 위해 다음과 같은 아키텍처와 설계 원칙을 따릅니다.

* **Multi-Module Architecture**: 기능별, 역할별로 코드를 분리하여 모듈의 재사용성을 높이고 빌드 시간을 단축합니다.
* **MVVM (Model-View-ViewModel)**: UI와 비즈니스 로직을 분리하여 각 부분의 독립적인 개발과 테스트를 용이하게 합니다.
* **Android Architecture Guidelines 준수**: Google에서 권장하는 [공식 아키텍처 가이드](https://developer.android.com/topic/architecture)를 최대한 준수합니다.
* **DI 프레임워크 최소화**: Hilt/Dagger와 같은 DI 프레임워크 대신, 순수 Kotlin 코드로 의존성을 관리하여 빌드 복잡성을 낮추고 프로젝트를 가볍게 유지합니다.
* **선언형 UI (Declarative UI)**: Jetpack Compose만을 사용하여 UI를 개발하며, 기존 View 시스템은 사용하지 않습니다.

---

## 🏗️ 모듈 구조 (Module Structure)

프로젝트는 역할에 따라 다음과 같은 멀티 모듈 구조로 구성됩니다.

```

openknights\_mobile/
├── app/                  \# 📱 Android Application 모듈 (메인 진입점)
│
├── core/
│   ├── designsystem/     \# 🎨 공통 디자인 시스템 (테마, 색상, 타이포그래피, 공용 컴포넌트)
│   ├── data/             \# 💾 데이터 처리 계층 (Repository, DataSource, Firebase 연동)
│   └── model/            \# 📦 공통 데이터 모델 (DTO, Entity)
│
├── feature/
│   ├── auth/             \# 🔑 로그인, 회원가입 기능
│   ├── contests/         \# 🏆 대회 목록, 상세 정보 기능
│   ├── projects/         \# 🚀 출품작 목록, 상세 정보 기능
│   └── users/            \# 👤 사용자 프로필 기능
│
└── build.gradle.kts      \# 📜 프로젝트 레벨 빌드 스크립트

````

---

## 🛠️ 기술 스택 및 개발 환경

* **IDE**: Android Studio 2025.1.2
* **Android API**:
    * `targetSdk`: 36
    * `minSdk`: 32
* **Build & Language**:
    * `Kotlin`: 2.0.21
    * `AGP (Android Gradle Plugin)`: 8.12.0
* **UI Toolkit**:
    * `Jetpack Compose`: Material 3
    * `Navigation`: Custom BackStack 기반 (타입 세이프 미적용)
* **Backend & Database**:
    * `Firebase`: Authentication, Firestore, Storage, FCM
    * `Room`: (추후 오프라인 지원을 위해 도입 예정)

---

## 🗂️ 데이터 모델 (Firestore Collection)

Firestore 데이터베이스는 다음과 같은 구조로 설계되었습니다.

* **`contests`**: 대회의 모든 정보를 담는 허브 컬렉션. (예: 2025년 1학기 오픈소스 경진대회)
    * `schedules`: 대회 주요 일정 (접수, 마감, 발표 등)
    * `teams`: 해당 대회에 참가한 팀 목록
* **`projects`**: 특정 팀이 특정 대회에 제출한 작품 정보.
    * `prd_docs`: 작품 기획 문서
    * `images`: 화면 예시 이미지
    * `attachments`: 발표 자료, 데모 영상 등
* **`users`**: 모든 사용자의 마스터 정보.
* **`participants`**: `contest`, `user`, `team`을 연결하여 누가 어떤 역할로 참여했는지 정의.
* **`roles`**: 사용자의 역할(심사위원, 스태프, 지도교수 등)을 정의.

---

## 🧭 내비게이션 처리 방식

본 프로젝트는 빌드 복잡성을 줄이고 디버깅을 용이하게 하기 위해, Navigation Compose의 타입 세이프 라우팅(KSP 기반) 대신 **직접 구현한 BackStack 기반 내비게이션**을 사용합니다.

* **Route 정의**: `sealed interface`를 사용하여 각 화면을 타입으로 안전하게 정의합니다.
    ```kotlin
    sealed interface AppRoute
    data object Home : AppRoute
    data class ProjectDetail(val id: String) : AppRoute
    ```
* **BackStack 관리**: `mutableStateListOf<AppRoute>`를 사용하여 화면 이동 기록을 직접 관리합니다.
* **화면 전환**: `backStack.add(ProjectDetail(id = "some-id"))` 와 같이 리스트에 직접 Route를 추가하여 화면을 전환합니다.
* **장점**: KSP 의존성 제거로 인한 빌드 속도 향상 및 안정성 확보, 단순하고 직관적인 코드.
* **단점**: Deep Link 연동 시 별도의 처리 필요.

---

## 🔄 데이터 소스 교체 전략 (Fake Data ↔ Firebase)

본 프로젝트는 **Repository Pattern**을 사용하여 데이터 소스를 유연하게 교체할 수 있도록 설계되었습니다. 이를 통해 개발 초기에는 가짜 데이터(Fake Data)로 빠르게 UI를 개발하고, 추후 실제 Firebase 데이터 소스로 손쉽게 전환할 수 있습니다.

1.  **역할 분리**:

      * **`:core:model`**: 데이터의 \*\*모양(구조)\*\*만 정의합니다. (`Project` data class 등)
      * **`:core:data`**: 데이터를 \*\*어떻게 가져올지(구현)\*\*를 책임집니다. (`ProjectRepository` 인터페이스 및 구현체)

2.  **구현 방법**:

    1.  `:core:data`에 `ProjectRepository`라는 **인터페이스**를 정의합니다.
    2.  개발 초기에는 가짜 데이터를 반환하는 `FakeProjectRepository`를 구현하여 사용합니다.
    3.  이후 Firestore 연동이 필요할 때, 동일한 인터페이스를 따르는 `FirebaseProjectRepository`를 구현합니다.
    4.  ViewModel 등에서는 실제 구현체가 아닌 `ProjectRepository` 인터페이스에만 의존하므로, **어떤 구현체를 주입할지만 변경**하면 데이터 소스 전환이 완료됩니다.

이러한 구조 덕분에 데이터 소스가 변경되어도 **UI나 비즈니스 로직 관련 코드는 전혀 수정할 필요가 없습니다.**


## 🚀 시작하기 (Getting Started)

### 1. 초기 개발 환경 설정

```Kotlin
// build.gradle.kts (:app)
android {
    namespace = "com.openknights.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.openknights.mobile"
	}
}

// settings.gradles.kts
rootProject.name = "openknights_mobile"
include(":app")
include(":core:designsystem")
include(":core:ui")
include(":core:data")
include(":core:model")
include(":feature")
```

```bash
# 1. :core:designsystem 모듈 생성 후, app 모듈에서 해당 테마를 사용하도록 수정
# 2. :core:model 모듈에 Firestore 데이터 구조에 맞는 데이터 클래스 정의
# 3. :core:data 모듈에 임시 데이터(Fake Data)를 제공하는 Repository 구현
# 4. Repository "데이터 창고 관리자" 역할
UI(화면)나 비즈니스 로직은 데이터가 어디서 오는지(Fake Data인지, 서버 API인지, 로컬 DB인지) 신경 쓸 필요 없이,
그냥 "데이터 창고 관리자"에게 필요한 데이터를 요청하기만 하면 됩니다.
# 5. 공통 UI 요소를 정의한 :core:ui 구현
# 6. 앱의 시작점인 :app 모듈 및 OpenKnightsApp.kt 구현
# 7. Feature 모듈의 Auth, User, Contest, Project 기능 구현 (제일 복잡한 일이다.)
````

- users: roles 및 projects를 가지고 있다.


### 2\. 특정 모듈만 빌드하여 개발 시간 단축

개발 중에는 전체 프로젝트 대신 작업 중인 모듈만 빌드하여 생산성을 높일 수 있습니다.

```bash
# designsystem 모듈만 빌드하기
./gradlew :core:designsystem:build

# designsystem 모듈 클린 후 빌드하기
./gradlew :core:designsystem:clean :core:designsystem:build
```

-----