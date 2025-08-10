package com.openknights.feature.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UserScreen(
    userViewModel : UserViewModel = viewModel()
) {
    val uiState by userViewModel.uiState.collectAsState()

    UserScreenContent(
        uiState = uiState,
        onLoadUserClick = { userViewModel.loadUser("1") }
    )
}

@Composable
fun UserScreenContent(
    uiState: UserUiState,
    onLoadUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is UserUiState.Loading -> {
                CircularProgressIndicator()
            }
            is UserUiState.Success -> {
                Text("User ID: ${uiState.user.id}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("User Name: ${uiState.user.name}", style = MaterialTheme.typography.bodyLarge)
            }
            is UserUiState.Error -> {
                Text("Error: ${uiState.message}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }
            is UserUiState.Initial -> {
                Text("Click the button to load user data.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoadUserClick, enabled = uiState !is UserUiState.Loading) {
            Text(if (uiState is UserUiState.Loading) "Loading..." else "Load User")
        }
    }
}