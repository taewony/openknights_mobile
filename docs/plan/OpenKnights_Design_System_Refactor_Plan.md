# OpenKnights 디자인 시스템 개선 작업 계획서

이 문서는 `openknights` 앱의 `core:designsystem` 모듈을 개선하기 위한 구체적인 실행 계획을 정의합니다.

## 1. 최종 목표 (Goal)

- **단일 진실 공급원 (Single Source of Truth)**: 중복 코드를 제거하여 색상, 타이포그래피, 모양(Shape)에 대한 명확한 단일 진실 공급원을 확립합니다.
- **유지보수성 향상**: 불필요한 파일을 제거하고 구조를 단순화하여 코드의 이해와 유지보수를 용이하게 만듭니다.
- **개발자 경험 개선**: Preview 기능을 적극적으로 활용하여 디자인 변경사항을 즉시 확인하고, Dark/Light 모드를 쉽게 테스트할 수 있는 환경을 구축합니다.

---

## 2. 개선 작업 상세 계획 (Detailed Refactoring Plan)

**작업 위치**: `D:\code\mobileApp\ComposeLab\openknights\core\designsystem\src\main\java\com\openknights\app\core\designsystem`

### 2.1. Step 1: 중복 및 미사용 파일 제거 (완료)

- **목표**: 현재 `theme` 패키지에 존재하지만 실제로는 사용되지 않는 중복 파일들을 제거하여 혼란을 없앱니다.
- **실행 항목** (완료):
  - 아래 파일들을 프로젝트에서 **삭제**했습니다.
    1.  `theme/KnightsColor.kt`
    2.  `theme/KnightsShape.kt`
    3.  `theme/KnightsType.kt`

### 2.2. Step 2: 색상 시스템 단일화 (완료)

- **목표**: `color` 패키지를 색상 토큰의 유일한 관리 지점으로 지정합니다.
- **실행 항목** (완료):
  1.  `color/KnightsColor.kt` 파일을 열었습니다.
  2.  삭제된 `theme/KnightsColor.kt` 파일에서 필요했던 색상들을 `color/KnightsColor.kt`로 옮겨와 통합했습니다. (`Blue03`, `PaleGray` 추가)
  3.  `theme/KnightsTheme.kt`에서 `darkColorScheme`과 `lightColorScheme`이 `com.openknights.app.core.designsystem.color.KnightsColor`를 올바르게 import하여 사용하고 있는지 확인했습니다.

### 2.3. Step 3: 타이포그래피 시스템 단일화 (완료)

- **목표**: `typography` 패키지를 타이포그래피의 유일한 관리 지점으로 지정하고, 더 상세한 시스템을 채택합니다.
- **실행 항목** (완료):
  1.  `typography/KnightsTypography.kt` 파일의 내용을 **업데이트**했습니다.
  2.  삭제된 `theme/KnightsType.kt` 파일의 전체 내용을 `typography/KnightsTypography.kt` 파일에 **통합**했습니다. (`titleSmallB` 등 추가)
  3.  `typography/KnightsTypography.kt` 내의 `Material3Typography`를 `@Composable` 함수로 변경했습니다.
  4.  `theme/KnightsTheme.kt`에서 `LocalTypography`를 제공하는 부분을 확인하고, 새로 통합된 `KnightsTypography`를 사용하도록 수정했습니다. (`Material3Typography()` 호출)

### 2.4. Step 4: Shape 시스템 확인 (완료)

- **목표**: `shape` 패키지를 Shape 토큰의 유일한 관리 지점으로 지정합니다.
- **실행 항목** (완료):
  1.  `theme/KnightsTheme.kt`에서 `shapes` 파라미터가 `com.openknights.app.core.designsystem.shape.KnightsShape`를 올바르게 import하여 사용하고 있는지 확인했습니다.

---

## 3. 프리뷰를 통한 디자인 테스트 및 Dark/Light 모드 전환 (완료)

- **목표**: 디자인 시스템의 변경사항을 시각적으로 즉시 테스트하고, Dark/Light 모드 전환을 쉽게 제어할 수 있는 환경을 구축합니다.

### 3.1. 테스트용 Preview 파일 생성 (완료)

- **실행 항목** (완료):
  1.  `designsystem` 모듈 내에 `preview` 라는 새 패키지를 생성했습니다.
     - `D:\code\mobileApp\ComposeLab\openknights\core\designsystem\src\main\java\com\openknights\app\core\designsystem\preview`
  2.  해당 패키지 안에 `DesignSystemPreview.kt` 파일을 생성했습니다.

### 3.2. 디자인 시스템 테스트용 Preview 코드 작성 (완료)

- **실행 항목** (완료): `DesignSystemPreview.kt` 파일에 아래와 같이 코드를 작성하여 색상, 타이포그래피, 컴포넌트를 한눈에 볼 수 있는 Preview를 구성했습니다.

```kotlin
package com.openknights.app.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openknights.app.core.designsystem.theme.KnightsTheme
import com.openknights.app.core.designsystem.theme.knightsTypography

// 단일 테마(Light/Dark)에 대한 디자인 시스템 요소들을 보여주는 Composable
@Composable
private fun DesignSystemContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Color Palette Preview
        item {
            Text("Color Palette", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            ColorPalettePreview()
        }

        // 2. Typography Preview
        item {
            Text("Typography", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            TypographyPreview()
        }
        
        // 3. Shape & Component Preview
        item {
            Text("Shape & Component", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Card(shape = MaterialTheme.shapes.large) {
                Text("Card with Large Shape", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

// Dark/Light 모드를 동시에 보며 테스트하기 위한 Preview 설정
@Preview(name = "Design System - Light Mode", showBackground = true, widthDp = 360)
@Composable
fun DesignSystemLightPreview() {
    KnightsTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            DesignSystemContent()
        }
    }
}

@Preview(name = "Design System - Dark Mode", showBackground = true, widthDp = 360)
@Composable
fun DesignSystemDarkPreview() {
    KnightsTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            DesignSystemContent()
        }
    }
}

// 세부 Preview 컴포저블
@Composable
private fun ColorPalettePreview() {
    val colors = mapOf(
        "primary" to MaterialTheme.colorScheme.primary,
        "secondary" to MaterialTheme.colorScheme.secondary,
        "background" to MaterialTheme.colorScheme.background,
        "surface" to MaterialTheme.colorScheme.surface,
        "onPrimary" to MaterialTheme.colorScheme.onPrimary,
        "onBackground" to MaterialTheme.colorScheme.onBackground
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        colors.forEach { (name, color) ->
            ColorChip(name = name, color = color)
        }
    }
}

@Composable
private fun TypographyPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // MaterialTheme.typography 와 커스텀 확장인 knightsTypography 를 모두 테스트
        Text("Headline Medium (M3)", style = MaterialTheme.typography.headlineMedium)
        Text("Headline Medium Bold (Custom)", style = MaterialTheme.knightsTypography.headlineMediumB)
        Text("Title Large Bold (Custom)", style = MaterialTheme.knightsTypography.titleLargeB)
        Text("Body Large (M3)", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ColorChip(name: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color), shape = MaterialTheme.shapes.small) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = if (name.startsWith("on")) Color.Black else Color.White // 대비를 위해 임시 처리
        )
    }
}

```

---

## 4. 최종 App 모듈 적용 방안 (완료)

- **목표**: 개선된 디자인 시스템을 `app` 모듈에 적용하여 앱 전체의 UI 일관성을 확보합니다.
- **실행 항목** (완료):
  1.  `openknights`의 `app` 모듈에 있는 `MainActivity.kt` (또는 앱의 진입점) 파일을 열었습니다.
  2.  최상위 Composable을 `KnightsTheme`으로 감싸주었습니다.
  3.  `OpenKnightsApp.kt`의 `TopAppBar` 제목에 `MaterialTheme.knightsTypography.headlineMediumB` 스타일을 명시적으로 적용했습니다.
  4.  `OpenKnightsApp.kt`에 `import com.openknights.app.core.designsystem.theme.knightsTypography`를 추가했습니다.

```kotlin
// openknights/app/src/main/java/.../MainActivity.kt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 개선된 KnightsTheme을 앱 전체에 적용
            KnightsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // 여기에 앱의 메인 네비게이션 또는 화면이 위치합니다.
                    Greeting("OpenKnights")
                }
            }
        }
    }
}
```

---

## 5. 기대 효과 (Expected Outcomes)

- **명확한 코드 베이스**: 더 이상 사용되지 않는 코드가 사라져, 처음 프로젝트를 접하는 개발자도 디자인 시스템의 구조를 쉽게 파악할 수 있습니다.
- **효율적인 디자인 작업**: 통합된 Preview 환경에서 다양한 디자인 옵션을 빠르게 테스트하고, Dark/Light 모드에서의 UI 깨짐 현상을 사전에 방지할 수 있습니다.
- **일관된 사용자 경험**: 앱 전체에 통일된 색상, 타이포그래피, 모양이 적용되어 사용자에게 일관되고 안정적인 경험을 제공합니다.