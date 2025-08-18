package com.openknights.data.repository

import com.openknights.model.User

/**
 * 사용자 데이터 처리를 위한 Repository 인터페이스
 */
interface UserRepository {

    /**
     * 모든 사용자 목록을 가져옵니다.
     * @return User의 리스트
     */
    // fun getUsers(): List<User>
    suspend fun getUsers(): List<User>

    /**
     * 학번(studentId)으로 특정 사용자를 찾습니다.
     * @param studentId 찾고자 하는 사용자의 학번
     * @return 찾은 User 객체, 없으면 null
     */
    // fun getUserByStudentId(studentId: String): User?
    suspend fun getUserByStudentId(studentId: String): User?

    /**
     * 이름(name)으로 특정 사용자를 찾습니다.
     */
    // fun getUserByName(name: String): User?
    suspend fun getUserByName(name: String): User?

    suspend fun addUserProfile(user: User)

    /**
     * 사용자 프로필 정보를 업데이트합니다.
     * @param userId 업데이트할 사용자의 ID
     * @param name 업데이트할 이름
     * @param description 업데이트할 소개
     * @param profileImageUrl 업데이트할 프로필 이미지 URL
     */
    suspend fun updateUserProfile(userId: String, name: String, description: String, profileImageUrl: String)
}