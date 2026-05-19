package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class FarmProject(
    @SerializedName("id")
    val id: Long = 0,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("cropType", alternate = ["crop_type"])
    val cropType: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("targetAmount", alternate = ["target_amount"])
    val targetAmount: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("currentFunding", alternate = ["current_funding", "escrow_balance", "escrowBalance"])
    val amountAlreadyRaised: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("withdrawableBalance", alternate = ["withdrawable_balance"])
    val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("releasedToFarmer", alternate = ["released_to_farmer"])
    val releasedToFarmer: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("minInvestmentAmount", alternate = ["min_investment_amount"])
    val minInvestmentAmount: BigDecimal? = BigDecimal.ZERO,
    
    @SerializedName("landImageUrl", alternate = ["land_image_url"])
    val landImageUrl: String? = null,
    
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("farmer")
    val farmer: User? = null,
    
    @SerializedName("farmerName", alternate = ["farmer_name"])
    val farmerName: String? = null,

    @SerializedName("equityOffered", alternate = ["equity_offered", "equityPct", "equity_pct"])
    val equityOffered: Double = 0.0,

    @SerializedName("finalFarmerProfit", alternate = ["final_farmer_profit"])
    val finalFarmerProfit: BigDecimal? = null
)
