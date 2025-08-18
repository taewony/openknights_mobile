package com.openknights.feature.user

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import com.openknights.feature.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.openknights.data.repository.UserRepositoryImpl
import com.openknights.feature.user.uistate.UserScreenState
import com.openknights.feature.user.uistate.UserUiState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun UsersScreen(
    padding: PaddingValues,
    userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            UserRepositoryImpl(LocalContext.current.applicationContext, Firebase.firestore, FirebaseStorage.getInstance())
        )
    ),
    onUserClick: (String) -> Unit // uid를 받아 처리하는 콜백
) {
    val uiState by userViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("UsersScreen", "Automatically load users when the screen is composed")
        userViewModel.loadUsers()
    }

    UsersScreenContent(
        modifier = Modifier.padding(padding),
        uiState = uiState,
        onUserClick = onUserClick
    )
}

@Composable
fun UsersScreenContent(
    modifier: Modifier = Modifier,
    uiState: UserScreenState,
    onUserClick: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is UserScreenState.Loading -> {
                CircularProgressIndicator()
            }
            is UserScreenState.Success -> {
                UserList(users = uiState.users, onUserClick = onUserClick)
            }
            is UserScreenState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun UserList(
    users: ImmutableList<UserUiState>,
    onUserClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            UserCard(userState = user, onUserClick = onUserClick)
        }
    }
}

@Composable
fun UserCard(
    userState: UserUiState,
    onUserClick: (String) -> Unit
) {
    val cardModifier = if (userState.isCurrentUser) {
        Modifier.fillMaxWidth().clickable { onUserClick(userState.uid) }
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타 이미지
            // 표시 우선순위: profileImageUrl (원격) -> localProfileImageResId (로컬) -> Icons.Default.Person (기본 아이콘)
            val imageModifier = Modifier.size(80.dp).clip(CircleShape)

            if (userState.profileImageUrl != null && userState.profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = userState.profileImageUrl,
                    contentDescription = "User Profile Image",
                    modifier = imageModifier,
                    placeholder = painterResource(id = R.drawable.default_avatar), // 로딩 중 기본 이미지
                    error = painterResource(id = R.drawable.default_avatar), // 에러 시 기본 이미지
                    contentScale = ContentScale.Crop
                )
            } /* else if (userState.localProfileImageResId != null) {
                Image(
                    painter = painterResource(id = userState.localProfileImageResId),
                    contentDescription = "User Profile Image",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } */ else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default User Icon",
                    modifier = imageModifier,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 아이콘 색상 조정
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 사용자 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userState.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userState.introduction,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}