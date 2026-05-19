package com.example.agri_invest_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agri_invest_app.data.model.SignupRequest
import com.example.agri_invest_app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    fun signup(fullName: String, email: String, pass: String, role: String, aadhaar: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val request = SignupRequest(fullName, email, pass, role, aadhaar)
            val result = repository.signup(request)
            result.onSuccess { user ->
                _state.update { it.copy(isLoading = false, successUser = user) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
