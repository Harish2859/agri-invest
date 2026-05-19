package com.example.agri_invest_app.data.model

import java.math.BigDecimal

data class Transaction(
    val id: Long,
    val type: String, // "INVESTMENT", "RELEASE", "WITHDRAWAL", "ROI_PAYOUT"
    val amount: BigDecimal,
    val timestamp: String,
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val description: String
)
