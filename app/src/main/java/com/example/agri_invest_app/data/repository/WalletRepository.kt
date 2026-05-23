package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.data.model.DepositRequest
import com.example.agri_invest_app.data.model.WalletResponse
import com.example.agri_invest_app.data.network.WalletService
import com.example.agri_invest_app.util.Resource

class WalletRepository(private val api: WalletService) : BaseRepository() {
    suspend fun depositFunds(amount: Double): Resource<WalletResponse> {
        return try {
            val response = api.depositFunds(DepositRequest(amount))
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error occurred")
        }
    }
}
