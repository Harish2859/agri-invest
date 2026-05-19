package com.example.agri_invest_app.data.repository

import android.util.Log
import com.example.agri_invest_app.data.model.*
import com.example.agri_invest_app.data.network.FarmerService
import okhttp3.MultipartBody
import java.math.BigDecimal

class FarmerRepository(private val api: FarmerService) {
    suspend fun getProfile(): kotlin.Result<User> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Profile sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("FarmerRepository", "Network error fetching profile", e)
            kotlin.Result.failure(e)
        }
    }

    suspend fun getFreshProfile(): kotlin.Result<User> = getProfile()

    suspend fun uploadKyc(request: KycRequest): kotlin.Result<KycResponse> {
        return try {
            val response = api.uploadKyc(request)
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("KYC upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun createProject(request: ProjectCreateRequest): kotlin.Result<FarmProject> {
        return try {
            val response = api.createProject(request)
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Project creation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun getMyProjects(): kotlin.Result<List<FarmProject>> {
        return try {
            val response = api.getMyProjects()
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Fetch projects failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun submitMilestoneProof(milestoneId: Long, imagePart: MultipartBody.Part): kotlin.Result<Unit> {
        return try {
            val uploadResponse = api.uploadMilestoneProof(milestoneId, imagePart)
            if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
                val imageUrl = uploadResponse.body()!!["url"] ?: return kotlin.Result.failure(Exception("No URL returned from upload"))
                
                val submitResponse = api.submitMilestone(milestoneId, ProofRequest(imageUrl))
                if (submitResponse.isSuccessful) {
                    kotlin.Result.success(Unit)
                } else {
                    val errorBody = submitResponse.errorBody()?.string()
                    Log.e("FarmerRepository", "Final submission failed: $errorBody")
                    kotlin.Result.failure(Exception("Final submission failed"))
                }
            } else {
                kotlin.Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun withdrawFunds(amount: BigDecimal, projectId: Long? = null, bankDetails: String? = null): kotlin.Result<Unit> {
        return try {
            // PROPER FIX: Sending projectId, amount, and bankDetails to satisfy backend validation.
            val body = mutableMapOf<String, Any>("amount" to amount)
            
            if (projectId != null) {
                body["projectId"] = projectId
                body["project_id"] = projectId
            } else {
                return kotlin.Result.failure(Exception("A project context is required for withdrawal."))
            }

            if (bankDetails != null) {
                body["bankDetails"] = bankDetails
                body["bank_details"] = bankDetails
            } else {
                return kotlin.Result.failure(Exception("Bank details are mandatory for withdrawal."))
            }
            
            val response = api.withdrawFunds(body)
            if (response.isSuccessful) {
                kotlin.Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FarmerRepository", "Withdrawal failed: ${response.code()} - $errorBody")
                kotlin.Result.failure(Exception(errorBody ?: "Server error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("FarmerRepository", "Network error during withdrawal", e)
            kotlin.Result.failure(e)
        }
    }

    suspend fun settleProject(projectId: Long, revenue: BigDecimal): kotlin.Result<Unit> {
        return try {
            val response = api.settleProject(projectId, SettlementRequest(revenue))
            if (response.isSuccessful) {
                kotlin.Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FarmerRepository", "Project settlement failed: ${response.code()} - $errorBody")
                kotlin.Result.failure(Exception(errorBody ?: "Server error ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("FarmerRepository", "Network error during project settlement", e)
            kotlin.Result.failure(e)
        }
    }

    suspend fun getTransactionHistory(): kotlin.Result<List<Transaction>> {
        return try {
            val response = api.getTransactionHistory()
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Failed to fetch transactions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("FarmerRepository", "Network error fetching transactions", e)
            kotlin.Result.failure(e)
        }
    }
}
