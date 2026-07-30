package com.aimobile.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aimobile_auth", Context.MODE_PRIVATE)

    // Access Token (short-lived JWT)
    fun saveAccessToken(token: String) = prefs.edit().putString("access_token", token).apply()
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    // Refresh Token (long-lived)
    fun saveRefreshToken(token: String) = prefs.edit().putString("refresh_token", token).apply()
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    // User Profile (JSON string)
    fun saveUser(userJson: String) = prefs.edit().putString("user_profile", userJson).apply()
    fun getUser(): String? = prefs.getString("user_profile", null)

    // Legacy single-token support (for SocketManager compatibility)
    fun saveToken(token: String) = saveAccessToken(token)
    fun getToken(): String? = getAccessToken()

    fun isLoggedIn(): Boolean = getAccessToken() != null && getRefreshToken() != null

    // Server URL configuration
    fun saveServerUrl(url: String) = prefs.edit().putString("server_url", url).apply()
    
    // Floating Overlay Widget configuration
    fun saveOverlayEnabled(enabled: Boolean) = prefs.edit().putBoolean("overlay_enabled", enabled).apply()
    fun isOverlayEnabled(): Boolean = prefs.getBoolean("overlay_enabled", false)
    
    fun getServerUrl(): String {
        val rawUrl = prefs.getString("server_url", "https://aimobile-backend.onrender.com/") ?: "https://aimobile-backend.onrender.com/"
        var clean = rawUrl.trim()
        if (clean.isEmpty()) {
            return "https://aimobile-backend.onrender.com/"
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return try {
            java.net.URL(clean)
            clean
        } catch (e: Exception) {
            "https://aimobile-backend.onrender.com/"
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // Legacy
    fun clearToken() = clearAll()
}


