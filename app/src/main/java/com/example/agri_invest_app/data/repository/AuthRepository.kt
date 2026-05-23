package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.data.model.*
import com.example.agri_invest_app.data.network.AuthService
import com.example.agri_invest_app.util.Resource

class AuthRepository(private val api: AuthService) : BaseRepository() {
    suspend fun login(loginRequest: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = api.login(loginRequest)
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login Failed")
        }
    }

    suspend fun signup(signupRequest: SignupRequest): Resource<User> {
        return try {
            val response = api.signup(signupRequest)
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Signup Failed")
        }
    }

    suspend fun getProfile(): Resource<User> {
        return try {
            val response = api.getProfile()
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    suspend fun submitKyc(documentUrl: String): Resource<KycResponse> {
        return try {
            val response = api.submitKyc(KycRequest(documentUrl))
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "KYC submission failed")
        }
    }
}
