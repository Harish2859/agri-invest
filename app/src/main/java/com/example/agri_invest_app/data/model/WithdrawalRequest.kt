package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName

data class WithdrawalRequest(
    @SerializedName("amount")
    val amount: Double,

    @SerializedName("projectId", alternate = ["project_id"])
    val projectId: Long,

    @SerializedName("bankDetails", alternate = ["bank_details"])
    val bankDetails: String
)
