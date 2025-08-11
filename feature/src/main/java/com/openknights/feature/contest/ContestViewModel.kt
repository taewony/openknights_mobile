package com.openknights.feature.contest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openknights.data.repository.ContestRepository
import com.openknights.model.Contest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ContestUiState {
    object Initial : ContestUiState
    object Loading : ContestUiState
    data class Success(val contests: List<Contest>) : ContestUiState
    data class Error(val message: String) : ContestUiState
}

class ContestViewModel(private val contestRepository: ContestRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Initial)
    val uiState: StateFlow<ContestUiState> = _uiState

    fun loadContests() {
        viewModelScope.launch {
            _uiState.value = ContestUiState.Loading
            try {
                val contests = contestRepository.getContests()
                _uiState.value = ContestUiState.Success(contests)
            } catch (e: Exception) {
                _uiState.value = ContestUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveContests(contests: List<Contest>) {
        viewModelScope.launch {
            try {
                contests.forEach { contest ->
                    contestRepository.saveContest(contest)
                    println("Contest saved successfully: ${contest.id}")
                }
            } catch (e: Exception) {
                println("Error saving contests: ${e.message}")
            }
        }
    }
}

class ContestViewModelFactory(private val contestRepository: ContestRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContestViewModel(contestRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
