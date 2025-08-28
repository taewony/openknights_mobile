package com.openknights.designsystem.typography

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SansSerif = FontFamily.SansSerif

private val SansSerifStyle = TextStyle(
    fontFamily = SansSerif,
    fontWeight = FontWeight.Normal,
)

data class KnightsTypography(
    val displayLargeR: TextStyle,
    val displayMediumR: TextStyle,
    val displaySmallR: TextStyle,
    val headlineLargeB: TextStyle,
    val headlineLargeM: TextStyle,
    val headlineLargeR: TextStyle,
    val headlineMediumB: TextStyle,
    val headlineMediumM: TextStyle,
    val headlineMediumR: TextStyle,
    val headlineSmallBL: TextStyle,
    val headlineSmallB: TextStyle,
    val headlineSmallM: TextStyle,
    val headlineSmallR: TextStyle,
    val titleLargeBL: TextStyle,
    val titleLargeB: TextStyle,
    val titleLargeM: TextStyle,
    val titleLargeR: TextStyle,
    val titleMediumBL: TextStyle,
    val titleMediumB: TextStyle,
    val titleMediumR: TextStyle,
    val titleSmallM: TextStyle,
    val titleSmallB: TextStyle,
    val titleSmallR140: TextStyle,
    val titleSmallR: TextStyle,
    val labelLargeM: TextStyle,
    val labelSmallM: TextStyle,
    val bodyLargeR: TextStyle,
    val bodyMediumR: TextStyle,
    val bodySmallR: TextStyle,
)

val LocalTypography = staticCompositionLocalOf {
    KnightsTypography(
        displayLargeR = SansSerifStyle.copy(
            fontSize = 57.sp,
            lineHeight = 64.sp,
        ),
        displayMediumR = SansSerifStyle.copy(
            fontSize = 45.sp,
            lineHeight = 52.sp,
        ),
        displaySmallR = SansSerifStyle.copy(
            fontSize = 36.sp,
            lineHeight = 44.sp,
        ),
        headlineLargeB = SansSerifStyle.copy(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineLargeM = SansSerifStyle.copy(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Medium,
        ),
        headlineLargeR = SansSerifStyle.copy(
            fontSize = 32.sp,
            lineHeight = 40.sp,
        ),
        headlineMediumB = SansSerifStyle.copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineMediumM = SansSerifStyle.copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Medium,
        ),
        headlineMediumR = SansSerifStyle.copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
        ),
        headlineSmallBL = SansSerifStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        ),
        headlineSmallB = SansSerifStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmallM = SansSerifStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        ),
        headlineSmallR = SansSerifStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        titleLargeBL = SansSerifStyle.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
        ),
        titleLargeB = SansSerifStyle.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleLargeM = SansSerifStyle.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleLargeR = SansSerifStyle.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMediumBL = SansSerifStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp,
        ),
        titleMediumB = SansSerifStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleMediumR = SansSerifStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        titleSmallM = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleSmallB = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleSmallR140 = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        titleSmallR = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelLargeM = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        ),
        labelSmallM = SansSerifStyle.copy(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.2).sp,
        ),
        bodyLargeR = SansSerifStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMediumR = SansSerifStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp,
        ),
        bodySmallR = SansSerifStyle.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
    )
}

@Composable
fun Material3Typography(): Typography {
    val typography = LocalTypography.current
    return Typography(
        displayLarge = typography.displayLargeR,
        displayMedium = typography.displayMediumR,
        displaySmall = typography.displaySmallR,
        headlineLarge = typography.headlineLargeR,
        headlineMedium = typography.headlineMediumR,
        headlineSmall = typography.headlineSmallR,
        titleLarge = typography.titleLargeR,
        titleMedium = typography.titleMediumR,
        titleSmall = typography.titleSmallR,
        bodyLarge = typography.bodyLargeR,
        bodyMedium = typography.bodyMediumR,
        bodySmall = typography.bodySmallR,
        labelLarge = typography.labelLargeM,
        labelSmall = typography.labelSmallM,
    )
}