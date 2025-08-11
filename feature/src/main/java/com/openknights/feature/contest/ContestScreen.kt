package com.openknights.feature.contest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openknights.data.repository.ContestRepositoryImpl
import com.openknights.model.Contest

@Composable
fun ContestScreen(modifier: Modifier = Modifier) {
    val contestViewModel: ContestViewModel = viewModel(
        factory = ContestViewModelFactory(ContestRepositoryImpl(LocalContext.current.applicationContext))
    )
    val uiState by contestViewModel.uiState.collectAsState()

    ContestScreenContent(
        modifier = modifier,
        uiState = uiState,
        onLoadClick = { contestViewModel.loadContests() }
    )
}

@Composable
fun ContestScreenContent(
    modifier: Modifier = Modifier,
    uiState: ContestUiState,
    onLoadClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ContestUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is ContestUiState.Success -> {
                    ContestList(contests = uiState.contests)
                }

                is ContestUiState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ContestUiState.Initial -> {
                    Text(text = "대회 정보를 보려면 버튼을 눌러주세요.")
                }
            }
        }

        if (uiState !is ContestUiState.Loading) {
            Button(
                onClick = onLoadClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (uiState is ContestUiState.Success) "Reload Contests" else "Load Contests")
            }
        }
    }
}

@Composable
fun ContestList(contests: List<Contest>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contests) { contest ->
            ContestCard(contest = contest)
        }
    }
}

@Composable
fun ContestCard(contest: Contest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = contest.description,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "기간: ${contest.term}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "담당: ${contest.staff.joinToString()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "상태: ${contest.phase?.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}