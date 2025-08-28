# OpenKnights 앱 디자인 시스템 분석 및 진단

이 문서는 `openknights_mobile`의 `core:designsystem` 모듈을 분석하여, 앱의 디자인 언어, 토큰, 가이드라인을 설명하고 전문가 관점에서 문제점을 진단하며 올바른 활용 방안을 제시합니다.

## 1. 디자인 시스템 개요

OpenKnights 디자인 시스템은 Material 3를 기반으로, `DroidKnights` 앱의 디자인 시스템을 간소화하고 재구성하려는 초기 단계에 있는 것으로 보입니다. `KnightsTheme`를 중심으로 색상, 타이포그래피, 모양을 통합하여 앱의 전체적인 룩앤필을 관리하는 것을 목표로 합니다.

- **구조**: `color`, `shape`, `typography`, `theme` 등 기능별로 패키지가 명확하게 분리되어 있어 구조적으로 이해하기 쉽습니다.
- **테마**: 라이트/다크 모드를 모두 지원하며, `KnightsTheme` Composable을 통해 앱 전체에 일관된 스타일을 적용합니다.
- **확장성**: `CompositionLocalProvider`를 사용하여 커스텀 타이포그래피를 제공하고, `MaterialTheme`의 확장 프로퍼티(`knightsTypography`)를 통해 접근성을 높이는 등 좋은 확장 패턴을 사용하고 있습니다.

## 2. 디자인 토큰 분석

### 2.1. 색상 (Color)

- **정의**: `color/KnightsColor.kt` 와 `theme/KnightsColor.kt` 두 파일에 색상 팔레트가 정의되어 있습니다.
- **팔레트**:
  - `color/KnightsColor.kt`: `White`, `Black`, `Gray01`, `Gray03`, `Blue01`, `Blue02`, `Neon01` 등 핵심적인 색상만 간결하게 정의되어 있습니다.
  - `theme/KnightsColor.kt`: `DroidKnights`에서 가져온 것으로 보이는 매우 상세하고 많은 수의 색상이 정의되어 있습니다.
- **적용**: `KnightsTheme.kt`에서는 `color/KnightsColor.kt`에 정의된 간결한 팔레트를 사용하여 라이트/다크 컬러 스킴을 구성합니다.

### 2.2. 타이포그래피 (Typography)

- **정의**: `typography/KnightsTypography.kt`와 `theme/KnightsType.kt` 두 파일에 타이포그래피 시스템이 정의되어 있습니다.
- **스타일**:
  - `typography/KnightsTypography.kt`: `headlineMediumB`, `titleSmallB` 등 일부 커스텀 스타일과 함께 Material3의 기본 스타일(`displayLarge`, `bodyLarge` 등)을 포함하는 `KnightsTypography` 데이터 클래스를 정의합니다.
  - `theme/KnightsType.kt`: `DroidKnights`에서 가져온 것으로 보이는, 가중치(EB, SB, B, M, R 등)를 접미사로 사용하는 매우 상세하고 체계적인 타이포그래피 시스템이 정의되어 있습니다.
- **적용**: `KnightsTheme.kt`는 `typography/KnightsTypography.kt`에 정의된 `KnightsTypography`와 `Material3Typography`를 사용합니다.

### 2.3. 모양 (Shape)

- **정의**: `shape/KnightsShape.kt`와 `theme/KnightsShape.kt` 두 파일에 모양 시스템이 정의되어 있습니다.
- **스타일**:
  - `shape/KnightsShape.kt`: Material 3의 표준 `Shapes` 객체를 사용하여 `extraSmall`부터 `extraLarge`까지 5단계의 모서리 둥글기를 정의합니다.
  - `theme/KnightsShape.kt`: `chip`, `rounded12` 등 특정 목적을 가진 커스텀 `KnightsShape` 데이터 클래스를 정의합니다.
- **적용**: `KnightsTheme.kt`는 `shape/KnightsShape.kt`에 정의된 표준 `Shapes` 객체를 사용합니다.

## 3. 디자인 시스템 평가 및 문제점

`openknights_mobile`의 디자인 시스템은 좋은 구조를 갖추고 있으나, `DroidKnights` 프로젝트에서 코드를 가져오는 과정에서 발생한 것으로 보이는 몇 가지 정리되지 않은 문제점들이 존재합니다.

### 3.1. 파일 중복 및 미사용 코드

- **가장 큰 문제**: `theme` 패키지 내에 `KnightsColor.kt`, `KnightsShape.kt`, `KnightsType.kt` 파일이 존재하지만, 실제 `KnightsTheme`에서는 이 파일들을 전혀 사용하지 않습니다. 대신 각각 `color`, `shape`, `typography` 패키지의 파일들을 참조합니다.
- **영향**:
  - **혼란 유발**: 어떤 파일을 수정해야 테마에 반영되는지 파악하기 어렵습니다.
  - **유지보수 비용 증가**: 불필요한 코드가 프로젝트에 남아있어 관리 포인트를 늘립니다.
- **해결 방안**: `theme` 패키지 내의 중복 파일들(`KnightsColor.kt`, `KnightsShape.kt`, `KnightsType.kt`)을 **과감히 삭제**하여 코드를 정리해야 합니다.

### 3.2. 타이포그래피 시스템의 불일치

- **문제**: `typography/KnightsTypography.kt`는 몇 가지 스타일만 정의하고 있는 반면, `theme/KnightsType.kt`에는 훨씬 더 체계적이고 상세한 시스템이 존재합니다. 현재 `KnightsTheme`는 간소화된 버전을 사용하고 있어, 표현력 풍부한 타이포그래피 시스템의 이점을 활용하지 못하고 있습니다.
- **해결 방안**:
  1.  프로젝트의 타이포그래피 요구사항을 명확히 정의합니다.
  2.  하나의 타이포그래피 시스템만 남기기로 결정합니다. (상세한 시스템인 `theme/KnightsType.kt`를 `typography` 패키지로 옮겨와 메인 시스템으로 사용하는 것을 추천합니다.)
  3.  `KnightsTheme`가 선택된 단일 타이포그래피 시스템을 사용하도록 수정합니다.

### 3.3. 색상 정의의 불일치

- **문제**: 타이포그래피와 마찬가지로, `color/KnightsColor.kt`의 간소화된 색상 팔레트가 실제 테마에 사용되고 있어, `theme/KnightsColor.kt`에 정의된 풍부한 색상들을 활용하지 못하고 있습니다.
- **해결 방안**: 프로젝트에 필요한 색상을 `color/KnightsColor.kt`에 명확히 정의하고, 불필요한 `theme/KnightsColor.kt`는 삭제합니다. 만약 더 많은 색상이 필요하다면, `theme/KnightsColor.kt`에서 필요한 색상만 `color/KnightsColor.kt`로 옮겨와 정리하는 과정이 필요합니다.

## 4. 활용 가이드

위의 문제점들이 해결되었다고 가정하고, 이 디자인 시스템을 올바르게 활용하는 방법은 다음과 같습니다.

### 4.1. 테마 적용

앱의 최상위 Composable을 `KnightsTheme`으로 감싸 앱 전체에 디자인 시스템을 적용합니다.

```kotlin
// MainActivity.kt 또는 앱의 메인 화면
setContent {
    KnightsTheme { // darkTheme = true/false 로 수동 설정 가능
        // 이 안의 모든 Composable은 OpenKnights 디자인 시스템의 영향을 받습니다.
        YourAppNavigation()
    }
}
```

### 4.2. 색상 사용

`MaterialTheme.colorScheme`을 통해 시맨틱한 색상을 사용합니다. 하드코딩된 색상 사용은 피해야 합니다.

```kotlin
// Good
Text(
    text = "Primary Color",
    color = MaterialTheme.colorScheme.primary
)

// Bad
Text(
    text = "Hardcoded Color",
    color = Color(0xFF00E5FF) // KnightsColor.Neon01
)
```

### 4.3. 타이포그래피 사용

`MaterialTheme.typography`를 통해 표준 스타일을 사용하거나, `MaterialTheme.knightsTypography` 확장 프로퍼티를 통해 커스텀 스타일을 사용합니다.

```kotlin
// 표준 Material 3 스타일 사용
Text(
    text = "Headline Medium",
    style = MaterialTheme.typography.headlineMedium
)

// 커스텀 Knights 스타일 사용 (정리 후)
Text(
    text = "Headline Medium Bold",
    style = MaterialTheme.knightsTypography.headlineMediumB
)
```

### 4.4. 모양 사용

`MaterialTheme.shapes`를 통해 정의된 모양을 사용합니다.

```kotlin
Card(
    shape = MaterialTheme.shapes.medium, // 12.dp 둥근 모서리
    ...
)
```

## 5. 결론

`openknights_mobile`의 디자인 시스템은 좋은 기반을 갖추고 있지만, 코드 정리와 방향성 확립이 시급합니다. `DroidKnights`에서 가져온 미사용/중복 코드를 제거하고, 프로젝트의 규모와 요구사항에 맞는 단일화된 색상 및 타이포그래피 시스템을 정립하는 것이 최우선 과제입니다. 이 과정을 거치면 유지보수성이 높고 일관된 디자인 시스템으로 발전할 수 있을 것입니다.
