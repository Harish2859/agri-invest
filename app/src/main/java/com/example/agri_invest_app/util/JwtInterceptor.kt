package com.example.agri_invest_app.util

import okhttp3.Interceptor
import okhttp3.Response

class JwtInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        
        // Get the token (from your DataStore later)
        val token = tokenProvider()
        
        // Attach it if it exists
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
