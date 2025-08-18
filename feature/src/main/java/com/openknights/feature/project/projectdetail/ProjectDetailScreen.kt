package com.openknights.feature.project.projectdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openknights.designsystem.theme.knightsTypography
import com.openknights.model.Project
import com.openknights.feature.R
import com.openknights.feature.project.di.ViewModelFactory
import com.openknights.feature.project.projectdetail.component.ProjectDetailChips
import com.openknights.feature.project.projectdetail.component.ProjectDetailTopAppBar
import com.openknights.feature.project.projectdetail.uistate.ProjectDetailUiState

import androidx.compose.foundation.layout.PaddingValues

// Screen: Project Detail
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onBack: () -> Unit,
    padding: PaddingValues
){
    val factory = ViewModelFactory(LocalContext.current)
    val viewModel: ProjectDetailViewModel = viewModel(factory = factory)
    val scrollState = rememberScrollState()
    val projectUiState by viewModel.projectUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim)
            .systemBarsPadding()
            .padding(padding) // Apply the passed padding
            .verticalScroll(scrollState),
    ) {
        ProjectDetailTopAppBar(
            onBackClick = { onBack() },
        )
        Box {
            when (val uiState = projectUiState) {
                is ProjectDetailUiState.Loading -> ProjectDetailLoading()
                is ProjectDetailUiState.Success -> ProjectDetailContent(uiState.project)
                is ProjectDetailUiState.Error -> Text("Error: ${uiState.message}")
            }
        }
    }

    LaunchedEffect(projectId) {
        viewModel.fetchProject(projectId)
    }
}

@Composable
private fun ProjectDetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProjectDetailContent(project: Project) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(end = 58.dp),
            text = project.name ?: "",
            style = MaterialTheme.knightsTypography.headlineMediumB,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProjectDetailChips(project = project)

        val description = project.description
        if (!description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ProjectOverview(content = description)
        }

        Spacer(modifier = Modifier.height(40.dp))
        // Leader
        project.leaderName?.let { leaderName ->
            Text(
                text = "Leader: $leaderName",
                style = MaterialTheme.knightsTypography.titleMediumB,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Members
        project.members?.let { members ->
            if (members.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Members:",
                        style = MaterialTheme.knightsTypography.titleMediumB,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    members.forEachIndexed { index, memberName ->
                        Text(
                            text = memberName,
                            style = MaterialTheme.knightsTypography.titleMediumB,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (index < members.size - 1) {
                            Text(
                                text = ", ",
                                style = MaterialTheme.knightsTypography.titleMediumB,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProjectOverview(content: String) {
    Column {
        Text(
            text = stringResource(id = R.string.project_overview_title),
            style = MaterialTheme.knightsTypography.titleSmallB,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.knightsTypography.titleSmallR140,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
