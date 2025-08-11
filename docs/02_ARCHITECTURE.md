# OpenKnights 앱 아키텍처 가이드

안녕하세요, OpenKnights 프로젝트에 오신 것을 환영합니다! 🚀

이 문서는 우리 앱이 어떤 구조로 만들어져 있는지 설명하는 안내서입니다. 이 문서를 통해 여러분은 코드를 어디에 작성해야 할지, 그리고 각 부분이 어떻게 상호작용하는지 쉽게 이해할 수 있을 것입니다.

우리 앱은 **클린 아키텍처(Clean Architecture) 원칙에 기반한 계층형 모듈 구조(Layered Modular Architecture)**를 따릅니다. 이름이 조금 복잡해 보이지만, 목표는 간단합니다.

> **"각 부분이 자기 역할에만 집중하게 만들어서, 앱을 쉽게 개발하고, 테스트하고, 오랫동안 유지보수할 수 있게 만들자!"**

---

## 🎯 핵심 원칙

우리 아키텍처를 지배하는 가장 중요한 규칙은 **의존성 규칙(The Dependency Rule)**입니다.

```
[ App ] -> [ Feature ] -> [ Data ] -> [ Model ]
```

*   화살표(`->`)는 "의존한다" 또는 "~를 알고 있다"는 의미입니다.
*   **바깥쪽 계층은 안쪽 계층을 알 수 있지만, 안쪽 계층은 절대 바깥쪽 계층을 알면 안 됩니다.**
*   예를 들어, `Data` 모듈은 `Model`이 무엇인지 알지만, `Feature`나 `App` 모듈에 대해서는 전혀 알지 못합니다. `Model`은 그 누구도 알지 못하는 가장 순수한 상태입니다.

이 규칙 덕분에 `Model`이나 `Data` 같은 핵심 로직은 UI가 어떻게 바뀌든 전혀 영향을 받지 않고 안정적으로 유지될 수 있습니다.

---

## 🧩 모듈 구성

우리 프로젝트는 여러 개의 모듈로 나뉘어 있습니다. 각 모듈은 특정한 역할을 수행하는 부품과 같습니다.

### 🏛️ `:core` 그룹

`core`는 이름 그대로 앱의 핵심 기능을 담당하는 모듈들의 집합입니다. 특정 화면이나 기능에 종속되지 않고 프로젝트 전반에서 사용되는 공통 기반입니다.

#### 💎 `:core:model`
*   **역할:** 앱에서 사용하는 모든 데이터의 모양(데이터 클래스)을 정의합니다.
*   **주요 책임:**
    *   `User`, `Contest`, `Project` 등 앱의 데이터 명세를 정의합니다.
    *   순수한 데이터 객체(DTOs)만 포함하며, 어떠한 로직도 가지지 않습니다.
*   **의존성:** **없음 (No Dependencies)**
*   **패키지 이름:** `com.openknights.model`
*   **학생들을 위한 핵심 개념:** 앱에서 사용하는 모든 '명사'들을 정의하는 사전과 같습니다. 가장 독립적이고 순수한 부분입니다.

#### 📚 `:core:data`
*   **역할:** 데이터의 출처(네트워크 API, 로컬 DB 등)와 통신하여 데이터를 가져오거나 저장합니다.
*   **주요 책임:**
    *   Repository 패턴을 사용하여 데이터 소스를 관리합니다.
    *   서버 API를 호출하고, 받은 JSON 데이터를 `:model`의 객체로 변환합니다.
*   **의존성:** `:core:model`
*   **패키지 이름:** `com.openknights.data`
*   **학생들을 위한 핵심 개념:** 도서관의 사서와 같습니다. `ViewModel`이 "이런 책(데이터) 좀 찾아줘"라고 요청하면, 서고(서버, DB)에 가서 책(`Model`)을 찾아서 돌려줍니다.

#### 🎨 `:core:designsystem`
*   **역할:** 앱 전체에서 공통으로 사용되는 UI 부품(컴포넌트)들을 모아놓습니다.
*   **주요 책임:**
    *   공통 버튼, 텍스트 필드, 색상, 폰트, 테마 등을 정의합니다.
    *   앱의 전체적인 디자인 통일성을 유지합니다.
*   **의존성:** **없음 (No Dependencies)**
*   **패키지 이름:** `com.openknights.designsystem`
*   **학생들을 위한 핵심 개념:** 미리 만들어진 레고(LEGO) 블록 상자입니다. 어떤 화면을 만들든 이 상자에서 필요한 블록을 가져와 조립하면 됩니다.

### ✨ `:feature` 모듈
*   **역할:** 사용자가 실제로 보고 상호작용하는 화면의 UI와 상태를 관리합니다.
*   **주요 책임:**
    *   Jetpack Compose를 사용해 각 화면(Screen)을 그립니다.
    *   `ViewModel`을 통해 비즈니스 로직을 처리하고 화면의 상태(State)를 관리합니다.
    *   사용자의 입력을 받아 `ViewModel`에 전달합니다.
*   **의존성:** `:core:data`, `:core:model`, `:core:designsystem`
*   **패키지 이름:** `com.openknights.feature` (내부적으로 `user`, `contest` 등 기능별 패키지로 나뉨)
*   **학생들을 위한 핵심 개념:** 앱의 '얼굴'과 '두뇌'입니다. 사용자와 직접 소통하며, 사서(`:data`)에게 데이터를 요청하고, 레고 블록(`:designsystem`)으로 화면을 조립하여 보여줍니다. 대부분의 화면 개발은 이곳에서 이루어집니다.

### 🚀 `:app` 모듈
*   **역할:** 모든 모듈을 하나로 조립하여 최종 안드로이드 앱 패키지(.apk)를 만듭니다.
*   **주요 책임:**
    *   메인 `Activity`를 가집니다.
    *   Navigation의 시작점(NavHost)을 설정합니다.
    *   필요한 경우 Application 클래스를 통해 앱 전역 초기화를 수행합니다.
*   **의존성:** `:feature`, `:core:designsystem`
*   **패키지 이름:** `com.openknights.app`
*   **학생들을 위한 핵심 개념:** 모든 부품을 모아 완성된 제품을 만드는 최종 조립 라인입니다. 이 모듈 자체에는 복잡한 로직이 거의 없습니다.

---

## 🌊 데이터 흐름의 예시

사용자가 '새로고침' 버튼을 누를 때 데이터가 어떻게 흘러가는지 알아봅시다.

1.  **`User` -> `Screen` (`:feature`)**
    *   사용자가 화면의 새로고침 버튼을 누릅니다.

2.  **`Screen` -> `ViewModel` (`:feature`)**
    *   `Button`의 `onClick` 람다가 실행되어 `UserViewModel`의 `refreshUsers()` 함수를 호출합니다.

3.  **`ViewModel` -> `Repository` (`:core:data`)**
    *   `UserViewModel`은 `UserRepository`의 `getUsers()` 함수를 호출하여 사용자 목록을 요청합니다.

4.  **`Repository` -> `API` / `DB` (`:core:data`)**
    *   `UserRepository`는 Retrofit 같은 라이브러리를 사용해 서버 API를 호출합니다.

5.  **`API` -> `Model` (`:core:data` -> `:core:model`)**
    *   서버에서 받은 JSON 응답을 `kotlinx.serialization` 등을 통해 `List<User>` 객체(`:model`)로 변환합니다.

6.  **`Repository` -> `ViewModel` (`:core:data` -> `:feature`)**
    *   `UserRepository`는 변환된 `List<User>`를 `UserViewModel`에 반환합니다.

7.  **`ViewModel` -> `State` -> `Screen` (`:feature`)**
    *   `UserViewModel`은 전달받은 데이터로 화면의 상태(`UiState`)를 업데이트합니다.
    *   Compose `Screen`은 `State`의 변화를 감지하고, 새로운 데이터로 화면을 다시 그립니다(Recomposition).

이처럼 데이터는 단방향으로 흐르며, 각 모듈은 자신의 책임만 다합니다. 이 구조를 잘 이해하고 따르면 누구나 깨끗하고 안정적인 코드를 작성할 수 있습니다.
