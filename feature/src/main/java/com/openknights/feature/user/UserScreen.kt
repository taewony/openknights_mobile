package com.openknights.feature.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openknights.data.repository.UserRepositoryImpl
import com.openknights.model.User

@Composable
fun UserScreen(
    userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            UserRepositoryImpl(LocalContext.current.applicationContext)
        )
    )
) {
    val uiState by userViewModel.uiState.collectAsState()

    // 화면이 처음 구성될 때 사용자 목록을 자동으로 불러오도록 LaunchedEffect 사용
    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }

    UserScreenContent(
        uiState = uiState,
        onReloadClick = { userViewModel.loadUsers() } // 버튼 클릭 시 재로딩
    )
}

@Composable
fun UserScreenContent(
    uiState: UserUiState,
    onReloadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is UserUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is UserUiState.Success -> {
                    // 성공 상태일 때 LazyColumn으로 사용자 목록 표시
                    UserList(users = uiState.users)
                }
                is UserUiState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is UserUiState.Initial -> {
                    Text(
                        text = "Loading user data...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReloadClick, enabled = uiState !is UserUiState.Loading) {
            Text(if (uiState is UserUiState.Loading) "Loading..." else "Reload Users")
        }
    }
}

@Composable
fun UserList(users: List<User>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            UserCard(user = user)
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${user.studentId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Roles: ${user.roles.joinToString()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
