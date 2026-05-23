package com.example.agri_invest_app.data.repository

import com.example.agri_invest_app.util.Resource
import org.json.JSONObject
import retrofit2.Response

abstract class BaseRepository {
    protected fun <T> handleNetworkResponse(response: Response<T>): Resource<T> {
        return try {
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty payload")
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    val json = JSONObject(errorBody ?: "")
                    // Optimized error parsing to handle Spring Boot, Django, and Node.js error formats
                    json.optString("error", json.optString("message", json.optString("detail", "Error ${response.code()}")))
                } catch (e: Exception) {
                    "Error ${response.code()}"
                }
                
                when (response.code()) {
                    401 -> Resource.Error("Session expired. Please log in again.")
                    403 -> Resource.Error("Forbidden: $message")
                    404 -> Resource.Error("Endpoint not found (404). Check API path.")
                    400 -> Resource.Error("Bad Request: $message")
                    else -> Resource.Error(message)
                }
            }
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.message}")
        }
    }
}
