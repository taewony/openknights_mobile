package com.openknights.data

import com.openknights.model.Announcement
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    fun getAnnouncements(userId: String): Flow<List<Announcement>>
    suspend fun markAsRead(userId: String, announcementId: String)
    fun getUnreadCount(userId: String): Flow<Int>
}