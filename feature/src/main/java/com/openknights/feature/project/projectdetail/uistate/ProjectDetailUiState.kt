package com.openknights.feature.project.projectdetail.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.openknights.model.Project

@Stable
sealed interface ProjectDetailUiState {

    @Immutable
    data object Loading : ProjectDetailUiState

    @Immutable
    data class Success(val project: Project) : ProjectDetailUiState
    data class Error(val message: String) : ProjectDetailUiState
}