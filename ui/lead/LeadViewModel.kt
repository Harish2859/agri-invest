package com.example.agri_invest_app.ui.lead

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeadViewModel(private val repository: LeadRepository) : ViewModel() {

    private val _state = MutableStateFlow(LeadDashboardState())
    val state: StateFlow<LeadDashboardState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Fetch both concurrently
            val usersResult = repository.getPendingUsers()
            val projectsResult = repository.getPendingProjects()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    pendingUsers = usersResult.getOrDefault(emptyList()),
                    pendingProjects = projectsResult.getOrDefault(emptyList()),
                    // Only show error if BOTH failed, otherwise handle individual lists
                    error = if (usersResult.isFailure && projectsResult.isFailure) {
                        "Failed to connect to governance services."
                    } else if (usersResult.isFailure) {
                        "Partial error: Users queue offline."
                    } else if (projectsResult.isFailure) {
                        "Partial error: Projects queue offline."
                    } else null
                )
            }
        }
    }

    fun verifyUser(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.verifyUser(userId).onSuccess {
                loadData()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Verification failed: ${e.message}") }
            }
        }
    }

    fun approveProject(projectId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.approveProject(projectId).onSuccess {
                loadData()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Approval failed: ${e.message}") }
            }
        }
    }
}
