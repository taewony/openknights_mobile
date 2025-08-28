# DroidKnights 앱 디자인 시스템 분석

이 문서는 `DroidKnightsApp`의 `core:designsystem` 모듈을 분석하여 앱의 디자인 언어, 토큰, 그리고 전반적인 디자인 가이드라인을 설명합니다.

## 1. 디자인 철학 및 개요

DroidKnights 디자인 시스템은 Google의 Material 3를 기반으로 구축되었으며, 앱의 정체성을 나타내는 커스텀 색상, 타이포그래피, 컴포넌트를 추가하여 확장한 형태입니다. 시스템은 라이트 모드와 다크 모드를 모두 지원하며, `KnightsTheme`라는 통합된 Composable을 통해 앱 전체에 일관된 스타일을 적용합니다.

- **모듈성**: 색상, 타이포그래피, 모양(Shape)이 별도의 파일로 명확하게 분리되어 관리됩니다.
- **재사용성**: 커스텀 컴포넌트들은 정의된 디자인 토큰(색상, 글꼴 등)을 사용하여 일관성을 유지합니다.
- **테마 확장성**: `KnightsTheme`를 통해 MaterialTheme을 감싸고, 커스텀 타이포그래피(`KnightsTypography`)와 모양(`KnightsShape`)을 `CompositionLocalProvider`로 제공하여 시스템을 확장합니다.

## 2. 디자인 토큰 (Design Tokens)

디자인 토큰은 UI를 구성하는 가장 기본적인 시각적 요소의 값입니다. (예: 특정 색상 코드, 글꼴 크기)

### 2.1. 색상 (Color System)

`KnightsColor.kt` 파일은 앱에서 사용되는 모든 커스텀 색상을 정의하는 중앙 팔레트 역할을 합니다. 이 색상들은 `Theme.kt`에서 라이트/다크 모드에 따라 Material 3의 시맨틱 역할(primary, secondary, surface 등)에 매핑됩니다.

#### 주요 색상 팔레트 (`KnightsColor`)

- **Grays**: `White`, `LightGray`, `Gray`, `DarkGray`, `Black` 등 다양한 명도의 회색 계열
- **Blues**: `Blue01` (#215BF6), `Blue02` (#5180FF) 등
- **Greens**: `Neon01` (#49F300) - **라이트 모드의 Primary 색상**으로 사용되어 앱의 시그니처 컬러 역할을 합니다.
- **Yellows**: `Yellow01` (#F2E800) 등
- **Reds**: `Red01` (#F9DEDC) 등
- **Special**: `Purple01` (#B469FF), `Cosmos` (#151515)

#### 테마별 색상 적용

- **Light Theme**:
  - `primary`: `Neon01` (밝은 녹색)
  - `surface`: `White`
  - `onSurface`: `Black`
- **Dark Theme**:
  - `primary`: `White`
  - `surface`: `Graphite` (#292929)
  - `onSurface`: `White`

### 2.2. 타이포그래피 (Typography System)

`Type.kt` 파일은 `KnightsTypography` 데이터 클래스를 통해 매우 상세하고 표현력이 풍부한 타이포그래피 시스템을 정의합니다. 모든 스타일은 `FontFamily.SansSerif`를 기반으로 합니다.

#### 명명 규칙

스타일 이름은 `[스타일]-[크기]-[가중치]` 조합으로 명명됩니다. (예: `headlineLargeEB`)

- **스타일**: `display`, `headline`, `title`, `label`, `body`
- **크기**: `Large`, `Medium`, `Small`
- **가중치**: `EB` (ExtraBold), `BL` (Black), `B` (Bold), `SB` (SemiBold), `M` (Medium), `R` (Regular)

#### 주요 스타일 예시

| 이름 | 크기/줄높이 | 가중치 |
| --- | --- | --- |
| `displayLargeR` | 57.sp / 64.sp | Regular |
| `headlineLargeEB` | 32.sp / 40.sp | ExtraBold |
| `headlineSmallBL` | 24.sp / 32.sp | Black |
| `titleLargeB` | 22.sp / 28.sp | Bold |
| `titleMediumB` | 16.sp / 24.sp | Bold |
| `bodyLargeR` | 16.sp / 24.sp | Regular |
| `labelSmallM` | 11.sp / 16.sp | Medium |

### 2.3. 모양 (Shape System)

`KnightsShape.kt`에 정의된 모양 시스템은 비교적 단순하며, 주로 컴포넌트의 모서리 둥글기를 제어합니다.

- `chip`: `RoundedCornerShape(10.dp)`
- `rounded12`: `RoundedCornerShape(12.dp)`

`KnightsCard`와 같은 일부 컴포넌트는 `12.dp` 또는 `32.dp` 등 특정 값을 직접 사용하기도 합니다.

## 3. 컴포넌트 라이브러리 (Component Library)

`component` 디렉토리에는 앱의 UI를 구성하는 재사용 가능한 커스텀 Composable 함수들이 포함되어 있습니다. 이 컴포넌트들은 위에서 정의된 디자인 토큰을 사용하여 만들어졌습니다.

- **`KnightsCard`**: 그림자, 모서리 둥글기 등을 커스텀할 수 있는 기본 카드 컴포넌트.
- **`KnightsTopAppBar`**: 뒤로가기/닫기 등 정해진 유형을 갖는 커스텀 상단 앱 바.
- **Chip 계열**:
  - `TextChip`: 간단한 텍스트를 표시하는 기본 칩.
  - `IconTextChip`: 아이콘과 텍스트를 함께 표시하는 칩.
  - `OutlineChip`: 투명한 배경과 테두리를 가진 칩.
- **`NetworkImage`**: 이미지 로딩 라이브러리인 Coil을 감싸서 URL로부터 이미지를 쉽게 표시하는 Composable.
- **`BottomLogo`**: "Droid Knights 2023" 텍스트를 표시하는 하단 로고.

## 4. 테마 적용 (`KnightsTheme`)

`Theme.kt`의 `KnightsTheme` Composable은 이 모든 요소를 하나로 묶는 핵심적인 역할을 합니다.

1.  시스템의 다크 모드 설정을 감지하여 `DarkColorScheme` 또는 `LightColorScheme`을 선택합니다.
2.  `MaterialTheme`에 선택된 색상 스킴을 적용합니다.
3.  `CompositionLocalProvider`를 사용하여 `LocalTypography`와 `LocalShape`에 커스텀 타이포그래피와 모양 시스템을 제공합니다.
4.  앱의 모든 하위 Composable은 `KnightsTheme.typography` 또는 `KnightsTheme.shape`를 통해 중앙에서 관리되는 타이포그래피와 모양 스타일에 접근할 수 있습니다.

```kotlin
// KnightsTheme 사용 예시
KnightsTheme {
    // 이 안의 모든 컴포넌트는 DroidKnights 디자인 시스템이 적용됩니다.
    Text(
        text = "Hello DroidKnights!",
        style = KnightsTheme.typography.headlineMediumB
    )
}
```

## 5. 결론 및 가이드라인

- **일관성 유지**: 새로운 UI를 개발할 때는 항상 `KnightsTheme` 내에서 작업하고, `MaterialTheme.colorScheme`, `KnightsTheme.typography`, `KnightsTheme.shape`에 정의된 토큰을 사용해야 합니다.
- **컴포넌트 재사용**: `Card`, `Chip`, `TopAppBar` 등 이미 만들어진 커스텀 컴포넌트를 최대한 재사용하여 일관된 사용자 경험을 제공합니다.
- **색상 사용**: 특정 색상 코드(예: `#FFFFFF`)를 직접 사용하기보다, `MaterialTheme.colorScheme.primary`와 같이 시맨틱한 색상 역할을 사용해야 테마(라이트/다크) 전환에 유연하게 대응할 수 있습니다.
- **확장**: 새로운 컴포넌트나 스타일이 필요한 경우, 기존 시스템의 명명 규칙과 구조를 따라 `core:designsystem` 모듈에 추가하는 것을 권장합니다.
