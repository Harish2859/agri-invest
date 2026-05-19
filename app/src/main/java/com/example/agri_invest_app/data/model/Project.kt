package com.example.agri_invest_app.data.model

data class Project(
    val id: Long?,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val status: String, // PENDING, APPROVED, ACTIVE, COMPLETED
    val farmerId: Long
)
