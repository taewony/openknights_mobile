# UserCard Composable Design (개선안)

## 1. 개요 (Overview)

`UserCard`는 사용자 프로필의 핵심 정보를 보여주는 UI 컴포넌트입니다. **사용자 본인의 카드일 경우, 카드 전체를 클릭하여 편집 화면으로 이동할 수 있는 더 간결한 인터랙션을 제공합니다.**

## 2. 데이터 모델 (Data Model)

`UserCard`는 UI에 필요한 상태만 담고 있는 `UserUiState` 객체를 파라미터로 받습니다. 이 객체는 데이터 모델(`User`)을 ViewModel에서 UI 상태에 맞게 변환(mapping)한 결과물입니다.

```kotlin
data class UserUiState(
    val uid: String,
    val name: String,
    val introduction: String,
    val profileImageUrl: String?,
    val isCurrentUser: Boolean,
    val localProfileImageResId: Int? = null // 로컬 드로어블 리소스 ID (테스트용 또는 임시용)
)
```

## 3. UI 레이아웃 (UI Layout)

개별적인 수정 버튼을 제거하여 UI를 더 깔끔하게 개선합니다.

```
+--------------------------------------------------+
|                                                  |
|  (Avatar Image/Icon)  [User Name]                |
|                       [Introduction]             |
|                                                  |
+--------------------------------------------------+
```

- **전체 카드**: `isCurrentUser`가 `true`일 경우에만 카드 전체에 `clickable` 속성이 적용됩니다. 클릭 시 프로필 편집 화면으로 이동합니다.

## 4. 컴포넌트 상세 설계 (Component Design)

### 4.1. 아바타 이미지 (Avatar Image)

- `Coil`의 `AsyncImage`를 사용하여 `profileImageUrl`을 비동기적으로 로드합니다.
- `placeholder`로 **Material Design의 기본 사용자 아이콘(`Icons.Default.Person`)**을 표시합니다.

### 4.2. 사용자 정보 (User Info)

- `name`과 `introduction`을 `Text` 컴포저블로 표시합니다.
- **별도의 수정 버튼은 표시하지 않습니다.**

## 5. Composable 함수 시그니처

`on...Click` 콜백들을 하나의 `onCardClick`으로 통합하고, `clickable` Modifier는 `uiState.isCurrentUser` 값에 따라 조건부로 적용합니다.

**구현 파일 위치 추천**: `core/ui/src/main/java/com/openknights/core/ui/UserCard.kt`

```kotlin
@Composable
fun UserCard(
    uiState: UserUiState, // UserProfileUiState -> UserUiState로 변경
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (uiState.isCurrentUser) {
        modifier.clickable { onCardClick() }
    } else {
        modifier
    }

    Row(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아바타 이미지
        // 표시 우선순위: profileImageUrl (원격) -> localProfileImageResId (로컬) -> Icons.Default.Person (기본 아이콘)
        val imageModifier = Modifier.size(80.dp).clip(CircleShape)

        if (uiState.profileImageUrl != null) {
            AsyncImage(
                model = uiState.profileImageUrl,
                contentDescription = "User Profile Image",
                modifier = imageModifier,
                placeholder = painterResource(id = R.drawable.default_avatar), // 로딩 중 기본 이미지
                error = painterResource(id = R.drawable.default_avatar), // 에러 시 기본 이미지
                contentScale = ContentScale.Crop
            )
        } else if (uiState.localProfileImageResId != null) {
            Image(
                painter = painterResource(id = uiState.localProfileImageResId),
                contentDescription = "User Profile Image",
                modifier = imageModifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default User Icon",
                modifier = imageModifier,
                tint = MaterialTheme.colorScheme.onSurfaceVariant // 아이콘 색상 조정
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 사용자 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(text = uiState.name, style = MaterialTheme.typography.h6)
            Text(text = uiState.introduction, style = MaterialTheme.typography.body1)
        }
    }
}
```

## 6. 예제 사용법 (Example Usage)

```kotlin
@Preview
@Composable
fun UserCardPreview() {
    val sampleUiState = UserProfileUiState(
        name = "홍길동",
        introduction = "번개를 다루는 의적",
        profileImageUrl = null,
        isCurrentUser = true // 본인의 프로필인 경우
    )

    UserCard(
        uiState = sampleUiState,
        onCardClick = { /* 프로필 편집 화면으로 이동 */ }
    )
}
```