package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<User>

    @GET("/api/auth/me")
    suspend fun getProfile(): Response<User>

    @POST("/api/users/upload-kyc")
    suspend fun submitKyc(@Body request: KycRequest): Response<KycResponse>
}
