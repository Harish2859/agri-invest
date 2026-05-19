package com.example.agri_invest_app.ui.auth

import com.example.agri_invest_app.data.model.User

data class SignupState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successUser: User? = null
)
