package com.openknights.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.openknights.designsystem.color.KnightsColor
import com.openknights.designsystem.shape.KnightsShape
import com.openknights.designsystem.typography.KnightsTypography
import com.openknights.designsystem.typography.LocalTypography
import com.openknights.designsystem.typography.Material3Typography

/**
 * `MaterialTheme`의 확장 프로퍼티로, 커스텀 정의된 `KnightsTypography`에 접근할 수 있도록 합니다.
 * 이 확장 프로퍼티를 통해 `MaterialTheme.knightsTypography.headlineMediumB`와 같이
 * MaterialTheme의 표준 타이포그래피와 함께 커스텀 스타일을 사용할 수 있습니다.
 */
val MaterialTheme.knightsTypography: KnightsTypography
    @Composable
    get() = LocalTypography.current

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
 * Module: core/designsystem - Material Design 3 가이드라인에 맞춰 앱의 디자인 시스템(테마, 색상, 타이포그래피, 도형)을 정의합니다.
 * `KnightsColor.kt`: 앱에서 사용될 색상 팔레트를 정의하는 파일입니다.
 * `KnightsShape.kt`: 앱에서 사용될 도형(모서리 곡률)을 정의하는 파일입니다.
 * `KnightsTypography.kt`: 앱에서 사용될 텍스트 스타일(폰트 크기, 두께, 줄 간격 등)을 정의하는 파일입니다.
 */
/**
 * `KnightsTheme`는 앱의 전체적인 디자인 테마를 설정하는 Composable 함수입니다.
 * Material Design 3의 색상, 타이포그래피, 도형 시스템을 통합하여 앱 전체에 일관된 디자인을 적용합니다.
 * `darkTheme` 인자를 통해 다크 모드와 라이트 모드를 전환할 수 있습니다.
 */
@Composable
fun KnightsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalTypography provides LocalTypography.current,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Material3Typography(),
            shapes = KnightsShape,
            content = content
        )
    }
}

@Preview(showBackground = true, name = "Light Theme Preview")
@Composable
fun KnightsThemeLightPreview() {
    KnightsTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Text("Hello Knights! (Light)", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true, name = "Dark Theme Preview")
@Composable
fun KnightsThemeDarkPreview() {
    KnightsTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Text("Hello Knights! (Dark)", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
