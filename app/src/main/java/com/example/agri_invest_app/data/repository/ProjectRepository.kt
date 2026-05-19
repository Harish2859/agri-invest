package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.data.model.FarmProject
import com.example.agri_invest_app.data.model.InvestorPortfolio
import com.example.agri_invest_app.data.model.Milestone
import com.example.agri_invest_app.data.model.ProjectDiscovery
import com.example.agri_invest_app.data.network.ProjectService

class ProjectRepository(private val api: ProjectService) {
    suspend fun discoverProjects(): Result<List<ProjectDiscovery>> {
        return try {
            val response = api.discoverProjects()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch projects: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjectDetails(id: Long): Result<FarmProject> {
        return try {
            val response = api.getProjectDetails(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch project details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvestorPortfolio(): Result<InvestorPortfolio> {
        return try {
            val response = api.getInvestorPortfolio()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch portfolio: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjectMilestones(projectId: Long): Result<List<Milestone>> {
        return try {
            val response = api.getProjectMilestones(projectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch milestones: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
