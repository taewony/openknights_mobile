package com.openknights.feature.project.projectlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.openknights.feature.project.projectlist.component.ProjectCard
import com.openknights.feature.project.projectlist.component.ProjectListTopAppBar
import com.openknights.designsystem.theme.knightsTypography
import com.openknights.feature.project.di.ViewModelFactory
import com.openknights.feature.project.projectlist.model.ProjectUiState
import com.openknights.feature.project.projectlist.model.rememberProjectState
import com.openknights.model.Phase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

/**
 * Module: feature/project - 프로젝트 목록 화면을 정의합니다.
 * Screen: Project List 
 */
@Composable
fun ProjectListScreen(
    contestTerm: String,   // 프로젝트 목록을 가져올 기준 (예: "2024-1st")
    onProjectClick: (Long) -> Unit,  // 유저가 프로젝트 카드를 클릭했을 때 호출되는 콜백
    onShowErrorSnackBar: (throwable: Throwable?) -> Unit, // 에러 발생 시 스낵바를 보여주기 위한 콜백
    scrollToProjectId: String? = null,   // 특정 프로젝트로 스크롤하기 위한 ID, null이면 스크롤하지 않음
    padding: PaddingValues,
) {
    val factory = ViewModelFactory(LocalContext.current)
    val viewModel: ProjectListViewModel = viewModel(factory = factory)
    val density = LocalDensity.current

    // StateFlow로 관리되는 ViewModel의 상태(uiState)를 Compose에서 관찰
    // collectAsStateWithLifecycle을 사용하여 ViewModel의 상태를 관찰하고, 상태가 변경될 때 UI가 자동으로 recomposition
    val projectUiState by viewModel.uiState.collectAsStateWithLifecycle()

    // projectUiState가 ProjectUiState.Projects 타입이면 내부 프로젝트 리스트를 꺼냄
    // .groups.flatMap { it.projects }: 여러 그룹에 속한 모든 프로젝트들을 한 리스트로 변환
    // rememberProjectState(...): Compose에서 재구성을 방지하기 위해 프로젝트 상태를 메모리에 저장
    // 없을 경우에는 빈 리스트로 상태 생성
    val projectState = (projectUiState as? ProjectUiState.Projects)?.let { projects ->
        rememberProjectState(projects = projects.groups.flatMap { it.projects }.toPersistentList())
    } ?: rememberProjectState(projects = persistentListOf())

    LaunchedEffect(contestTerm) {
        viewModel.fetchProjects(contestTerm)
        viewModel.errorFlow.collectLatest { throwable -> onShowErrorSnackBar(throwable) }
    }

    var highlighted by remember { mutableStateOf(false) }

    LaunchedEffect(scrollToProjectId, projectState.groups) {
        scrollToProjectId?.let { projectId ->
            delay(300)
            val offset = with(density) { ((-6).dp).toPx().toInt() }
            projectState.scrollToProject(projectId, offset)
            delay(300)
            highlighted = true
            delay(500)
            highlighted = false
        }
    }

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        ProjectListTopAppBar(
            projectState = projectState,
            onBackClick = {}, //backStack.removeLastOrNull()
            contestTerm = contestTerm,
        )
        ProjectList(
            projectUiState = projectUiState,
            onProjectClick = onProjectClick,
            modifier = Modifier
                .systemBarsPadding()
                .padding(top = 48.dp)
                .fillMaxSize(),
            highlightProjectId = if (highlighted) scrollToProjectId else null,
        )
    }
}

@Composable
private fun ProjectList(
    projectUiState: ProjectUiState,
    onProjectClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    highlightProjectId: String? = null,
) {
    when (projectUiState) {
        ProjectUiState.Loading -> {
            // TODO: Show loading indicator
        }
        is ProjectUiState.Projects -> {
            val projectState = rememberProjectState(projects = projectUiState.groups.flatMap { it.projects }.toPersistentList())
            val totalProjectCount = projectUiState.groups.sumOf { it.projects.size }

            Column(modifier = modifier) {
                Text(
                    text = "총 ${totalProjectCount}개 프로젝트",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    state = projectState.listState,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val allProjects = projectUiState.groups.flatMap { it.projects }
                    itemsIndexed(items = allProjects) { index, project ->
                        ProjectCard(
                            project = project,
                            isHighlighted = project.id == highlightProjectId?.toLongOrNull(),
                            onProjectClick = { project -> onProjectClick(project.id) },
                            index = index
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectPhaseTitle(
    projectPhase: Phase,
    topPadding: Dp,
) {
    Column(
        modifier = Modifier
            .padding(start = 20.dp, top = topPadding, end = 20.dp)
    ) {
        Text(
            text = projectPhase.label,
            style = MaterialTheme.knightsTypography.titleLargeB,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)

        Spacer(
            modifier = Modifier
                .height(32.dp)
        )
    }
}

private val ProjectTopSpace = 16.dp
private val ProjectGroupSpace = 100.dp

@Preview(showBackground = true)
@Composable
private fun ProjectListScreenPreview() {
    ProjectListScreen(
        contestTerm = "2024-1st",
        onProjectClick = {},
        onShowErrorSnackBar = {},
        padding = PaddingValues(all = 16.dp)
    )
}