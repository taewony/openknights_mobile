package com.openknights.designsystem.preview

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
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.designsystem.theme.knightsTypography

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
