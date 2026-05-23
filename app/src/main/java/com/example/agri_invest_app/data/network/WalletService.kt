package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.DepositRequest
import com.example.agri_invest_app.data.model.WalletResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WalletService {
    @POST("/api/wallet/deposit")
    suspend fun depositFunds(@Body request: DepositRequest): Response<WalletResponse>
}
