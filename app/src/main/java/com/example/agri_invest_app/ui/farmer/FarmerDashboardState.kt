package com.example.agri_invest_app.ui.farmer

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.Transaction
import java.math.BigDecimal

data class FarmerDashboardState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    val releasedToFarmer: BigDecimal = BigDecimal.ZERO,
    val myProjects: List<FarmProject> = emptyList(),
    val projectMilestones: Map<Long, List<Milestone>> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val kycMessage: String? = null
)
