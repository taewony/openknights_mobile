package com.openknights.feature.project.projectlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openknights.feature.project.data.repository.ProjectRepository
import com.openknights.feature.project.projectlist.model.ProjectUiState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// TODO: Hilt를 사용하여 Repository 주입 받도록 변경
class ProjectListViewModel(val projectRepository: ProjectRepository) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchProjects(contestTerm: String) {
        flow {
            val projects = projectRepository.getProjects(contestTerm)
                .groupBy { it.phase }
                .map { (phase, projects) ->
                    ProjectUiState.Projects.Group(phase!!, projects.toPersistentList())
                }
                .toPersistentList()
            emit(ProjectUiState.Projects(projects))
        }
            .catch { throwable ->
                _errorFlow.emit(throwable)
            }
            .onEach { projectUiState ->
                _uiState.value = projectUiState
            }
            .launchIn(viewModelScope)
    }
}