package com.openknights.data.repository

import android.content.Context
import com.openknights.data.R
import com.openknights.model.Contest
import com.openknights.model.Phase
import kotlinx.serialization.json.Json
import java.io.InputStream

interface ContestRepository {
    suspend fun getContests(): List<Contest>
}

class ContestRepositoryImpl(private val context: Context) : ContestRepository {
    override suspend fun getContests(): List<Contest> {
        // 1. res/raw에서 JSON 파일을 InputStream으로 엽니다.
        val inputStream: InputStream = context.resources.openRawResource(R.raw.fake_contest)

        // 2. InputStream에서 텍스트를 읽습니다.
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        // 3. JSON 텍스트를 List<Contest> 객체로 파싱합니다.
        return Json.decodeFromString<List<Contest>>(jsonString)

        /* 기존 Fake 데이터 주석 처리
        return listOf(
            Contest(
                id = 1,
                term = "2024-2",
                description = "2024년 2학기 교내 알고리즘 경진대회",
                staff = listOf("김교수", "이조교"),
                phase = Phase.REGISTERED
            ),
            Contest(
                id = 2,
                term = "2024-1",
                description = "2024년 1학기 교내 앱 개발 경진대회",
                staff = listOf("박교수"),
                phase = Phase.AWARDED_GRAND
            ),
            Contest(
                id = 3,
                term = "2023-2",
                description = "2023년 2학기 교내 AI 경진대회",
                staff = listOf("최교수", "강조교"),
                phase = Phase.FINISHED
            )
        )
        */
    }
}
