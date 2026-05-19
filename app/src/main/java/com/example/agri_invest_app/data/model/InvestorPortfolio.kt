package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName

data class InvestorPortfolio(
    val summary: PortfolioSummary?,
    val riskProfile: RiskProfile?,
    val investments: List<InvestmentDetail>?
)

data class PortfolioSummary(
    @SerializedName("walletBalance", alternate = ["wallet_balance"])
    val walletBalance: Double,
    @SerializedName("totalPortfolioValue", alternate = ["total_portfolio_value"])
    val totalPortfolioValue: Double,
    @SerializedName("impactFarmersHelped", alternate = ["impact_farmers_helped"])
    val impactFarmersHelped: Int,
    @SerializedName("activeInvestmentsCount", alternate = ["active_investments_count"])
    val activeInvestmentsCount: Int
)

data class RiskProfile(
    val profileType: String?,
    val diversifiedCrops: List<String>?
)

data class InvestmentDetail(
    @SerializedName("id", alternate = ["investmentId", "investment_id"])
    val investmentId: Long?,
    @SerializedName("projectId", alternate = ["project_id"])
    val projectId: Long?,
    @SerializedName("projectTitle", alternate = ["project_title"])
    val projectTitle: String?,
    @SerializedName("amountInvested", alternate = ["amount_invested"])
    val amountInvested: Double,
    @SerializedName("status")
    val currentStatus: String?,
    @SerializedName("expectedReturn", alternate = ["expected_return"])
    val expectedReturn: Double? = null,
    @SerializedName("finalReturn", alternate = ["final_return"])
    val finalReturn: Double? = null,
    @SerializedName("settled")
    val settled: Boolean = false,
    val crop: String? = null
)
