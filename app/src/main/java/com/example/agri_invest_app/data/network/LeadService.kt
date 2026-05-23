package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.Transaction
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

data class WithdrawRequest(
    val amount: BigDecimal,
    val bankDetails: String
)

interface LeadService {
    @GET("/api/auth/me")
    suspend fun getProfile(): Response<User>

    @GET("/api/admin/pending-kyc")
    suspend fun getPendingUsers(): Response<List<User>>

    @GET("/api/admin/pending-projects")
    suspend fun getPendingProjects(): Response<List<FarmProject>>

    @GET("/api/admin/pending-milestones")
    suspend fun getPendingMilestones(): Response<List<Milestone>>

    @POST("/api/admin/verify-user/{id}")
    suspend fun verifyUser(
        @Path("id") userId: Long,
        @Query("approve") isApproved: Boolean,
        @Query("reason") reason: String? = null
    ): Response<User>

    @POST("/api/admin/approve-project/{id}")
    suspend fun approveProject(
        @Path("id") projectId: Long,
        @Query("approve") approve: Boolean = true
    ): Response<FarmProject>

    @POST("/api/milestones/{id}/verify")
    suspend fun approveMilestone(
        @Path("id") id: Long,
        @Query("approved") approved: Boolean = true
    ): Response<Milestone>

    @GET("/api/transactions/my")
    suspend fun getTransactionHistory(): Response<List<Transaction>>

    @POST("/api/wallet/withdraw")
    suspend fun withdrawCommissions(@Body request: WithdrawRequest): Response<Unit>
}
