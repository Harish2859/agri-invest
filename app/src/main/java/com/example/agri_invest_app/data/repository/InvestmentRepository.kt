package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.data.model.Investment
import com.example.agri_invest_app.data.model.InvestmentDetail
import com.example.agri_invest_app.data.model.InvestmentRequest
import com.example.agri_invest_app.data.model.InvestmentResponse
import com.example.agri_invest_app.data.network.InvestmentService

class InvestmentRepository(private val api: InvestmentService) {
    suspend fun initiateInvestment(request: InvestmentRequest): Result<InvestmentResponse> {
        return try {
            val response = api.initiateInvestment(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to initiate investment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeInvestment(investmentId: Long, txnId: String): Result<Investment> {
        return try {
            val response = api.completeInvestment(investmentId, txnId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to complete investment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvestmentHistory(): Result<List<InvestmentDetail>> {
        return try {
            val response = api.getInvestmentHistory()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch investment history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
