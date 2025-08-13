package com.openknights.feature.project.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.openknights.model.Project
import kotlinx.coroutines.tasks.await

// TODO: Hilt를 사용하여 FirebaseFirestore 주입 받도록 변경
class ProjectRemoteDataSource(private val db: FirebaseFirestore) {

    suspend fun getProjects(contestTerm: String): List<Project> {
        return db.collection("projects")
            .whereEqualTo("term", contestTerm)
            .get()
            .await()
            .toObjects(Project::class.java)
    }

    suspend fun getProject(projectId: Long): Project? {
        return db.collection("projects")
            .document(projectId.toString())
            .get()
            .await()
            .toObject(Project::class.java)
    }
}
