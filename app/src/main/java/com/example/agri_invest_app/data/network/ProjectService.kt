package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.InvestorPortfolio
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.ProjectDiscovery
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectService {
    @GET("/api/projects/discover")
    suspend fun discoverProjects(): Response<List<ProjectDiscovery>>

    @GET("/api/projects/{id}")
    suspend fun getProjectDetails(@Path("id") id: Long): Response<FarmProject>

    @GET("/api/dashboard/investor/me/portfolio")
    suspend fun getInvestorPortfolio(): Response<InvestorPortfolio>

    // FIX: Match the actual backend endpoint you found
    @GET("/api/milestones/project/{projectId}")
    suspend fun getProjectMilestones(@Path("projectId") projectId: Long): Response<List<Milestone>>
}
