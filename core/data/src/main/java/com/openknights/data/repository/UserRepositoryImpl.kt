package com.openknights.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
class UserRepositoryImpl(private val context: Context, private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) : UserRepository {

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

    override suspend fun addUserProfile(user: User) {
        // "users" 컬렉션에 접근하여, 사용자의 고유 ID(uid)를 문서 이름으로 지정하고
        // user 객체 데이터를 저장합니다.
        try {
            firestore.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            // Firestore 저장 실패 시 예외를 던지거나 로그를 남겨서
            // ViewModel에서 에러를 인지할 수 있도록 합니다.
            throw e
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

    /*
    override suspend fun testStorageAccess() {
        val testFilePath = "test_uploads/test_file_${System.currentTimeMillis()}.txt"
        val testContent = "Hello Firebase Storage Test!"
        val storageRef = storage.reference.child(testFilePath)

        try {
            // Upload test file
            storageRef.putBytes(testContent.toByteArray()).await()
            Log.d(TAG, "Storage Test: File uploaded successfully to $testFilePath")

            // Download test file
            val bytes = storageRef.getBytes(1024 * 1024).await() // Max 1MB
            val downloadedContent = String(bytes)
            Log.d(TAG, "Storage Test: File downloaded successfully. Content: $downloadedContent")

            if (downloadedContent == testContent) {
                Log.d(TAG, "Storage Test: Upload and download content match. Spark Plan access confirmed!")
            } else {
                Log.e(TAG, "Storage Test: Downloaded content mismatch.")
            }

            // Clean up: Delete the test file
            storageRef.delete().await()
            Log.d(TAG, "Storage Test: Test file deleted successfully.")

        } catch (e: Exception) {
            Log.e(TAG, "Storage Test: Failed to access Firebase Storage.", e)
        }
    }
    */

    override suspend fun updateUserProfile(userId: String, name: String, description: String, profileImageUrl: String) {
        try {
            val userRef = firestore.collection("users").document(userId)
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "introduction" to description,
                "imageUrl" to profileImageUrl
            )
            userRef.update(updates).await()
            Log.d(TAG, "User profile for $userId updated successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile for $userId: ${e.message}", e)
            throw e
        }
    }
}