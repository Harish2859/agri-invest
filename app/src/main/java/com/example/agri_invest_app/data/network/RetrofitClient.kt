package com.example.agri_invest_app.data.network

import com.example.agri_invest_app.util.Constants
import com.example.agri_invest_app.util.JwtInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(JwtInterceptor { token })
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val projectService: ProjectService by lazy {
        retrofit.create(ProjectService::class.java)
    }

    val investmentService: InvestmentService by lazy {
        retrofit.create(InvestmentService::class.java)
    }

    val farmerService: FarmerService by lazy {
        retrofit.create(FarmerService::class.java)
    }

    val leadService: LeadService by lazy {
        retrofit.create(LeadService::class.java)
    }

    val walletService: WalletService by lazy {
        retrofit.create(WalletService::class.java)
    }

    // Keep instance for backward compatibility if needed, but preferred to use specific services
    val instance: AuthService get() = authService
}
