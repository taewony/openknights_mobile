package com.openknights.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.openknights.data.R
import com.openknights.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStream

private const val TAG = "UserRepository"

/**
 * res/raw 디렉토리의 JSON 파일을 데이터 소스로 사용하는 UserRepository 구현체
 *
 * @property context 리소스에 접근하기 위한 Application Context
 */
class UserRepositoryImpl(private val context: Context, private val firestore: FirebaseFirestore) : UserRepository {

    /*
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
    */

    override suspend fun getUsers(): List<User> {
        // return _users
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.toObjects(User::class.java)
            Log.d(TAG, "Successfully fetched ${users.size} users from Firestore.")
            users
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users from Firestore.", e)
            emptyList()
        }
    }

    override suspend fun getUserByStudentId(studentId: String): User? {
        // return _users.find { it.studentId == studentId }
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            val user = snapshot.toObjects(User::class.java).firstOrNull()
            if (user != null) {
                Log.d(TAG, "User found with studentId: $studentId")
            } else {
                Log.w(TAG, "User with studentId: $studentId not found.")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by studentId from Firestore.", e)
            null
        }
    }

    override suspend fun getUserByName(name: String): User? {
        // return _users.find { it.name == name }
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("name", name)
                .get()
                .await()
            val user = snapshot.toObjects(User::class.java).firstOrNull()
            if (user != null) {
                Log.d(TAG, "User found with name: $name")
            } else {
                Log.w(TAG, "User with name: $name not found.\n")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by name from Firestore.", e)
            null
        }
    }
}
