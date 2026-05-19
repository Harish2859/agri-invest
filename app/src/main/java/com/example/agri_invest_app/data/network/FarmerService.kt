package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FarmerService {
    @GET("/api/auth/me")
    suspend fun getProfile(): Response<User>

    @POST("/api/users/upload-kyc")
    suspend fun uploadKyc(@Body request: KycRequest): Response<KycResponse>

    @POST("/api/projects/create")
    suspend fun createProject(@Body request: ProjectCreateRequest): Response<FarmProject>

    @GET("/api/projects/user/me")
    suspend fun getMyProjects(): Response<List<FarmProject>>

    @Multipart
    @POST("/api/milestones/{id}/upload-proof")
    suspend fun uploadMilestoneProof(
        @Path("id") milestoneId: Long,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @POST("/api/milestones/{id}/submit")
    suspend fun submitMilestone(
        @Path("id") milestoneId: Long,
        @Body request: ProofRequest
    ): Response<Unit>

    // FINAL FIX: Explicit JSON Header and absolute path to resolve the "body missing" 400 error
    @Headers("Content-Type: application/json")
    @POST("/api/wallet/withdraw")
    suspend fun withdrawFunds(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Unit>

    // Settlement Logic: Mark project as settled to trigger ROI distribution
    @POST("/api/projects/{id}/settle")
    suspend fun settleProject(@Path("id") projectId: Long, @Body request: SettlementRequest): Response<Unit>

    @GET("/api/transactions/my")
    suspend fun getTransactionHistory(): Response<List<Transaction>>
}
