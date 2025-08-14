package com.openknights.designsystem.typography

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// Define a data class to hold all custom typography styles
/**
 * `KnightsTypography`는 앱의 모든 커스텀 텍스트 스타일을 정의하는 데이터 클래스입니다.
 * Material Design의 기본 타이포그래피 외에 추가적으로 필요한 스타일(예: 볼드체, 특정 줄 간격 등)을 이곳에 정의합니다.
 */
data class KnightsTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 57.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 64.sp
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 36.sp
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    val labelSmall: TextStyle = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    ),
    val labelSmallM: TextStyle = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    ),
    val titleSmallM: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    ),
    val labelLargeM: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),
    val titleMediumB: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    ),
    // Custom typography styles
    val headlineMediumB: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp
    ),
    val titleSmallB: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.sp
    ),
    val titleSmallR140: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp * 1.4f
    ),
    val titleLargeB: TextStyle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
)

// Provide a default instance for the CompositionLocal
/**
 * `LocalTypography`는 `KnightsTypography` 객체를 Composition Local로 제공합니다.
 * 이를 통해 Composable 트리 어디에서든 `MaterialTheme.knightsTypography` 확장 프로퍼티를 통해
 * 커스텀 타이포그래피 스타일에 접근할 수 있습니다.
 */
val LocalTypography = compositionLocalOf { KnightsTypography() }

// Standard Material3 Typography for MaterialTheme
/**
 * `Material3Typography`는 표준 Material Design 3의 `Typography` 객체입니다.
 * `KnightsTypography`에 정의된 기본 스타일들을 사용하여 MaterialTheme에 전달됩니다.
 */
val Material3Typography = Typography(
    displayLarge = KnightsTypography().displayLarge,
    headlineMedium = KnightsTypography().headlineMedium,
    bodyLarge = KnightsTypography().bodyLarge,
    labelSmall = KnightsTypography().labelSmall
)