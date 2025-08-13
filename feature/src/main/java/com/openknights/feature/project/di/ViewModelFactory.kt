package com.openknights.feature.project.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.openknights.feature.project.data.datasource.ProjectLocalDataSource
import com.openknights.feature.project.data.datasource.ProjectRemoteDataSource
import com.openknights.feature.project.data.repository.ProjectRepository
import com.openknights.feature.project.projectdetail.ProjectDetailViewModel
import com.openknights.feature.project.projectlist.ProjectListViewModel

/**
 * ViewModel에 의존성을 수동으로 주입하기 위한 Factory 클래스입니다.
 * TODO: Hilt를 사용하여 자동화된 의존성 주입으로 변경해야 합니다.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val repository: ProjectRepository by lazy {
        val remoteDataSource = ProjectRemoteDataSource(FirebaseFirestore.getInstance())
        val localDataSource = ProjectLocalDataSource(context.applicationContext)
        ProjectRepository(remoteDataSource, localDataSource)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProjectListViewModel::class.java) -> {
                ProjectListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProjectDetailViewModel::class.java) -> {
                ProjectDetailViewModel(repository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
