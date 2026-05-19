package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.AuthResponse
import com.example.agri_invest_app.data.model.LoginRequest
import com.example.agri_invest_app.data.model.SignupRequest
import com.example.agri_invest_app.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<User>

    // UPDATED: Correct backend mapping found in analysis
    @GET("/api/auth/me")
    suspend fun getProfile(): Response<User>
}
