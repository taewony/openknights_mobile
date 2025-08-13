# **OpenKnights App UI Design**

이 문서는 OpenKnights 앱의 메인 화면 UI 구조와 구성 요소의 동작을 설명합니다. UI는 Jetpack Compose의 `Scaffold`를 사용하여 구성되어 있으며, **TopAppBar**, **BottomNavigationBar**, 그리고 화면 전환을 위한 **Main Content** 영역으로 나뉩니다.

## 0. UI 요구사항 (UI Requirements)
- **일관된 디자인 시스템**: `core-designsystem` 모듈의 `KnightsTheme`을 앱 전체에 적용하여 색상, 타이포그래피, 컴포넌트 스타일의 일관성을 유지합니다.
- **기본 화면 구조**: 앱의 진입점으로서 상단 앱 바(TopAppBar)와 하단 탐색 메뉴(Bottom Navigation)를 포함하는 기본 화면 구조를 가집니다.
- **메인 콘텐츠 표시**: 초기 화면에는 "Hello, OpenKnights!"와 같은 환영 메시지를 표시하여 앱이 정상적으로 실행됨을 확인합니다.
---


### 화면 구현 프로세스 예시: 'Contest' 기능

새로운 기능을 기획하고 화면에 구현하는 전체적인 프로세스를 'Contest(대회)' 기능 개발을 예시로 알아봅시다. 이 프로세스는 우리가 정의한 아키텍처를 어떻게 실제로 적용하는지 보여주는 좋은 예시가 될 것입니다.

### Phase 1: 데이터 계층 설정 (in `:core:data`)

가장 먼저 UI에 보여줄 데이터가 무엇인지 정의하고, 그 데이터를 가져올 방법을 준비합니다.

1.  **`ContestRepository` 인터페이스 정의:**
    *   `Contest` 데이터를 처리하는 함수의 명세를 설계합니다. 예를 들어 `getContests()` 라는 함수를 정의하여 "대회 목록을 가져온다"는 기능을 약속합니다.

2.  **`ContestRepositoryImpl` 구현체 생성:**
    *   `ContestRepository` 인터페이스의 실제 동작을 구현합니다.
    *   초기 개발 단계에서는 UI 팀이 실제 데이터 없이도 화면을 만들 수 있도록, **가짜(Fake) 데이터**를 반환하는 코드를 작성합니다.
    *   *추후 이 부분은 Firebase Firestore와 통신하여 실제 데이터를 가져오는 코드로 변경됩니다.*

### Phase 2: 기능/UI 계층 설정 (in `:feature`)

데이터가 준비되었으니, 이제 사용자가 볼 화면과 화면의 상태를 관리하는 로직을 만듭니다.

1.  **`contest` 패키지 생성:**
    *   `:feature` 모듈의 `com.openknights.feature` 패키지 내부에 `contest` 라는 새로운 패키지를 만들어 관련 파일들을 그룹화합니다.

2.  **`ContestViewModel` 생성:**
    *   화면의 상태(로딩 중, 성공, 실패 등)를 관리하고, 데이터 계층(`ContestRepository`)과 통신하는 `ViewModel`을 만듭니다.
    *   `loadContests()` 같은 함수를 통해 Repository에게 데이터를 요청하고, 그 결과를 `ContestUiState`에 담아 UI가 사용할 수 있도록 노출합니다.

3.  **`ContestScreen` 생성:**
    *   `ContestViewModel`이 제공하는 상태(`UiState`)를 보고 실제 화면을 그리는 Composable 함수입니다.
    *   사용자가 "새로고침" 버튼을 누르면 `ViewModel`의 `loadContests()` 함수를 호출하도록 연결합니다.

4.  **`ContestList` 및 `ContestCard` 생성:**
    *   데이터 로딩에 성공했을 때, 대회 목록 전체를 보여주는 `ContestList`와 목록의 각 항목 하나하나를 보여주는 `ContestCard`를 만들어 화면을 완성합니다.


## **1\. 전체 레이아웃 (Scaffold)**

앱의 기본 구조는 `Scaffold` 컴포저블을 사용하여 상단 바, 하단 바, 그리고 중앙 콘텐츠 영역으로 구성됩니다.

* **`topBar`**: 화면 상단에 위치하며 앱의 제목과 주요 액션 버튼들을 포함합니다.  
* **`bottomBar`**: 화면 하단에 위치하며 주요 화면으로 빠르게 이동할 수 있는 내비게이션 기능을 제공합니다.  
* **`content`**: `topBar`와 `bottomBar` 사이에 위치하며, 사용자의 상호작용에 따라 다양한 화면(Screen)이 표시됩니다.

## **2\. 상단 앱 바 (Top App Bar)**

`TopAppBar`는 앱의 브랜딩과 상황에 맞는 액션을 제공합니다.

### **2.1. 구성 요소**

| 요소 | 아이콘/텍스트 | 위치 | 설명 |
| ----- | ----- | ----- | ----- |
| **Navigation Icon** | `Menu` 또는 `ArrowBack` | 왼쪽 | \- **뒤로가기**: 내비게이션 스택에 2개 이상의 화면이 쌓여 있을 때 `ArrowBack` 아이콘이 표시되며, 클릭 시 이전 화면으로 돌아갑니다.\<br\>- **메뉴**: 첫 화면일 경우 `Menu` 아이콘이 표시되며, 클릭 시 드롭다운 메뉴가 나타납니다. |
| **Title** | "OpenKnights" | 중앙 | 앱의 타이틀이 중앙 정렬로 표시됩니다. |
| **Action Icon** | `Notifications` | 오른쪽 | \- **알림**: `Notifications` 아이콘이 표시되며, 클릭 시 알림 화면(`NoticeScreen`)으로 이동합니다.\<br\>- **로그인 상태 반영**: 로그인 상태(`isLoggedIn`)에 따라 아이콘의 모양(채워짐/외곽선)과 색상이 변경되어 사용자가 로그인 여부를 시각적으로 인지할 수 있습니다. |

### **2.2. 드롭다운 메뉴 (Dropdown Menu)**

`Menu` 아이콘 클릭 시 나타나는 메뉴 항목들은 다음과 같습니다.

* **사용자 등록**: `RegisterScreen`으로 이동합니다.  
* **로그인/로그아웃**:  
  * **로그인 상태**: "로그아웃" 메뉴가 표시되며, 클릭 시 로그아웃 처리 후 `LoginScreen`으로 이동합니다.  
  * **로그아웃 상태**: "로그인" 메뉴가 표시되며, 클릭 시 `LoginScreen`으로 이동합니다.  
* **프로젝트 등록**: 프로젝트 등록 화면으로 이동하는 기능을 수행합니다. (현재는 TODO로 명시됨)

## **3\. 하단 내비게이션 바 (Bottom Navigation Bar)**

`NavigationBar`는 앱의 핵심 기능을 대표하는 3개의 탭으로 구성됩니다.

| 아이콘 | 텍스트 | 설명 |
| ----- | ----- | ----- |
| `Home` | **HOME** | 앱의 메인 화면인 `ContestListScreen`으로 이동합니다. |
| `List` | **프로젝트** | `ProjectListScreen`으로 이동하여 프로젝트 목록을 보여줍니다. |
| `Person` | **사용자** | `UserScreen`으로 이동하여 사용자 관련 정보를 보여줍니다. |

*   
  현재 활성화된 화면에 해당하는 탭이 **`selected`** 상태로 표시되어 사용자에게 현재 위치를 알려줍니다.  
* 각 탭을 클릭하면 내비게이션 스택이 초기화되고 해당 화면으로 바로 이동합니다.

## **4\. 메인 콘텐츠 및 화면 전환 (NavDisplay)**

`Scaffold`의 중앙 영역으로, `NavDisplay` 컴포저블을 통해 내비게이션 스택(`backStack`)에 따라 동적으로 화면이 교체됩니다.

### **4.1. 주요 화면 (Screen)**

* `ContestListScreen`: 경진대회 목록을 보여주는 홈 화면.  
* `ProjectListScreen`: 특정 경진대회의 프로젝트 목록을 보여주는 화면.  
* `ProjectDetailScreen`: 개별 프로젝트의 상세 정보를 보여주는 화면.  
* `UserScreen`: 사용자 정보를 보여주는 화면.  
* `NoticeScreen`: 알림 목록을 보여주는 화면.  
* `LoginScreen`: 사용자 로그인 화면.  
* `RegisterScreen`: 사용자 등록 화면.

### **4.2. 내비게이션 흐름**

* 사용자가 UI 요소(버튼, 리스트 아이템 등)와 상호작용하면 `backStack`에 새로운 화면(`ScreenEntry`)이 추가되거나 제거됩니다.  
* `NavDisplay`는 `backStack`의 마지막 항목을 감지하여 해당 화면을 렌더링하고, 자연스러운 화면 전환 애니메이션을 제공합니다.



## **5\. 공통 UI 컴포넌트**

앱 전반에서 재사용되는 UI 컴포넌트입니다.

### **5.1. `KnightsCard`**

`KnightsCard`는 정보를 감싸는 컨테이너 역할을 하는 카드 컴포넌트입니다.

* **기능**:
  * `onClick` 핸들러를 제공하여 클릭 이벤트를 처리할 수 있습니다.
  * `shape`, `color` 등 Material Design의 `Card` 컴포넌트 속성을 커스터마이징할 수 있습니다.
* **사용 예시**:
  * 경진대회 목록의 각 항목
  * 프로젝트 목록의 각 항목

### **5.2. `KnightsTopAppBar`**

`KnightsTopAppBar`는 앱의 상단 바를 구성하는 컴포넌트입니다.

* **기능**:
  * `title`을 중앙에 표시합니다.
  * `navigationType` (Back, Close, None)에 따라 다른 내비게이션 아이콘을 표시할 수 있습니다. (현재는 아이콘이 구현되어 있지 않음)
  * `actions`를 통해 오른쪽에 여러 개의 아이콘 버튼을 추가할 수 있습니다.

### **5.3. `NetworkImage`**

`NetworkImage`는 네트워크 URL로부터 이미지를 비동기적으로 로드하여 표시하는 컴포넌트입니다.

* **기능**:
  * `Coil` 라이브러리를 사용하여 이미지를 로드합니다.
  * `contentScale`을 통해 이미지의 스케일링 방식을 조절할 수 있습니다.

### **5.4. `OutlineChip`**

`OutlineChip`은 외곽선만 있는 칩(Chip) 컴포넌트입니다.

* **기능**:
  * `borderColor`와 `textColor`를 커스터마이징하여 다양한 상태를 표현할 수 있습니다.
* **사용 예시**:
  * 태그
  * 필터

### **5.5. `TextChip`**

`TextChip`은 배경색이 있는 칩(Chip) 컴포넌트입니다.

* **기능**:
  * `containerColor`와 `textColor`를 커스터마이징할 수 있습니다.
* **사용 예시**:
  * 상태 표시 (예: "모집중", "마감")
  * 카테고리 분류