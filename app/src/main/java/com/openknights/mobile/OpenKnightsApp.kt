package com.openknights.mobile

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

// B. 이전 버전과 동일하게 화면 Entry 정의 추가
import com.openknights.designsystem.theme.KnightsTheme
import com.openknights.feature.contest.ContestsScreen
import com.openknights.feature.project.projectdetail.ProjectDetailScreen
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.openknights.feature.project.projectlist.ProjectListScreen
import com.openknights.feature.project.projectlist.ProjectListViewModel
import com.openknights.feature.project.di.ViewModelFactory
import com.openknights.feature.user.UsersScreen
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.openknights.feature.auth.AuthViewModelFactory
import com.openknights.feature.auth.AuthViewModel
import com.openknights.feature.auth.LoginScreen
import com.openknights.feature.auth.RegisterScreen
import com.openknights.feature.notice.NoticeScreen
import androidx.compose.foundation.layout.padding
import com.openknights.feature.profile.ProfileEditScreen
import com.openknights.feature.user.UserViewModel
import com.openknights.feature.user.UserViewModelFactory
import com.openknights.data.repository.UserRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.content.Context


// --- Navigation 대상 정의
sealed interface ScreenEntry
data object ContestsScreenEntry : ScreenEntry
data class ProjectsScreenEntry(val term: String) : ScreenEntry
data class ProjectDetailScreenEntry(val projectId: Long) : ScreenEntry
data object UsersScreenEntry : ScreenEntry
data class ProfileEditScreenEntry(val userId: String) : ScreenEntry // 프로필 수정 화면 Entry 추가
data class NoticeScreenEntry(val isLoggedIn: Boolean) : ScreenEntry
data object RegisterScreenEntry : ScreenEntry
data object LoginScreenEntry : ScreenEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenKnightsApp() {
    val backStack = remember { mutableStateListOf<ScreenEntry>(ContestsScreenEntry) }
    val currentEntry = backStack.lastOrNull()
    val latestContestTerm = "2025-2nd" //FakeOpenKnightsData.fakeContests.firstOrNull()?.term ?: ""
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // viewModel()은 **"화면의 상태 창고를 관리하는 비서"**에게 "AuthViewModel 하나 주세요"라고 요청하는 것.
    // 이미 있으면 그냥 가져다 주고, 없으면 ViewModelProvider를 통해 새 인스턴스 생성한 후 창고에 넣고 그걸 주는 겁니다.
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current.applicationContext as Application))
    val projectListViewModel: ProjectListViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))

    val context = LocalContext.current.applicationContext
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userRepository = UserRepositoryImpl(context, firestore, storage)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    Log.d("OpenKnightsApp", "isLoggedIn: $isLoggedIn")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "OpenKnights",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (backStack.size > 1) {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("사용자 등록") },
                                    onClick = {
                                        showMenu = false
                                        backStack.add(RegisterScreenEntry)
                                    }
                                )
                                if (isLoggedIn) {
                                    DropdownMenuItem(
                                        text = { Text("로그아웃") },
                                        onClick = {
                                            showMenu = false
                                            authViewModel.signOut()
                                            backStack.clear()
                                            backStack.add(LoginScreenEntry)
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("로그인") },
                                        onClick = {
                                            showMenu = false
                                            backStack.add(LoginScreenEntry)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("프로젝트 등록") },
                                    onClick = {
                                        showMenu = false
                                        // TODO: 프로젝트 등록 화면으로 이동 로직 추가
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Upload Fake Projects") },
                                    onClick = {
                                        showMenu = false
                                        coroutineScope.launch {
                                            projectListViewModel.projectRepository.uploadFakeProjectsToFirestore()
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { backStack.add(NoticeScreenEntry(isLoggedIn)) }
                    ) {
                        val imageVector = if (isLoggedIn) {
                            Icons.Default.Notifications
                        } else {
                            Icons.Outlined.Notifications
                        }
                        // D. onPrimary 색상을 사용하던 것을 onSurface로 변경 (이전 코드 기준)
                        val tint = if (isLoggedIn) {
                            MaterialTheme.colorScheme.onPrimary // 이전 코드에서는 onSurface 였으나, onPrimary가 TopAppBar와 더 잘 어울립니다.
                        } else {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        }
                        Icon(
                            imageVector = imageVector,
                            contentDescription = "Notifications",
                            tint = tint
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentEntry is ContestsScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(ContestsScreenEntry)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("HOME") }
                )
                NavigationBarItem(
                    selected = currentEntry is ProjectsScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(ProjectsScreenEntry(latestContestTerm))
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("프로젝트") }
                )
                NavigationBarItem(
                    selected = currentEntry is UsersScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(UsersScreenEntry)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("사용자") }
                )
            }
        }
    ) { innerPadding ->
        // E. NavDisplay로 화면 전환 처리
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { entry ->
                when (entry) {
                    is ContestsScreenEntry -> NavEntry(entry) {
                        ContestsScreen(
                            onContestClick = { term ->
                                backStack.add(ProjectsScreenEntry(term))
                            },
                            padding = innerPadding
                        )
                    }
                    is ProjectsScreenEntry -> NavEntry(entry) {
                        ProjectListScreen(
                            contestTerm = entry.term,
                            onProjectClick = { projectId ->
                                backStack.add(ProjectDetailScreenEntry(projectId))},
                            onShowErrorSnackBar = {},
                            padding = innerPadding
                        )
                    }
                    is UsersScreenEntry -> NavEntry(entry) {
                        UsersScreen(
                            padding = innerPadding,
                            onUserClick = { userId ->
                                backStack.add(ProfileEditScreenEntry(userId))
                            }
                        )
                    }
                    is ProjectDetailScreenEntry -> NavEntry(entry) {
                        ProjectDetailScreen(
                            projectId = entry.projectId,
                            onBack = { backStack.removeLastOrNull() },
                            padding = innerPadding // Pass innerPadding
                        )
                    }
                    is ProfileEditScreenEntry -> NavEntry(entry) {
                        ProfileEditScreen(
                            userId = entry.userId,
                            onSaveClick = { name, description, profileImageUrl ->
                                Log.d("ProfileEditScreen ${entry.userId}", "Name: $name, Description: $description, Image URL: $profileImageUrl")
                                userViewModel.updateUserProfile(entry.userId, name, description, profileImageUrl)
                                backStack.removeLastOrNull() // Go back after saving
                            },
                            onBackClick = { backStack.removeLastOrNull() }
                        )
                    }
                    is NoticeScreenEntry -> NavEntry(entry) {
                        val currentUserEmail by authViewModel.currentUserEmail.collectAsState()
                        NoticeScreen(
                            userEmail = currentUserEmail,
                            isLoggedIn = entry.isLoggedIn,
                            onLogoutClick = {
                                authViewModel.signOut()
                                backStack.clear()
                                backStack.add(LoginScreenEntry)
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    is RegisterScreenEntry -> NavEntry(entry) {
                        RegisterScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onRegisterSuccess = {
                                backStack.removeLastOrNull() // 회원가입 화면 제거
                                backStack.add(LoginScreenEntry)
                            }
                        )
                    }
                    is LoginScreenEntry -> NavEntry(entry) {
                        LoginScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onLoginSuccess = {
                                backStack.clear()
                                backStack.add(ContestsScreenEntry)
                            },
                            onNavigateToRegister = { backStack.add(RegisterScreenEntry) }
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OpenKnightsAppPreview() {
    // F. Preview에 KnightsTheme 적용
    KnightsTheme {
        OpenKnightsApp()
    }
}