**Material Design 3 가이드에 맞춰**, `colorScheme`, `typography`, `shapes`를 모두 적용한 **완성도 높은 `KnightsTheme` 구현 예제**

---

## ✅ 1. 전체 보완된 코드

```kotlin
package com.openknights.mobile.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.openknights.app.core.designsystem.color.KnightsColor
import com.openknights.app.core.designsystem.shape.KnightsShape
import com.openknights.app.core.designsystem.typography.KnightsTypography

// Dark ColorScheme (Material 3 기반)
private val DarkColorScheme = darkColorScheme(
    primary = KnightsColor.White,
    onPrimary = KnightsColor.Blue01,
    secondary = KnightsColor.Gray03,
    onSecondary = KnightsColor.White,
    background = KnightsColor.Black,
    onBackground = KnightsColor.White,
    surface = KnightsColor.Gray01,
    onSurface = KnightsColor.White
)

// Light ColorScheme (Material 3 기반)
private val LightColorScheme = lightColorScheme(
    primary = KnightsColor.Neon01,
    onPrimary = KnightsColor.White,
    secondary = KnightsColor.Blue02,
    onSecondary = KnightsColor.White,
    background = KnightsColor.White,
    onBackground = KnightsColor.Black,
    surface = KnightsColor.Gray01,
    onSurface = KnightsColor.Black
)

// CompositionLocal for manual override if needed
val LocalDarkTheme = compositionLocalOf { true }

/**
 * Module: core/designsystem - 앱의 디자인 시스템(테마, 색상, 타이포그래피, 도형)을 정의합니다.
 */
@Composable
fun KnightsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KnightsTypography,
        shapes = KnightsShape,
        content = content
    )
}
```

---

## ✅ 2. 필요한 부가 모듈 구조

### 📦 `KnightsColor.kt`

```kotlin
object KnightsColor {
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Gray01 = Color(0xFFF5F5F5)
    val Gray03 = Color(0xFF9E9E9E)
    val Blue01 = Color(0xFF0D47A1)
    val Blue02 = Color(0xFF1976D2)
    val Neon01 = Color(0xFF00E5FF)
}
```

### 📦 `KnightsShape.kt`

```kotlin
val KnightsShape = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

### 📦 `KnightsTypography.kt`

```kotlin
val KnightsTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 64.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 36.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
    // 원하는대로 계속 추가 가능
)
```

---

## 🧪 4. 미리보기용 예시

```kotlin
@Preview(showBackground = true)
@Composable
fun KnightsThemePreview() {
    KnightsTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Text("Hello Knights!", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
```

---

## 🧩 결론

* `KnightsTheme`는 M3 기반으로 `colorScheme`, `typography`, `shapes` 모두 채워야 완성됩니다.
* 이렇게 하면 **전역에서 일관된 디자인을 유지**하고, 각 Feature는 `MaterialTheme.xxx`만 사용하면 됩니다.
* 실무에서는 `dynamicLightColorScheme()` 등을 이용해 **시스템 다이나믹 컬러 연동**도 가능하지만, 수동 테마 정의도 많이 사용됩니다.