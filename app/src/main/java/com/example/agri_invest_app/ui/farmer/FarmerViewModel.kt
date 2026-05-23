package com.example.agri_invest_app.ui.farmer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.KycRequest
import com.example.agri_invest_app.data.model.ProjectCreateRequest
import com.example.agri_invest_app.data.repository.FarmerRepository
import com.example.agri_invest_app.data.repository.ProjectRepository
import com.example.agri_invest_app.util.DataStoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

class FarmerViewModel(
    private val farmerRepository: FarmerRepository,
    private val projectRepository: ProjectRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(FarmerDashboardState())
    val state: StateFlow<FarmerDashboardState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(ProjectCreateRequest("", "", BigDecimal.ZERO, BigDecimal.ZERO, 0.0, "", ""))
    val formState: StateFlow<ProjectCreateRequest> = _formState.asStateFlow()

    private val _projectCreated = MutableStateFlow(false)
    val projectCreated: StateFlow<Boolean> = _projectCreated.asStateFlow()

    init {
        viewModelScope.launch {
            val cachedVerified = dataStoreManager.isVerified()
            _state.update { it.copy(isVerified = cachedVerified) }
            loadDashboardData()
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // 1. Parallel Fetch: Profile, Projects, and Transactions
            val userDeferred = async { farmerRepository.getFreshProfile() }
            val projectsDeferred = async { farmerRepository.getMyProjects() }
            val transactionsDeferred = async { farmerRepository.getTransactionHistory() }

            val user = userDeferred.await().getOrNull()
            val basicProjects = projectsDeferred.await().getOrDefault(emptyList())
            val transactions = transactionsDeferred.await().getOrDefault(emptyList())

            // 2. Sync Detailed Project Info (optional, but keep for consistency)
            val detailedProjects = basicProjects.map { p ->
                async { projectRepository.getProjectDetails(p.id).getOrDefault(p) }
            }.awaitAll()

            // 3. Reliable Totals: Use User Profile as the single source of truth for the wallet.
            // FIXED: Removed the erroneous client-side summation that caused balance jumps.
            val finalWithdrawable = user?.walletBalance ?: BigDecimal.ZERO
            val finalReleased = user?.releasedToFarmer ?: BigDecimal.ZERO

            if (user != null) {
                dataStoreManager.saveAuthData(dataStoreManager.getToken() ?: "", "FARMER", user.verified)
            }

            _state.update { it.copy(
                isLoading = false,
                isVerified = user?.verified ?: it.isVerified,
                withdrawableBalance = finalWithdrawable,
                releasedToFarmer = finalReleased,
                myProjects = detailedProjects,
                transactions = transactions.sortedByDescending { it.timestamp }
            ) }

            // Background sync for milestones
            detailedProjects.forEach { project ->
                fetchMilestones(project.id)
            }
        }
    }

    fun fetchMilestones(projectId: Long) {
        viewModelScope.launch {
            projectRepository.getProjectMilestones(projectId).onSuccess { milestones ->
                _state.update { currentState ->
                    val updatedMap = currentState.projectMilestones.toMutableMap()
                    updatedMap[projectId] = milestones
                    currentState.copy(projectMilestones = updatedMap)
                }
            }
        }
    }

    fun submitMilestoneProof(context: Context, milestoneId: Long, uri: Uri, projectId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val imagePart = prepareImagePart(context, uri, "file")
            if (imagePart != null) {
                farmerRepository.submitMilestoneProof(milestoneId, imagePart).onSuccess {
                    _state.update { it.copy(successMessage = "Proof submitted! Syncing wallet...") }
                    loadDashboardData()
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Upload failed: ${e.message}") }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Could not process image file.") }
            }
        }
    }

    fun withdrawFunds(amount: BigDecimal, projectId: Long? = null, bankDetails: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            farmerRepository.withdrawFunds(amount, projectId, bankDetails).onSuccess {
                _state.update { it.copy(successMessage = "Withdrawal request sent successfully!") }
                loadDashboardData()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun settleProject(projectId: Long, revenue: BigDecimal) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            farmerRepository.settleProject(projectId, revenue).onSuccess {
                _state.update { it.copy(successMessage = "Project settled! Funds distributed successfully.") }
                loadDashboardData()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun prepareImagePart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
        } catch (e: Exception) {
            Log.e("FarmerViewModel", "Error preparing image part", e)
            null
        }
    }

    fun clearMessages() { _state.update { it.copy(error = null, successMessage = null) } }
    fun updateForm(update: (ProjectCreateRequest) -> ProjectCreateRequest) { _formState.update(update) }
    fun createProject() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            farmerRepository.createProject(_formState.value).onSuccess {
                _projectCreated.value = true
                loadDashboardData()
                resetForm()
            }
        }
    }
    fun resetProjectCreated() { _projectCreated.value = false }
    private fun resetForm() { _formState.value = ProjectCreateRequest("", "", BigDecimal.ZERO, BigDecimal.ZERO, 0.0, "", "") }
    fun submitKyc(documentUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            farmerRepository.uploadKyc(KycRequest(documentUrl)).onSuccess { loadDashboardData() }
        }
    }
}
