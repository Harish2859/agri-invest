package com.example.agri_invest_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.LoginRequest
import com.example.agri_invest_app.data.network.RetrofitClient
import com.example.agri_invest_app.data.repository.AuthRepository
import com.example.agri_invest_app.util.DataStoreManager
import com.example.agri_invest_app.util.Resource
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
            
            when (result) {
                is Resource.Success -> {
                    val response = result.data
                    // SAFE NULL HANDLING: Provide defaults for token and role
                    val token = response.token ?: ""
                    val role = response.role ?: "INVESTOR"
                    dataStoreManager.saveAuthData(token, role, response.verified)
                    RetrofitClient.setToken(token)
                    _state.update { it.copy(isLoading = false, isSuccess = true, role = role) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> { }
            }
        }
    }

    // KILL THE LOOP: Reset the login state after logout
    fun resetState() {
        _state.update { LoginState() }
    }
}
