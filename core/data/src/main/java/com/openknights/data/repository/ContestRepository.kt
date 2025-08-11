package com.openknights.data.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.openknights.data.R
import com.openknights.model.Contest
import com.openknights.model.Phase
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStream

interface ContestRepository {
    suspend fun getContests(): List<Contest>
    suspend fun saveContest(contest: Contest)
}

class ContestRepositoryImpl(private val firestore: FirebaseFirestore) : ContestRepository {
    override suspend fun getContests(): List<Contest> {
        /*
        // 1. res/raw에서 JSON 파일을 InputStream으로 엽니다.
        val inputStream: InputStream = context.resources.openRawResource(R.raw.fake_contest)

        // 2. InputStream에서 텍스트를 읽습니다.
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        // 3. JSON 텍스트를 List<Contest> 객체로 파싱합니다.
        return Json.decodeFromString<List<Contest>>(jsonString)
        */

        return firestore.collection("contests").get().await().toObjects(Contest::class.java)
    }

    override suspend fun saveContest(contest: Contest) {
        Firebase.firestore.collection("contests").document(contest.id.toString()).set(contest).await()
    }
}