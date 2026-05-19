package com.example.agri_invest_app.ui.lead

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.User
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.Transaction
import java.math.BigDecimal

data class LeadDashboardState(
    val isLoading: Boolean = false,
    val walletBalance: BigDecimal = BigDecimal.ZERO,
    val pendingUsers: List<User> = emptyList(),
    val pendingProjects: List<FarmProject> = emptyList(),
    val pendingMilestones: List<Milestone> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)
