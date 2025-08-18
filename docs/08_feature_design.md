# Project 기능 아키텍처 설계: DataSource 패턴 적용

## 1. 목표

이 문서는 '프로젝트 정보' 기능을 구현하는 데 사용되는 아키텍처를 설명합니다. 우리는 이 설계를 통해 깨끗하고, 테스트하기 쉬우며, 유지보수가 용이한 코드를 작성하는 것을 목표로 합니다.

## 2. 우리가 해결하려는 문제

프로젝트 데이터는 두 곳에 존재합니다.

1.  **원격(Remote):** Firebase Firestore 데이터베이스 (실제 데이터)
2.  **로컬(Local):** 앱 내부에 저장된 `fake_projects.json` 파일 (테스트 및 오프라인용 임시 데이터)

이때, 다음과 같은 요구사항을 만족하는 안정적인 데이터 로딩 기능이 필요합니다.

> "우선 Firebase에서 데이터를 가져오려고 시도하고, 만약 데이터가 없거나 네트워크 오류가 발생하면 로컬의 JSON 파일에서 데이터를 읽어와서 화면을 보여주자."

이 로직을 어떻게 효율적으로 구현할 수 있을까요? 모든 코드를 `ViewModel`이나 `Repository` 한 곳에 모아두면 코드가 복잡해지고, 특히 안드로이드 `Context` 의존성 때문에 테스트가 매우 어려워집니다.

## 3. 해결책: DataSource 패턴 아키텍처

이 문제를 해결하기 위해, 각 컴포넌트(클래스)가 하나의 책임만 갖도록 역할을 명확히 나누는 **DataSource 패턴**을 적용합니다. 이것을 '관심사 분리(Separation of Concerns)'라고 부릅니다.

### 전체 아키텍처 흐름

데이터는 아래와 같은 흐름으로 UI까지 전달됩니다.

```
[ UI (Screen) ] <--> [ ViewModel ] <--> [ Repository ] <--> [ DataSources ]
                                                          /       \
                                              [ Remote ]   [ Local ]
                                              (Firebase)   (JSON)
```

### 각 컴포넌트의 역할

#### 1. UI Layer (`ProjectListScreen.kt`)

*   **역할:** 화면을 그리는 역할만 담당합니다. 사용자로부터 클릭 같은 입력을 받으면 `ViewModel`에 알려줍니다.
*   **규칙:** 데이터를 어떻게 가져오는지, 어디서 오는지 전혀 알지 못합니다. 그저 `ViewModel`이 주는 상태(State)에 따라 화면을 갱신할 뿐입니다.

#### 2. ViewModel (`ProjectListViewModel.kt`)

*   **역할:** UI 상태를 관리하고, `Repository`로부터 데이터를 요청합니다.
*   **규칙:** UI가 필요로 하는 데이터를 `Repository`에 요청하고, 가공하여 UI 상태로 만듭니다. 데이터 로딩 규칙(예: Firebase 먼저, 후 JSON)은 전혀 알지 못합니다.

    ```kotlin
    // ProjectListViewModel.kt (예시)
    class ProjectListViewModel(private val projectRepository: ProjectRepository) : ViewModel() {
        fun fetchProjects(term: String) {
            // 그냥 Repository에 요청만 보낸다.
            val projects = projectRepository.getProjects(term)
            // ... UI 상태로 가공 ...
        }
    }
    ```

#### 3. Repository (`ProjectRepository.kt`)

*   **역할:** 데이터의 '총책임자'이자 '중개자'입니다. ViewModel에게 신뢰할 수 있는 데이터를 제공하는 유일한 통로(Single Source of Truth) 역할을 합니다.
*   **규칙:** **어떤 데이터를 어디서 가져올지 결정하는 비즈니스 로직**을 담당합니다. Firebase에서 가져올지, JSON에서 가져올지 결정하지만, '어떻게' 가져오는지는 모릅니다. 안드로이드 프레임워크(`Context`)에 대한 의존성이 전혀 없습니다.

    ```kotlin
    // ProjectRepository.kt (예시)
    class ProjectRepository(
        private val remoteDataSource: ProjectRemoteDataSource,
        private val localDataSource: ProjectLocalDataSource
    ) {
        suspend fun getProjects(term: String): List<Project> {
            return try {
                // 1. Remote에 먼저 물어본다.
                val remoteProjects = remoteDataSource.getProjects(term)
                if (remoteProjects.isNotEmpty()) {
                    remoteProjects
                } else {
                    // 2. Remote에 없으면 Local에 물어본다.
                    localDataSource.getFakeProjects(term)
                }
            } catch (e: Exception) {
                // 3. Remote 요청 중 에러나면 Local에 물어본다.
                localDataSource.getFakeProjects(term)
            }
        }
    }
    ```

#### 4. DataSources (핵심!)

*   **역할:** '어떻게' 데이터를 가져오는지에 대한 구체적인 방법을 알고 있는 전문가들입니다.

*   **`ProjectRemoteDataSource.kt`**
    *   Firebase Firestore와 통신하는 방법만 압니다.
    *   Firebase SDK를 사용하여 데이터를 요청하고 결과를 반환합니다.

*   **`ProjectLocalDataSource.kt`**
    *   안드로이드 앱 내부 리소스에서 JSON 파일을 읽는 방법만 압니다.
    *   `Context` 객체를 사용하여 `res/raw/fake_projects.json` 파일을 읽고 파싱하여 결과를 반환합니다. **`Context` 의존성은 이 클래스에만 존재합니다.**

    ```kotlin
    // ProjectLocalDataSource.kt (예시)
    class ProjectLocalDataSource(private val context: Context) {
        fun getFakeProjects(term: String): List<Project> {
            // Context를 사용해 JSON 파일을 읽고 파싱한다.
            val jsonString = context.resources.openRawResource(...).readText()
            return Json.decodeFromString<List<Project>>(jsonString).filter { ... }
        }
    }
    ```

## 4. 설계의 이점

이렇게 역할을 나누면 다음과 같은 강력한 장점을 얻게 됩니다.

1.  **관심사 분리:** 각 클래스는 하나의 책임만 가지므로 코드를 이해하기 쉽고, 변경이 필요할 때 딱 맞는 파일을 찾아 수정할 수 있습니다.

2.  **테스트 용이성:**
    *   `Repository`를 테스트할 때, 실제 Firebase나 `Context` 없이 가짜(Mock) `DataSource`를 주입하여 순수한 로직만 테스트할 수 있습니다.
    *   `ViewModel`도 가짜 `Repository`를 주입하여 쉽게 테스트할 수 있습니다.

3.  **유지보수 및 확장성:**
    *   나중에 로컬 데이터를 JSON이 아닌 Room DB로 바꾸고 싶다면? `ProjectLocalDataSource`의 내부 코드만 수정하면 됩니다. `Repository`나 `ViewModel`은 건드릴 필요가 없습니다.
    *   새로운 데이터 출처(예: 다른 서버 API)가 생겨도, 새로운 `DataSource`를 만들어 `Repository`에 간단히 추가할 수 있습니다.

이 설계는 당장의 기능 구현을 넘어, 앞으로 프로젝트가 더 커지고 복잡해지더라도 흔들리지 않는 안정적인 구조를 제공할 것입니다.

---

## 5. Feature 모듈 기능 명세

`feature` 모듈은 사용자가 직접 상호작용하는 화면과 비즈니스 로직을 담당합니다. 각 기능은 다음과 같이 구성됩니다.

### Auth (인증)

사용자 로그인 및 회원가입과 관련된 기능을 제공합니다.

*   **제공 화면:**
    *   `LoginScreen`: 이메일, 비밀번호로 로그인을 수행합니다.
    *   `RegisterScreen`: 이메일, 학번, 이름, 비밀번호를 입력받아 신규 사용자를 등록합니다.

### Contest (경진대회)

경진대회 정보 조회 기능을 제공합니다.

*   **제공 화면:**
    *   `ContestsScreen`: 현재 진행 중이거나 지난 경진대회 목록을 보여줍니다.

### Notice (공지사항)

공지사항 조회 기능을 제공합니다.

*   **제공 화면:**
    *   `NoticeScreen`: 전체 공지사항 목록을 보여줍니다.

### Project (프로젝트)

경진대회에 제출된 프로젝트의 조회 및 관리 기능을 제공합니다.

*   **제공 화면:**
    *   `ProjectListScreen`: 특정 경진대회에 제출된 프로젝트 목록을 보여줍니다.
    *   `ProjectDetailScreen`: 개별 프로젝트의 상세 정보를 보여줍니다.

### User (사용자)

사용자 정보 조회 및 관리 기능을 제공합니다.

*   **제공 화면:**
    *   `UsersScreen`: 전체 사용자 목록을 보여주거나, 특정 사용자의 프로필 정보를 보여줍니다.