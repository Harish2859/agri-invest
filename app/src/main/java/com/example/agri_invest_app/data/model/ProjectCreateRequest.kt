package com.example.agri_invest_app.data.model

import java.math.BigDecimal

data class ProjectCreateRequest(
    val title: String,
    val cropType: String,
    val targetAmount: BigDecimal,
    val minInvestmentAmount: BigDecimal,
    val equityOffered: Double, // Percentage can remain Double or use BigDecimal
    val location: String,
    val description: String,
    val landImageUrl: String? = null
)
