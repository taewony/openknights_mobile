# 07_navigation_design.md

## OpenKnightsApp 내비게이션 설계 (현재 구현 기준)

이 문서는 OpenKnights 모바일 애플리케이션에 현재 구현된 내비게이션 로직을 설명합니다. 이 앱은 Jetpack Compose의 내비게이션 라이브러리 대신 사용자 정의 백 스택 관리 시스템을 사용하여 화면 간 이동을 처리합니다. 이 문서는 학생들이 코드를 이해하는 데 도움을 주기 위해 작성되었습니다.

### 1. 종속성

현재 OpenKnightsApp의 주요 내비게이션 흐름에는 Jetpack Compose Navigation과 같은 전용 내비게이션 라이브러리가 사용되지 않습니다. 대신, Compose의 상태 관리 기능을 활용한 사용자 정의 백 스택 구현이 사용됩니다.

`app/build.gradle.kts` 파일에는 다음과 같은 내비게이션 관련 종속성이 포함되어 있습니다. 이들은 과거의 흔적이거나 다른 목적으로 사용될 수 있습니다.

```kotlin
dependencies {
    // ... (기존 종속성)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // ...
}
```

### 2. 핵심 내비게이션 구성 요소

앱의 내비게이션은 주로 다음 구성 요소를 중심으로 작동합니다.

*   **`ScreenEntry` Sealed Interface:**
    `OpenKnightsApp.kt` 파일 내에 정의된 `sealed interface ScreenEntry`는 앱의 각 내비게이션 대상을 나타냅니다. 예를 들어, `ContestListScreenEntry`, `UserScreenEntry`, `ProjectListScreenEntry` 등이 있습니다. 각 `ScreenEntry` 객체는 특정 화면을 식별하는 데 사용됩니다.

*   **`backStack` (사용자 정의 백 스택):**
    `val backStack = remember { mutableStateListOf<ScreenEntry>(ContestListScreenEntry) }`와 같이 `mutableStateListOf`를 사용하여 구현된 사용자 정의 백 스택입니다. 이 리스트는 사용자가 방문한 화면의 순서를 기록하며, 리스트의 마지막 요소가 현재 활성화된 화면을 나타냅니다.

*   **`currentEntry`:**
    `val currentEntry = backStack.lastOrNull()`은 `backStack`의 가장 마지막 요소를 참조하여 현재 사용자에게 표시되는 화면이 무엇인지 식별합니다.

### 3. 내비게이션 로직

앱의 화면 전환은 `Scaffold`의 `content` 블록 내에서 `backStack`의 상태를 기반으로 조건부 렌더링을 통해 이루어집니다.

*   **화면 전환 (`when` 문):**
    `Scaffold`의 `content` 영역에서는 `when (val entry = backStack.lastOrNull())` 문을 사용하여 `currentEntry`의 타입에 따라 해당하는 Composable 화면을 렌더링합니다. 예를 들어, `ContestListScreenEntry`이면 `ContestListScreen`을, `UserScreenEntry`이면 `UserScreen`을 표시합니다.

*   **스택에 푸시 (앞으로 이동):**
    새로운 화면으로 이동할 때는 `backStack.add(새로운ScreenEntry)`와 같이 `backStack` 리스트에 새로운 `ScreenEntry` 객체를 추가합니다. 이는 새로운 화면을 백 스택의 맨 위에 추가하는 효과를 줍니다.

*   **스택에서 팝 (뒤로 이동):**
    뒤로 가기 동작(예: 상단 앱 바의 뒤로 가기 버튼 클릭)은 `backStack.removeLastOrNull()`을 호출하여 `backStack`의 가장 마지막 요소를 제거합니다. 이는 현재 화면을 스택에서 제거하고 이전 화면으로 돌아가는 효과를 줍니다.

*   **스택 지우기 (초기화):**
    하단 내비게이션 바 항목을 클릭하는 경우와 같이 특정 상황에서는 `backStack.clear()`를 호출하여 백 스택 전체를 비우고, 이후 `backStack.add(초기ScreenEntry)`를 통해 원하는 초기 화면으로 이동합니다. 이는 내비게이션 상태를 완전히 재설정하는 역할을 합니다.

*   **초기 화면:**
    앱이 시작될 때 `backStack`은 `ContestListScreenEntry`로 초기화되어, 앱 진입 시 Contest 목록 화면이 기본적으로 표시됩니다.

### 4. 하단 내비게이션 바 통합

`Scaffold`의 `bottomBar`에 있는 `NavigationBar`는 각 `NavigationBarItem`의 `onClick` 람다 내에서 사용자 정의 백 스택 로직을 직접 호출합니다.

*   `NavigationBarItem` 클릭 시:
    - `backStack.clear()`를 호출하여 현재 백 스택을 비웁니다.
    - `backStack.add(해당ScreenEntry)`를 호출하여 선택된 화면의 `ScreenEntry`를 백 스택에 추가합니다. 이는 탭 전환 시 이전 화면의 상태를 유지하지 않고 항상 새로운 탭의 초기 상태로 이동하는 효과를 줍니다.
    - `selected` 상태는 `currentEntry`가 해당 `ScreenEntry` 타입인지 여부로 결정됩니다.

### 5. 상단 앱 바 내비게이션

상단 `TopAppBar`에는 다음과 같은 내비게이션 요소가 포함됩니다.

*   **뒤로 가기 버튼:**
    `backStack.size > 1`일 때 표시되며, `IconButton` 클릭 시 `backStack.removeLastOrNull()`을 호출하여 이전 화면으로 돌아갑니다.

*   **드롭다운 메뉴:**
    메뉴 아이콘 클릭 시 드롭다운 메뉴가 표시되며, 각 메뉴 항목은 `backStack.add()`를 통해 특정 `ScreenEntry`로 이동합니다. 예를 들어, "사용자 등록" 클릭 시 `RegisterScreenEntry`를 추가합니다.

### 6. 제한 사항

이러한 사용자 정의 백 스택 관리 방식은 간단한 내비게이션 요구 사항에는 적합할 수 있지만, Jetpack Compose Navigation과 같은 전문 라이브러리에 비해 다음과 같은 제한 사항이 있습니다.

*   **딥 링크 부재:** 외부 소스에서 앱 내 특정 화면으로 직접 이동하는 딥 링크를 기본적으로 지원하지 않습니다.
*   **수동적인 인수 전달:** 화면 간에 데이터를 전달하는 로직을 수동으로 구현해야 하며, 이는 복잡성을 증가시킬 수 있습니다.
*   **수명 주기 관리 부족:** 내비게이션 대상(화면)에 대한 세분화된 수명 주기 관리가 내장되어 있지 않아, 상태 보존 및 리소스 관리에 어려움이 있을 수 있습니다.
*   **확장성:** 앱의 규모가 커지거나 내비게이션 흐름이 복잡해질수록 유지보수 및 확장이 어려워질 수 있습니다.

이 문서는 현재 OpenKnightsApp의 내비게이션 구현 방식을 이해하는 데 도움이 될 것입니다. 향후 앱의 복잡성이 증가하면 Jetpack Compose Navigation과 같은 전문 라이브러리로의 전환을 고려할 수 있습니다.
