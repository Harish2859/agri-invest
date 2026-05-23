package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class InvestorPortfolio(
    val summary: PortfolioSummary?,
    val riskProfile: RiskProfile?,
    val investments: List<InvestmentDetail>?
)

data class PortfolioSummary(
    // CHANGED: Using BigDecimal for consistency and precision
    // CHANGED: Narrowed alternates to avoid mapping "Total Portfolio Value" into "Wallet Balance"
    @SerializedName("walletBalance", alternate = ["wallet_balance", "available_cash", "cash_balance"])
    val walletBalance: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("totalPortfolioValue", alternate = ["total_portfolio_value", "net_worth", "total_assets"])
    val totalPortfolioValue: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("impactFarmersHelped", alternate = ["impact_farmers_helped"])
    val impactFarmersHelped: Int = 0,
    
    @SerializedName("activeInvestmentsCount", alternate = ["active_investments_count"])
    val activeInvestmentsCount: Int = 0
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
    
    // CHANGED: Using BigDecimal
    @SerializedName("amountInvested", alternate = ["amount_invested", "principal"])
    val amountInvested: BigDecimal = BigDecimal.ZERO,
    
    @SerializedName("status")
    val currentStatus: String?,
    
    @SerializedName("expectedReturn", alternate = ["expected_return"])
    val expectedReturn: BigDecimal? = null,
    
    @SerializedName("finalReturn", alternate = ["final_return"])
    val finalReturn: BigDecimal? = null,

    @SerializedName("settled")
    val settled: Boolean = false,
    val crop: String? = null
)
