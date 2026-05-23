package com.example.agri_invest_app.ui.investor

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.InvestorPortfolio
import com.example.agri_invest_app.data.model.ProjectDiscovery
import com.example.agri_invest_app.data.model.User

data class InvestorDashboardState(
    val isLoading: Boolean = false,
    val projects: List<ProjectDiscovery> = emptyList(),
    val selectedProject: FarmProject? = null,
    val portfolio: InvestorPortfolio? = null,
    val user: User? = null,
    val error: String? = null,
    val successMessage: String? = null
)
