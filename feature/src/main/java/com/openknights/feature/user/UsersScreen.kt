package com.openknights.feature.user

import android.util.Log
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

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun UsersScreen(
    padding: PaddingValues,
    userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            UserRepositoryImpl(LocalContext.current.applicationContext, Firebase.firestore)
        )
    )
) {
    val uiState by userViewModel.uiState.collectAsState()

    // Automatically load users when the screen is composed
    LaunchedEffect(Unit) {
        Log.d("UsersScreen", "Automatically load users when the screen is composed")
        userViewModel.loadUsers()
    }

    
    UsersScreenContent(
        modifier = Modifier.padding(padding),
        uiState = uiState,
        onLoadClick = { userViewModel.loadUsers() }, // 버튼 클릭 시 로딩
        onSaveClick = { users -> userViewModel.saveUsersToFirestore(users) } // Firestore에 저장
    )
}

@Composable
fun UsersScreenContent(
    modifier: Modifier = Modifier,
    uiState: UserUiState,
    onLoadClick: () -> Unit,
    onSaveClick: (List<User>) -> Unit // Add this parameter
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 컨텐츠를 중앙으로 정렬
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
                    // 초기 상태일 때 아무것도 표시하지 않거나, 안내 문구를 표시할 수 있습니다.
                    // 여기서는 버튼이 중앙에 오도록 Box를 비워둡니다.
                }
            }
        }

        /* 초기 상태이거나 에러가 발생했을 때만 버튼을 크게 표시
        if (uiState is UserUiState.Initial || uiState is UserUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoadClick, enabled = uiState !is UserUiState.Loading) {
                Text("Load Users")
            }
        } else if (uiState is UserUiState.Success) {
            // 데이터가 성공적으로 로드된 후에는 'Reload' 버튼을 표시
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoadClick, enabled = uiState !is UserUiState.Loading) {
                Text(if (uiState is UserUiState.Loading) "Loading..." else "Reload Users")
            }
            Spacer(modifier = Modifier.height(8.dp)) // Add a small spacer
            Button(
                onClick = { onSaveClick(uiState.users) }, // Call onSaveClick with the user list
                enabled = uiState !is UserUiState.Loading
            ) {
                Text("Save Users to Firestore")
            }
        }*/
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
                text = user.introduction,
                style = MaterialTheme.typography.bodyMedium
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
