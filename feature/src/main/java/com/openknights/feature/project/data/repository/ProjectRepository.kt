package com.openknights.feature.project.data.repository

import android.util.Log
import com.openknights.feature.project.data.datasource.ProjectLocalDataSource
import com.openknights.feature.project.data.datasource.ProjectRemoteDataSource
import com.openknights.model.Project
import kotlinx.coroutines.tasks.await

// TODO: Hilt를 사용하여 DataSource 주입 받도록 변경
class ProjectRepository(
    private val remoteDataSource: ProjectRemoteDataSource,
    private val localDataSource: ProjectLocalDataSource
) {

    suspend fun getProjects(contestTerm: String): List<Project> {
        return try {
            val remoteProjects = remoteDataSource.getProjects(contestTerm)
            remoteProjects
        } catch (e: Exception) {
            Log.e("ProjectRepository", "DataSource: Error fetching from Firestore for term: $contestTerm.", e)
            emptyList()
        }
    }

    private fun getProjectsFromLocal(contestTerm: String): List<Project> {
        val localProjects = localDataSource.getFakeProjects().filter { it.term == contestTerm }
        Log.d("ProjectRepository", "DataSource: Fetched ${localProjects.size} projects from local fake JSON for term: $contestTerm.")
        return localProjects
    }

    suspend fun getProject(projectId: Long): Project? {
        return try {
            remoteDataSource.getProject(projectId)
                ?: localDataSource.getFakeProjects().find { it.id == projectId }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error fetching project $projectId from Firestore. Falling back to fake data.", e)
            localDataSource.getFakeProjects().find { it.id == projectId }
        }
    }

    // New function to upload fake projects to Firestore
    suspend fun uploadFakeProjectsToFirestore() {
        val fakeProjects = localDataSource.getFakeProjects()
        val projectsCollection = remoteDataSource.db.collection("projects") // Use remoteDataSource.db

        try {
            fakeProjects.forEach { project ->
                projectsCollection.document(project.id.toString()).set(project).await()
            }
            Log.d("ProjectRepository", "Successfully uploaded ${fakeProjects.size} fake projects to Firestore.")
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error uploading fake projects to Firestore.", e)
        }
    }
}
