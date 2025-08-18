package com.openknights.feature.project.projectlist.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.openknights.model.Project
import com.openknights.model.Phase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
sealed interface ProjectUiState {

    @Immutable
    data object Loading : ProjectUiState

    @Immutable
    data class Projects(
        val groups: ImmutableList<Group> = persistentListOf(),
    ) : ProjectUiState {
        @Immutable
        data class Group(
            val projectPhase: Phase,
            val projects: ImmutableList<Project>,
        )
    }
}