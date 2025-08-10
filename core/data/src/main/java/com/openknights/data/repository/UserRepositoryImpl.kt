package com.openknights.data.repository

import android.content.Context
import android.util.Log
import com.openknights.data.R
import com.openknights.model.User
import kotlinx.serialization.json.Json
import java.io.InputStream

private const val TAG = "UserRepository"

/**
 * res/raw 디렉토리의 JSON 파일을 데이터 소스로 사용하는 UserRepository 구현체
 *
 * @property context 리소스에 접근하기 위한 Application Context
 */
class UserRepositoryImpl(private val context: Context) : UserRepository {

    // JSON 파일을 읽어 파싱한 사용자 목록을 저장하는 변수
    // lazy를 사용해 처음 접근할 때 한 번만 파일을 읽도록 구현
    private val _users: List<User> by lazy {
        Log.d(TAG, "Initializing and parsing user data from JSON...")
        try {
            // 1. res/raw/fake_users.json 파일에 대한 InputStream 열기
            val inputStream: InputStream = context.resources.openRawResource(R.raw.fake_users)

            // 2. InputStream에서 텍스트 읽기
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // 3. JSON 문자열을 List<User> 객체로 파싱
            val parsedUsers = Json.decodeFromString<List<User>>(jsonString)
            Log.d(TAG, "JSON parsing successful. Parsed ${parsedUsers.size} users.")
            parsedUsers
        } catch (e: Exception) {
            // 파일 읽기나 파싱에 실패하면 빈 리스트 반환
            Log.e(TAG, "JSON parsing failed.", e)
            emptyList()
        }
    }

    override fun getUsers(): List<User> {
        return _users
    }

    override fun getUserByStudentId(studentId: String): User? {
        Log.d(TAG, "Searching for user with studentId: $studentId")
        val user = _users.find { it.studentId == studentId }
        if (user == null) {
            Log.w(TAG, "User with studentId: $studentId not found.")
        } else {
            Log.d(TAG, "User found: ${user.name}")
        }
        return user
    }

    override fun getUserByName(name: String): User? {
        return _users.find { it.name == name }
    }
}
