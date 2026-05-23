package com.example.agri_invest_app.ui.investor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.*
import com.example.agri_invest_app.data.repository.AuthRepository
import com.example.agri_invest_app.data.repository.InvestmentRepository
import com.example.agri_invest_app.data.repository.ProjectRepository
import com.example.agri_invest_app.data.repository.WalletRepository
import com.example.agri_invest_app.util.DataStoreManager
import com.example.agri_invest_app.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal

class InvestorViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val investmentRepository: InvestmentRepository,
    private val walletRepository: WalletRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(InvestorDashboardState())
    val state: StateFlow<InvestorDashboardState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        fetchUserProfile()
        discoverProjects()
        getInvestorPortfolio()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val result = authRepository.getProfile()
            if (result is Resource.Success) {
                val newUser = result.data
                _state.update { currentState ->
                    val currentBalance = currentState.user?.walletBalance ?: BigDecimal.ZERO
                    val newReceivedBalance = newUser.walletBalance ?: BigDecimal.ZERO
                    
                    var updatedUser = if (newReceivedBalance.signum() == 0 && currentBalance.signum() > 0) {
                        newUser.copy(walletBalance = currentBalance)
                    } else newUser

                    // LEGACY FIX: If user is an INVESTOR and status is SUBMITTED, auto-map to APPROVED
                    // This handles users who submitted before the backend auto-approval fix.
                    if (updatedUser.role?.uppercase() == "INVESTOR" && updatedUser.kycStatus == KycStatus.SUBMITTED) {
                        updatedUser = updatedUser.copy(kycStatus = KycStatus.APPROVED, verified = true)
                        // Sync local storage if we found a legacy "Submitted" investor
                        viewModelScope.launch { dataStoreManager.updateVerifiedStatus(true) }
                    }

                    // KYC GUARD: Maintain local APPROVED status during sync if we've already auto-approved
                    if (currentState.user?.kycStatus == KycStatus.APPROVED && 
                        updatedUser.kycStatus != KycStatus.APPROVED) {
                        updatedUser = updatedUser.copy(kycStatus = KycStatus.APPROVED)
                    }

                    currentState.copy(user = updatedUser)
                }
            }
        }
    }

    fun discoverProjects() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = projectRepository.discoverProjects()
            result.onSuccess { projects ->
                _state.update { it.copy(isLoading = false, projects = projects) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getProjectDetails(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = projectRepository.getProjectDetails(id)
            result.onSuccess { project ->
                _state.update { it.copy(isLoading = false, selectedProject = project) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getInvestorPortfolio() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val portfolioDeferred = async { projectRepository.getInvestorPortfolio() }
            val historyDeferred = async { investmentRepository.getInvestmentHistory() }
            val discoveryDeferred = async { projectRepository.discoverProjects() }

            val portfolioResult = portfolioDeferred.await()
            val historyResult = historyDeferred.await()
            val allProjects = discoveryDeferred.await().getOrDefault(emptyList())

            portfolioResult.onSuccess { portfolio ->
                historyResult.onSuccess { history ->
                    val resolvedHistory = history.map { investment ->
                        if (investment.projectTitle.isNullOrBlank()) {
                            val project = allProjects.find { it.projectId == investment.projectId }
                            investment.copy(projectTitle = project?.title ?: "Project #${investment.projectId}")
                        } else investment
                    }

                    _state.update { it.copy(
                        isLoading = false,
                        portfolio = portfolio.copy(investments = resolvedHistory)
                    ) }
                    
                    // SYNC CHECK: Guard against transient 0 balance during sync
                    portfolio.summary?.let { summary ->
                        _state.update { currentState ->
                            val currentBalance = currentState.user?.walletBalance ?: BigDecimal.ZERO
                            val incomingBalance = summary.walletBalance
                            
                            val updatedBalance = if (incomingBalance.signum() == 0 && currentBalance.signum() > 0) {
                                currentBalance
                            } else incomingBalance
                            
                            currentState.copy(
                                user = currentState.user?.copy(walletBalance = updatedBalance)
                            )
                        }
                    }
                }.onFailure {
                    _state.update { it.copy(isLoading = false, portfolio = portfolio) }
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun submitInvestorKyc(documentUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.submitKyc(documentUrl)
            if (result is Resource.Success) {
                val response = result.data
                val isVerified = response.verified ?: true
                
                // Update Local DataStore to persist verification state
                dataStoreManager.updateVerifiedStatus(isVerified)
                
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        user = currentState.user?.copy(
                            kycStatus = response.kycStatus ?: KycStatus.APPROVED,
                            verified = isVerified
                        ),
                        successMessage = response.message
                    )
                }
                // Refresh to ensure full alignment, but UI is already updated
                fetchUserProfile()
            } else if (result is Resource.Error) {
                _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun processInvestment(projectId: Long, amount: BigDecimal) {
        val currentUser = _state.value.user
        val balance = currentUser?.walletBalance ?: BigDecimal.ZERO
        
        if (currentUser?.kycStatus != KycStatus.APPROVED) {
            _state.update { it.copy(error = "KYC approval required.") }
            return
        }

        if (balance < amount) {
            _state.update { it.copy(error = "Insufficient funds.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            // OPTIMISTIC UPDATE: Deduct balance locally
            val optimisticBalance = balance.subtract(amount)
            _state.update { it.copy(user = it.user?.copy(walletBalance = optimisticBalance)) }

            val initiationResult = investmentRepository.initiateInvestment(
                InvestmentRequest(
                    project = ProjectIdWrapper(id = projectId),
                    amountInvested = amount
                )
            )
            
            initiationResult.onSuccess { response ->
                val completionResult = investmentRepository.completeInvestment(
                    investmentId = response.investmentId,
                    idempotencyKey = "TXN-${System.currentTimeMillis()}"
                )
                
                completionResult.onSuccess {
                    delay(1000)
                    fetchUserProfile()
                    getInvestorPortfolio()
                    _state.update { it.copy(isLoading = false, successMessage = "Invested ₹$amount successfully!") }
                }.onFailure { e ->
                    // ROLLBACK: Restore balance
                    _state.update { it.copy(isLoading = false, user = it.user?.copy(walletBalance = balance), error = "Payment failed: ${e.message}") }
                }
            }.onFailure { e ->
                // ROLLBACK
                _state.update { it.copy(isLoading = false, user = it.user?.copy(walletBalance = balance), error = "Initiation failed: ${e.message}") }
            }
        }
    }

    fun depositFunds(amount: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = walletRepository.depositFunds(amount)
            when (result) {
                is Resource.Success -> {
                    val newBalance = result.data.newBalance
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false, 
                            user = currentState.user?.copy(walletBalance = newBalance),
                            successMessage = "₹${String.format("%.2f", amount)} added to your wallet!"
                        )
                    }
                    // Give the backend a moment to settle before refreshing portfolio
                    delay(1000)
                    getInvestorPortfolio()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> { }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearSuccessMessage() { _state.update { it.copy(successMessage = null) } }
}
