package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.data.model.AuthResponse
import com.example.agri_invest_app.data.model.LoginRequest
import com.example.agri_invest_app.data.model.SignupRequest
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.network.AuthService

class AuthRepository(private val api: AuthService) {
    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login Failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(signupRequest: SignupRequest): Result<User> {
        return try {
            val response = api.signup(signupRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Signup Failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
