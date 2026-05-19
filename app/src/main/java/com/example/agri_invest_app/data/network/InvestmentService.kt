package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.Investment
import com.example.agri_invest_app.data.model.InvestmentDetail
import com.example.agri_invest_app.data.model.InvestmentRequest
import com.example.agri_invest_app.data.model.InvestmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InvestmentService {
    @POST("/api/investments/pay")
    suspend fun initiateInvestment(@Body request: InvestmentRequest): Response<InvestmentResponse>

    @POST("/api/investments/complete/{id}")
    suspend fun completeInvestment(
        @Path("id") investmentId: Long,
        @Query("txnId") txnId: String
    ): Response<Investment>

    @GET("/api/investments/my-history")
    suspend fun getInvestmentHistory(): Response<List<InvestmentDetail>>
}
