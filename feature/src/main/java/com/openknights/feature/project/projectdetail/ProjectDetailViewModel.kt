package com.openknights.feature.project.projectdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openknights.feature.project.data.repository.ProjectRepository
import com.openknights.feature.project.projectdetail.model.ProjectDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ProjectDetailViewModel(private val projectRepository: ProjectRepository) : ViewModel() {

    private val _projectUiState =
        MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val projectUiState = _projectUiState.asStateFlow()

    fun fetchProject(projectId: Long) {
        viewModelScope.launch {
            val project = projectRepository.getProject(projectId)
            if (project != null) {
                _projectUiState.value = ProjectDetailUiState.Success(project)
            } else {
                _projectUiState.value = ProjectDetailUiState.Error("Project not found")
            }
        }
    }
}