package com.openknights.data.repository

import android.content.Context
import com.openknights.data.R // res/raw/users.json에 접근하기 위해 R 클래스 임포트
import com.openknights.model.User
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.collections.find

/**
 * res/raw 디렉토리의 JSON 파일을 데이터 소스로 사용하는 UserRepository 구현체
 *
 * @property context 리소스에 접근하기 위한 Application Context
 */
class UserRepositoryImpl(private val context: Context) : UserRepository {

    // JSON 파일을 읽어 파싱한 사용자 목록을 저장하는 변수
    // lazy를 사용해 처음 접근할 때 한 번만 파일을 읽도록 구현
    private val users: List<User> by lazy {
        try {
            // 1. res/raw/users.json 파일에 대한 InputStream 열기
            val inputStream: InputStream = context.resources.openRawResource(R.raw.users)

            // 2. InputStream에서 텍스트 읽기
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // 3. JSON 문자열을 List<User> 객체로 파싱
            Json.decodeFromString<List<User>>(jsonString)
        } catch (e: Exception) {
            // 파일 읽기나 파싱에 실패하면 빈 리스트 반환
            e.printStackTrace()
            emptyList()
        }
    }

    override fun getUsers(): List<User> {
        return users
    }

    override fun getUserByStudentId(studentId: String): User? {
        return users.find { it.studentId == studentId }
    }

    override fun getUserByName(name: String): User? {
        return users.find { it.name == name }
    }
}
