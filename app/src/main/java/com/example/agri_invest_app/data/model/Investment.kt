package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

data class Investment(
    val id: Long?,
    val investorId: Long,
    val projectId: Long,
    @SerializedName("projectTitle")
    val projectTitle: String? = null,
    val amount: BigDecimal,
    val date: Date = Date()
)
