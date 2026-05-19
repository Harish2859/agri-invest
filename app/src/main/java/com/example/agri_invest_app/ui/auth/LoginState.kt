package com.example.agri_invest_app.ui.auth

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val role: String? = null // To know where to redirect
)
