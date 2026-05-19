package com.example.agri_invest_app.data.model

data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String, // FARMER or INVESTOR
    val aadhaarNo: String? = null
)
