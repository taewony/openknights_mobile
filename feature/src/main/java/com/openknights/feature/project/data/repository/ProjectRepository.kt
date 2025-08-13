package com.openknights.feature.project.data.repository

import android.util.Log
import com.openknights.feature.project.data.datasource.ProjectLocalDataSource
import com.openknights.feature.project.data.datasource.ProjectRemoteDataSource
import com.openknights.model.Project

// TODO: Hilt를 사용하여 DataSource 주입 받도록 변경
class ProjectRepository(
    private val remoteDataSource: ProjectRemoteDataSource,
    private val localDataSource: ProjectLocalDataSource
) {

    suspend fun getProjects(contestTerm: String): List<Project> {
        return try {
            val remoteProjects = remoteDataSource.getProjects(contestTerm)
            if (remoteProjects.isNotEmpty()) {
                Log.d("ProjectRepository", "Fetched projects from Firestore for term: $contestTerm")
                remoteProjects
            } else {
                Log.d("ProjectRepository", "Firestore is empty for term: $contestTerm. Falling back to fake JSON.")
                getProjectsFromLocal(contestTerm)
            }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error fetching from Firestore for term: $contestTerm. Falling back to fake data.", e)
            getProjectsFromLocal(contestTerm)
        }
    }

    private fun getProjectsFromLocal(contestTerm: String): List<Project> {
        return localDataSource.getFakeProjects().filter { it.term == contestTerm }
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
}
