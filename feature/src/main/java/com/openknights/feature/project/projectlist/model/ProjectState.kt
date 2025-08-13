package com.openknights.feature.project.projectlist.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.openknights.model.Project
import com.openknights.model.Phase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

@Immutable
data class ProjectGroup(
    val phase: Phase,
    val projects: PersistentList<Project>,
)

@Stable
class ProjectState(
    private val projects: ImmutableList<Project>,
    val listState: LazyListState,
    selectedProjectPhase: Phase? = projects.map { it.phase }.firstOrNull(),
) {

    val groups: List<ProjectGroup> = projects
        .groupBy { it.phase }
        .filterKeys { it != null } // Filter out null phases
        .map { (projectPhase, projects) -> ProjectGroup(projectPhase!!, projects.toPersistentList()) }

    val phases: List<Phase> = projects.mapNotNull { it.phase }.distinct()

    private val projectPhasePositions: Map<Phase, Int> = buildMap {
        var position = 0
        groups.forEach { group ->
            put(group.phase, position)
            position += group.projects.size
        }
    }

    var selectedProjectPhase: Phase? by mutableStateOf(selectedProjectPhase)
        private set

    val isAtTop by derivedStateOf {
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }

    fun groupIndex(index: Int): Phase? {
        for ((projectPhase, position) in projectPhasePositions) {
            if (position == index) {
                return projectPhase
            }
        }
        return null
    }

    fun select(projectPhase: Phase) {
        selectedProjectPhase = projectPhase
    }

    suspend fun scrollTo(projectPhase: Phase) {
        val index = projectPhasePositions[projectPhase] ?: return
        listState.animateScrollToItem(index)
    }

    fun findProjectIndex(projectId: String): Int {
        return groups.asSequence()
            .flatMap { it.projects }
            .indexOfFirst { it.id == projectId.toLongOrNull() }
    }

    suspend fun scrollToProject(projectId: String, offsetPx: Int = 0) {
        val index = findProjectIndex(projectId)
        if (index != PROJECT_NOT_FOUND) {
            listState.animateScrollToItem(
                index = index,
                scrollOffset = offsetPx
            )
        }
    }

    companion object {

        private const val PROJECT_NOT_FOUND = -1

        fun Saver(
            projects: ImmutableList<Project>,
            listState: LazyListState,
        ): Saver<ProjectState, *> = Saver(
            save = { it.selectedProjectPhase?.name },
            restore = { selectedProjectPhaseName ->
                val selectedProjectPhase = selectedProjectPhaseName?.let { Phase.valueOf(it) }
                ProjectState(
                    projects = projects,
                    listState = listState,
                    selectedProjectPhase = selectedProjectPhase,
                )
            }
        )
    }
}

@Composable
internal fun rememberProjectState(
    projects: ImmutableList<Project>,
    listState: LazyListState = rememberLazyListState(),
): ProjectState {
    val state = rememberSaveable(
        projects,
        listState,
        saver = ProjectState.Saver(projects, listState),
    ) {
        ProjectState(projects, listState)
    }
    LaunchedEffect(projects, listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .mapNotNull { index -> state.groupIndex(index) }
            .distinctUntilChanged()
            .collect { projectPhase -> state.select(projectPhase) }
    }
    return state
}