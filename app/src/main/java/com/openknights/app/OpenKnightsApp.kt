package com.openknights.app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// --- Data Classes (Dummy)
data class Contest(val term: String, val name: String)
data class Project(val id: String, val name: String)

// --- Fake Data (Dummy)
object FakeOpenKnightsData {
    val fakeContests = listOf(
        Contest(term = "2024-1", name = "2024년 1학기 경진대회"),
        Contest(term = "2023-2", name = "2023년 2학기 경진대회")
    )
}

// --- AuthViewModel (Dummy)
class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserEmail = MutableStateFlow<String?>("guest@example.com")
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    fun signOut() {
        _isLoggedIn.value = false
        _currentUserEmail.value = null
    }

    fun signIn() {
        _isLoggedIn.value = true
        _currentUserEmail.value = "user@example.com"
    }
}

// --- Navigation 대상 정의
sealed interface ScreenEntry
data object ContestListScreenEntry : ScreenEntry
data class ProjectListScreenEntry(val term: String) : ScreenEntry
data class ProjectDetailScreenEntry(val projectId: String) : ScreenEntry
data object UserScreenEntry : ScreenEntry
data class NoticeScreenEntry(val isLoggedIn: Boolean) : ScreenEntry
data object RegisterScreenEntry : ScreenEntry
data object LoginScreenEntry : ScreenEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenKnightsApp() {
    val backStack = remember { mutableStateListOf<ScreenEntry>(ContestListScreenEntry) }
    val currentEntry = backStack.lastOrNull()
    val latestContestTerm = FakeOpenKnightsData.fakeContests.firstOrNull()?.term ?: ""
    var showMenu by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = viewModel()
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
                        val tint = if (isLoggedIn) {
                            MaterialTheme.colorScheme.onPrimary
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
                    selected = currentEntry is ContestListScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(ContestListScreenEntry)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("HOME") }
                )
                NavigationBarItem(
                    selected = currentEntry is ProjectListScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(ProjectListScreenEntry(latestContestTerm))
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("프로젝트") }
                )
                NavigationBarItem(
                    selected = currentEntry is UserScreenEntry,
                    onClick = {
                        backStack.clear()
                        backStack.add(UserScreenEntry)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("사용자") }
                )
            }
        }
    ) { innerPadding ->
        // NavDisplay로 화면 전환 처리 (Dummy Implementation)
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val entry = backStack.lastOrNull()) {
                is ContestListScreenEntry -> ContestListScreen(
                    onContestClick = { contest ->
                        backStack.add(ProjectListScreenEntry(contest.term))
                    },
                    padding = innerPadding
                )
                is ProjectListScreenEntry -> ProjectListScreen(
                    contestTerm = entry.term,
                    onProjectClick = { project ->
                        backStack.add(ProjectDetailScreenEntry(project.id))
                    },
                    onShowErrorSnackBar = {},
                    padding = innerPadding
                )
                is UserScreenEntry -> UserScreen(padding = innerPadding)
                is ProjectDetailScreenEntry -> ProjectDetailScreen(
                    projectId = entry.projectId,
                    onBack = { backStack.removeLastOrNull() }
                )
                is NoticeScreenEntry -> {
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
                is RegisterScreenEntry -> RegisterScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onRegisterSuccess = {
                        backStack.removeLast() // Remove RegisterScreen
                        backStack.add(LoginScreenEntry)
                    }
                )
                is LoginScreenEntry -> LoginScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onLoginSuccess = {
                        authViewModel.signIn()
                        backStack.clear()
                        backStack.add(ContestListScreenEntry)
                    },
                    onNavigateToRegister = { backStack.add(RegisterScreenEntry) }
                )
                null -> {
                    // Should not happen
                }
            }
        }
    }
}

// --- Dummy Screen Composable Implementations ---

@Composable
fun DummyScreen(screenName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = screenName, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ContestListScreen(
    onContestClick: (Contest) -> Unit,
    padding: PaddingValues
) {
    DummyScreen("ContestListScreen", Modifier.padding(padding))
}

@Composable
fun ProjectListScreen(
    contestTerm: String,
    onProjectClick: (Project) -> Unit,
    onShowErrorSnackBar: () -> Unit,
    padding: PaddingValues
) {
    DummyScreen("ProjectListScreen (Term: $contestTerm)", Modifier.padding(padding))
}

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit
) {
    DummyScreen("ProjectDetailScreen (ID: $projectId)")
}

@Composable
fun UserScreen(padding: PaddingValues) {
    DummyScreen("UserScreen", Modifier.padding(padding))
}

@Composable
fun NoticeScreen(
    userEmail: String?,
    isLoggedIn: Boolean,
    onLogoutClick: () -> Unit,
    onBack: () -> Unit
) {
    DummyScreen("NoticeScreen (Logged in: $isLoggedIn)")
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    DummyScreen("LoginScreen")
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    DummyScreen("RegisterScreen")
}


@Preview(showBackground = true)
@Composable
fun OpenKnightsAppPreview() {
    MaterialTheme {
        OpenKnightsApp()
    }
}

/*
package com.openknights.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.openknights.feature.contest.ContestScreen
import com.openknights.feature.user.UserScreen

// 하단 네비게이션 아이템을 정의하는 sealed class
sealed class BottomNavItem(val title: String, val icon: ImageVector, val screenRoute: String) {
    object User : BottomNavItem("User", Icons.Filled.Person, "USER")
    object Contest : BottomNavItem("Contest", Icons.Filled.Home, "CONTEST")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenKnightsApp() {
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.User) }

    val navigationItems = listOf(
        BottomNavItem.User,
        BottomNavItem.Contest
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "OpenKnights") })
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = { selectedItem = item },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Scaffold의 content 영역
        // 선택된 아이템에 따라 다른 화면을 보여줍니다.
        when (selectedItem) {
            is BottomNavItem.User -> {
                UserScreen(modifier = Modifier.padding(innerPadding))
            }
            is BottomNavItem.Contest -> {
                ContestScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

 */