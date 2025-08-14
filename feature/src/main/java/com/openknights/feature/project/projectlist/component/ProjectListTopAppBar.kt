package com.openknights.feature.project.projectlist.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.openknights.ui.KnightsTopAppBar
import com.openknights.ui.TopAppBarNavigationType
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.designsystem.theme.knightsTypography
import com.openknights.model.Phase
import com.openknights.feature.project.projectlist.model.ProjectState
import com.openknights.feature.R
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.map

@Composable
internal fun ProjectListTopAppBar(
    projectState: ProjectState,
    onBackClick: () -> Unit,
    contestTerm: String, // contestTerm 파라미터 추가
) {
    val phases = projectState.phases
    val coroutineScope = rememberCoroutineScope()

    Box {
        if (phases.isNotEmpty()) {
            AnimatedVisibility(
                visible = projectState.isAtTop.not(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ProjectPhaseTabRow(
                    selectedProjectPhase = projectState.selectedProjectPhase,
                    phases = phases.toPersistentList(),
                    onProjectPhaseSelect = { phase ->
                        coroutineScope.launch {
                            projectState.scrollTo(phase)
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        }
        AnimatedVisibility(
            visible = projectState.isAtTop,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            KnightsTopAppBar(
                title = "프로젝트 목록(${contestTerm})", // 제목 변경
                navigationType = TopAppBarNavigationType.Close,
                navigationIconContentDescription = null,
                modifier = Modifier.statusBarsPadding(),
                onNavigationClick = onBackClick,
            )
        }
    }
}

@Composable
private fun ProjectPhaseTabRow(
    selectedProjectPhase: Phase?,
    phases: PersistentList<Phase>,
    onProjectPhaseSelect: (Phase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val tabWidths = remember {
        mutableStateListOf<Dp>().apply { addAll(phases.map { 0.dp }) }
    }
    val selectedTabIndex = phases.indexOf(selectedProjectPhase)
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabIndicator(
                height = 3.dp,
                modifier = Modifier.tabIndicatorOffset(
                    currentTabPosition = tabPositions[selectedTabIndex],
                    tabWidth = tabWidths[selectedTabIndex]
                )
            )
        },
        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }
    ) {
        phases.forEachIndexed { tabIndex, projectPhase ->
            ProjectPhaseTab(
                projectPhase = projectPhase,
                selected = selectedProjectPhase == projectPhase,
                onClick = { onProjectPhaseSelect(projectPhase) },
                onTextLayout = { textLayoutResult ->
                    tabWidths[tabIndex] = with(density) { textLayoutResult.size.width.toDp() }
                }
            )
        }
    }
}

@Composable
private fun TabIndicator(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val brush = SolidColor(MaterialTheme.colorScheme.onSurface)
    Box(
        modifier = modifier
            .height(height * 2)
            .offset { IntOffset(0, height.roundToPx()) }
            .background(brush, RoundedCornerShape(100.dp, 100.dp, 0.dp, 0.dp))
    )
}

@Composable
private fun ProjectPhaseTab(
    projectPhase: Phase,
    selected: Boolean,
    onClick: () -> Unit,
    onTextLayout: (TextLayoutResult) -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = projectPhase.label,
                style = MaterialTheme.knightsTypography.titleSmallM,
                onTextLayout = { textLayoutResult -> onTextLayout(textLayoutResult) }
            )
        }
    )
}

private fun Modifier.tabIndicatorOffset(
    currentTabPosition: TabPosition,
    tabWidth: Dp,
): Modifier = composed {
    val currentTabWidth by animateDpAsState(
        targetValue = tabWidth,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "currentTabWidth",
    )
    val indicatorOffset by animateDpAsState(
        targetValue = ((currentTabPosition.left + currentTabPosition.right - tabWidth) / 2),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "indicatorOffset",
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset)
        .width(currentTabWidth)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ProjectListTopAppBarPreview() {
    KnightsTheme {
        ProjectListTopAppBar(
            projectState = ProjectState(
                projects = persistentListOf(),
                listState = rememberLazyListState()
            ),
            onBackClick = { },
            contestTerm = "2024-1st" // Preview용 contestTerm 추가
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ProjectPhaseTabIndicatorPreview() {
    KnightsTheme {
        ProjectPhaseTabRow(
            selectedProjectPhase = Phase.FINALIST,
            phases = Phase.entries.toPersistentList(),
            onProjectPhaseSelect = { },
            modifier = Modifier.size(320.dp, 48.dp),
        )
    }
}