package com.openknights.feature.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openknights.data.AnnouncementRepository
import com.openknights.model.Announcement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AnnouncementViewModel(
    private val announcementRepository: AnnouncementRepository,
    private val currentUserId: String // In a real app, this would come from an auth manager
) : ViewModel() {

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        // Collect announcements
        announcementRepository.getAnnouncements(currentUserId)
            .onEach { _announcements.value = it }
            .launchIn(viewModelScope)

        // Collect unread count
        announcementRepository.getUnreadCount(currentUserId)
            .onEach { _unreadCount.value = it }
            .launchIn(viewModelScope)
    }

    fun markAnnouncementAsRead(announcementId: String) {
        viewModelScope.launch {
            announcementRepository.markAsRead(currentUserId, announcementId)
        }
    }
}