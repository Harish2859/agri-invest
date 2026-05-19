package com.example.agri_invest_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.LoginRequest
import com.example.agri_invest_app.data.network.RetrofitClient
import com.example.agri_invest_app.data.repository.AuthRepository
import com.example.agri_invest_app.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.login(LoginRequest(email, pass))
            
            result.onSuccess { response ->
                dataStoreManager.saveAuthData(response.token, response.role, response.verified)
                RetrofitClient.setToken(response.token)
                _state.update { it.copy(isLoading = false, isSuccess = true, role = response.role) }
            }.onFailure { e ->
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Invalid email or password."
                    e.message?.contains("403") == true -> "Account not authorized."
                    else -> e.message ?: "Login failed. Please check your connection."
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    // KILL THE LOOP: Reset the login state after logout
    fun resetState() {
        _state.update { LoginState() }
    }
}
