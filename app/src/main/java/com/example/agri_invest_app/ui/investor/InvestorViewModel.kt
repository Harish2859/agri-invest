package com.example.agri_invest_app.ui.investor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.*
import com.example.agri_invest_app.data.repository.InvestmentRepository
import com.example.agri_invest_app.data.repository.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal

class InvestorViewModel(
    private val projectRepository: ProjectRepository,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvestorDashboardState())
    val state: StateFlow<InvestorDashboardState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        discoverProjects()
        getInvestorPortfolio()
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
                    // RESOLVE NAMES: Cross-reference ProjectDiscovery to fix "Unknown Project" titles
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
                }.onFailure {
                    _state.update { it.copy(isLoading = false, portfolio = portfolio) }
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun processInvestment(projectId: Long, amount: BigDecimal) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val initiationResult = investmentRepository.initiateInvestment(
                InvestmentRequest(
                    project = ProjectIdWrapper(id = projectId),
                    amountInvested = amount
                )
            )
            
            initiationResult.onSuccess { response ->
                val completionResult = investmentRepository.completeInvestment(
                    investmentId = response.investmentId,
                    txnId = "TXN-${System.currentTimeMillis()}"
                )
                
                completionResult.onSuccess {
                    delay(1500)
                    getProjectDetails(projectId)
                    getInvestorPortfolio()
                    discoverProjects()
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Payment failed: ${e.message}") }
                }
            }.onFailure { e ->
                val errorMsg = try {
                    val errorBody = e.message?.let { JSONObject(it).getString("message") }
                    errorBody ?: e.message
                } catch (ex: Exception) {
                    e.message
                }
                _state.update { it.copy(isLoading = false, error = "Initiation failed: $errorMsg") }
            }
        }
    }
}
