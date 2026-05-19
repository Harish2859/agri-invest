package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName

data class InvestmentResponse(
    @SerializedName("id") val investmentId: Long,
    val status: String?,
    val message: String?
)
