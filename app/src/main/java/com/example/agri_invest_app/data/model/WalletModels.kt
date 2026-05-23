package com.example.agri_invest_app.data.model

import java.math.BigDecimal

data class DepositRequest(
    val amount: Double
)

data class WalletResponse(
    val status: String,
    val message: String,
    val newBalance: BigDecimal
)
