package com.example.agri_invest_app.data.model

data class PortfolioResponse(
    val walletBalance: Double,
    val totalPortfolioValue: Double,
    val activeInvestments: List<Investment>
)
