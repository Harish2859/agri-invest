package com.example.agri_invest_app.ui.lead

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.network.WithdrawRequest
import com.example.agri_invest_app.data.repository.LeadRepository
import com.example.agri_invest_app.data.repository.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class LeadViewModel(
    private val repository: LeadRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeadDashboardState())
    val state: StateFlow<LeadDashboardState> = _state.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val usersDeferred = async { repository.getPendingUsers() }
            val projectsDeferred = async { repository.getPendingProjects() }
            val milestonesDeferred = async { repository.getPendingMilestones() }
            val profileDeferred = async { repository.getProfile() }
            val transactionsDeferred = async { repository.getTransactionHistory() }

            val usersResult = usersDeferred.await()
            val pendingProjectsResult = projectsDeferred.await()
            val milestonesResult = milestonesDeferred.await()
            val profileResult = profileDeferred.await()
            val transactionsResult = transactionsDeferred.await()

            _state.update { currentState ->
                val errorMsg = when {
                    usersResult.isFailure || pendingProjectsResult.isFailure || milestonesResult.isFailure -> 
                        "Sync failed. Check connection."
                    else -> null
                }
                
                currentState.copy(
                    isLoading = false,
                    walletBalance = profileResult.getOrNull()?.walletBalance ?: currentState.walletBalance,
                    pendingUsers = usersResult.getOrDefault(emptyList()),
                    pendingProjects = pendingProjectsResult.getOrDefault(emptyList()),
                    pendingMilestones = milestonesResult.getOrDefault(emptyList()),
                    transactions = transactionsResult.getOrDefault(emptyList()).sortedByDescending { it.timestamp },
                    error = errorMsg
                )
            }
        }
    }

    fun verifyUser(userId: Long, approve: Boolean, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.verifyUser(userId, approve, reason).onSuccess {
                loadData()
                _message.value = if (approve) "User verified successfully!" else "User rejected."
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Verification failed: ${e.message}") }
                _message.value = "Verification failed: ${e.message}"
            }
        }
    }

    fun approveProject(projectId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.approveProject(projectId).onSuccess {
                loadData()
                _message.value = "Project approved successfully!"
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Project Approval failed: ${e.message}") }
                _message.value = "Project approval failed: ${e.message}"
            }
        }
    }

    fun approveMilestone(milestoneId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.approveMilestone(milestoneId).onSuccess {
                loadData()
                _message.value = "Milestone approved successfully!"
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Milestone approval failed: ${e.message}") }
                _message.value = "Milestone approval failed: ${e.message}"
            }
        }
    }

    fun processWithdrawal(amount: BigDecimal, bankDetails: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val request = WithdrawRequest(amount, bankDetails)
            repository.withdrawCommissions(request)
                .onSuccess {
                    loadData() // Refresh dashboard data including wallet balance
                    _message.value = "Withdrawal processed successfully!"
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Withdrawal failed: ${e.message}") }
                    _message.value = "Withdrawal failed: ${e.message}"
                }
        }
    }

    fun messageShown() {
        _message.update { null }
    }
}
