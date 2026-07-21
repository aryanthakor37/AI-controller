package com.aimobile.repository

import com.aimobile.api.ApiService
import com.aimobile.api.AuthResponse
import com.aimobile.api.LoginRequest
import com.aimobile.api.LogoutRequest
import com.aimobile.api.RegisterRequest
import com.aimobile.api.RefreshTokenRequest
import com.aimobile.api.UserProfile
import com.aimobile.utils.TokenManager
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String) : AuthResult<T>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            handleAuthResponse(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error. Check your connection.")
        }
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = apiService.register(com.aimobile.api.RegisterRequest(fullName, email, password))
            handleAuthResponse(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error. Check your connection.")
        }
    }

    suspend fun logout() {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                apiService.logout(LogoutRequest(refreshToken))
            }
        } catch (_: Exception) {}
        tokenManager.clearAll()
    }

    suspend fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return false
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveAccessToken(response.body()!!.accessToken)
                true
            } else {
                tokenManager.clearAll()
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getProfile(): AuthResult<UserProfile> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error(response.errorBody()?.string() ?: "Failed to load profile")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun forgotPassword(email: String): AuthResult<com.aimobile.api.MessageResponse> {
        return try {
            val response = apiService.forgotPassword(com.aimobile.api.ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error(response.errorBody()?.string() ?: "Failed to send reset link")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    private fun handleAuthResponse(response: Response<AuthResponse>): AuthResult<AuthResponse> {
        return if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            // Persist both tokens and user
            tokenManager.saveAccessToken(body.accessToken)
            tokenManager.saveRefreshToken(body.refreshToken)
            tokenManager.saveUser(gson.toJson(body))
            AuthResult.Success(body)
        } else {
            val errorMsg = try {
                val errorJson = response.errorBody()?.string()
                gson.fromJson(errorJson, Map::class.java)["message"] as? String ?: "Authentication failed"
            } catch (_: Exception) { "Authentication failed" }
            AuthResult.Error(errorMsg)
        }
    }
}
