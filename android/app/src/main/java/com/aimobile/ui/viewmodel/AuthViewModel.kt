package com.aimobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimobile.api.AuthResponse
import com.aimobile.repository.AuthRepository
import com.aimobile.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: AuthResponse) : AuthState()
    data class RegisterSuccess(val email: String) : AuthState()
    data class ForgotPasswordSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.login(email.trim(), password)) {
                is AuthResult.Success -> _authState.value = AuthState.Success(result.data)
                is AuthResult.Error -> _authState.value = AuthState.Error(result.message)
            }
        }
    }

    fun register(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.register(fullName.trim(), email.trim(), password)) {
                is AuthResult.Success -> _authState.value = AuthState.RegisterSuccess(email.trim())
                is AuthResult.Error -> _authState.value = AuthState.Error(result.message)
            }
        }
    }

    fun verifyEmail(email: String, code: String) {
        if (email.isBlank() || code.isBlank()) {
            _authState.value = AuthState.Error("Email and code cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.verifyEmail(email, code)) {
                is AuthResult.Success -> _authState.value = AuthState.Success(result.data)
                is AuthResult.Error -> _authState.value = AuthState.Error(result.message)
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authRepository.forgotPassword(email.trim())) {
                is AuthResult.Success -> _authState.value = AuthState.ForgotPasswordSuccess(result.data.message)
                is AuthResult.Error -> _authState.value = AuthState.Error(result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
