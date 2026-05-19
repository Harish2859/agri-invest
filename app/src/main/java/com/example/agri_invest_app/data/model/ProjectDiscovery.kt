package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ProjectDiscovery(
    val projectId: Long,
    val title: String?,
    val location: String,
    val crop: String,
    val targetAmount: BigDecimal,
    
    @SerializedName("currentFunding", alternate = ["amountAlreadyRaised", "raisedAmount"])
    val amountAlreadyRaised: BigDecimal,

    val remainingAmount: BigDecimal,
    val fundingPercentage: String,
    val farmerName: String
)
