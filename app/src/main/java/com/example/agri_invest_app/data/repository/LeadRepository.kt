package com.example.agri_invest_app.data.repository

import android.util.Log
import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.Transaction
import com.example.agri_invest_app.data.network.LeadService
import com.example.agri_invest_app.data.network.WithdrawRequest

class LeadRepository(private val api: LeadService) {
    suspend fun getProfile(): Result<User> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Profile sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Network error fetching profile", e)
            Result.failure(e)
        }
    }

    suspend fun getPendingUsers(): Result<List<User>> {
        return try {
            val response = api.getPendingUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pending users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Connection error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPendingProjects(): Result<List<FarmProject>> {
        return try {
            val response = api.getPendingProjects()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pending projects: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingMilestones(): Result<List<Milestone>> {
        return try {
            val response = api.getPendingMilestones()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pending milestones: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyUser(userId: Long, approve: Boolean, reason: String? = null): Result<User> {
        return try {
            val response = api.verifyUser(userId, approve, reason)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("LeadRepository", "User verification failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Verification failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Network error during user verification", e)
            Result.failure(e)
        }
    }

    suspend fun approveProject(projectId: Long): Result<FarmProject> {
        return try {
            val response = api.approveProject(projectId, true)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("LeadRepository", "Project approval failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Project approval failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Network error during project approval", e)
            Result.failure(e)
        }
    }

    suspend fun approveMilestone(milestoneId: Long): Result<Milestone> {
        return try {
            val response = api.approveMilestone(milestoneId, true)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("LeadRepository", "Milestone approval failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Milestone approval failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Network error during milestone approval", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactionHistory(): Result<List<Transaction>> {
        return try {
            val response = api.getTransactionHistory()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch transactions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeadRepository", "Network error fetching transactions", e)
            Result.failure(e)
        }
    }

    suspend fun withdrawCommissions(request: WithdrawRequest): Result<Unit> {
        return try {
            val response = api.withdrawCommissions(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Withdrawal failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
