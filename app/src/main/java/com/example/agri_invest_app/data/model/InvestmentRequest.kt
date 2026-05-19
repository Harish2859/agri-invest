package com.example.agri_invest_app.data.model

import java.math.BigDecimal

data class InvestmentRequest(
    val project: ProjectIdWrapper,
    val amountInvested: BigDecimal
)

data class ProjectIdWrapper(
    val id: Long
)
