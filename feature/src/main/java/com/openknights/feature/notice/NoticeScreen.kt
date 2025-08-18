package com.openknights.feature.notice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openknights.model.Announcement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.remember
import com.openknights.data.FirebaseAnnouncementRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(
    currentUserId: String,
    padding: PaddingValues,
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    val factory = remember { AnnouncementViewModelFactory(currentUserId, database) }
    val viewModel: AnnouncementViewModel = viewModel(factory = factory)
    val announcements by viewModel.announcements.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 첫 번째 item: 제목
        item {
            Text(
                text = "공지사항 (${unreadCount}개)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 공지사항이 없는 경우
        if (announcements.isEmpty()) {
            item {
                Text(
                    text = "알림이 없습니다.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // 공지사항 리스트
            items(announcements) { announcement ->
                AnnouncementItem(announcement = announcement) {
                    viewModel.markAnnouncementAsRead(announcement.id)
                    // Navigate to detail screen or show dialog
                }
            }
        }
    }
}
@Composable
fun AnnouncementItem(announcement: Announcement, onClick: (Announcement) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(announcement) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "게시일: ${formatTimestamp(announcement.publishAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (announcement.id.isNotEmpty() && announcement.id.startsWith("G")) { // Simple check for global notice
                    Text(
                        text = "전체 공지",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "개인 공지",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

class AnnouncementViewModelFactory(
    private val userId: String,
    private val database: FirebaseDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnnouncementViewModel(
                announcementRepository = FirebaseAnnouncementRepository(database),
                currentUserId = userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}