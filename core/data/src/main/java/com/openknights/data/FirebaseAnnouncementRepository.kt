package com.openknights.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.openknights.model.Announcement
import com.openknights.model.FeedItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
// import timber.log.Timber // Assuming Timber for logging, or replace with standard Android Log
import android.util.Log // Using standard Android Log

class FirebaseAnnouncementRepository(
    private val database: FirebaseDatabase
) : AnnouncementRepository {

    private val announcementsRef = database.getReference("announcements")
    private val feedsGlobalRef = database.getReference("feeds/global")
    private val feedsUsersRef = database.getReference("feeds/users")
    private val userStateRef = database.getReference("user_state")

    override fun getAnnouncements(userId: String): Flow<List<Announcement>> = combine(
        getGlobalFeedAnnouncements(),
        getUserFeedAnnouncements(userId)
    ) { globalAnnouncements, userAnnouncements ->
        (globalAnnouncements + userAnnouncements)
            .distinctBy { it.id } // Ensure unique announcements
            .sortedByDescending { it.publishAt } // Sort by publishAt
    }

    private fun getGlobalFeedAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcementIds = snapshot.children.mapNotNull { it.key }
                fetchAnnouncementsByIds(announcementIds) { announcements ->
                    trySend(announcements)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Failed to load global feed announcements", error.toException())
                close(error.toException())
            }
        }
        feedsGlobalRef.addValueEventListener(listener)
        awaitClose { feedsGlobalRef.removeEventListener(listener) }
    }

    private fun getUserFeedAnnouncements(userId: String): Flow<List<Announcement>> = callbackFlow {
        val userFeedRef = feedsUsersRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcementIds = snapshot.children.mapNotNull { it.key }
                fetchAnnouncementsByIds(announcementIds) { announcements ->
                    trySend(announcements)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Failed to load user feed announcements for $userId", error.toException())
                close(error.toException())
            }
        }
        userFeedRef.addValueEventListener(listener)
        awaitClose { userFeedRef.removeEventListener(listener) }
    }

    private fun fetchAnnouncementsByIds(ids: List<String>, onComplete: (List<Announcement>) -> Unit) {
        if (ids.isEmpty()) {
            onComplete(emptyList())
            return
        }
        val announcements = mutableListOf<Announcement>()
        var completedFetches = 0

        ids.forEach { id ->
            announcementsRef.child(id).get().addOnSuccessListener { snapshot ->
                snapshot.getValue(Announcement::class.java)?.let {
                    announcements.add(it.copy(id = snapshot.key ?: ""))
                }
                completedFetches++
                if (completedFetches == ids.size) {
                    onComplete(announcements)
                }
            }.addOnFailureListener {
                Log.e("FirebaseRepo", "Failed to fetch announcement with id: $id", it)
                completedFetches++
                if (completedFetches == ids.size) {
                    onComplete(announcements)
                }
            }
        }
    }

    override suspend fun markAsRead(userId: String, announcementId: String) {
        val updates = mapOf(
            "read/$announcementId" to ServerValue.TIMESTAMP,
            "unreadCount" to ServerValue.increment(-1.0)
        )
        userStateRef.child(userId).updateChildren(updates).await()
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val unreadCountRef = userStateRef.child(userId).child("unreadCount")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.getValue(Int::class.java) ?: 0
                Log.d("FirebaseRepo", "Unread count received for ${userId}: $count")
                trySend(count)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Failed to load unread count for $userId", error.toException())
                close(error.toException())
            }
        }
        unreadCountRef.addValueEventListener(listener)
        awaitClose { unreadCountRef.removeEventListener(listener) }
    }
}