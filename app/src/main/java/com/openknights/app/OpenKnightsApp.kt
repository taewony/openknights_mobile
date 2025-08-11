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