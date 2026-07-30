package com.aimobile.api

import retrofit2.Response
import retrofit2.http.*

// ===== Request Models =====
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val fullName: String, val email: String, val password: String)
data class RefreshTokenRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String)
data class ChatRequest(val command: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val resetToken: String, val newPassword: String)
data class UpdateProfileRequest(val fullName: String?, val avatar: String?)
data class VerifyEmailRequest(val email: String, val code: String)

data class PairingRequest(
    val pairingCode: String,
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String
)

// ===== Response Models =====
data class AuthResponse(
    val _id: String,
    val fullName: String,
    val email: String,
    val avatar: String?,
    val role: String?,
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenResponse(val accessToken: String)
data class MessageResponse(val message: String)

data class UserProfile(
    val _id: String,
    val fullName: String,
    val email: String,
    val avatar: String?,
    val role: String?
)

data class PairingResponse(
    val message: String,
    val token: String?
)

data class ChatResponse(
    val success: Boolean,
    val data: ChatData?
)

data class ChatData(
    val intent: String?,
    val reply: String?,
    val action: String?,
    val contact: String? = null,
    val number: String? = null,
    val message: String? = null,
    val time: String? = null,
    val duration: String? = null,
    val app: String? = null,
    val query: String? = null,
    val steps: List<com.aimobile.data.local.MacroStep>? = null
)

// ===== API Interface =====
interface ApiService {

    // Auth
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<MessageResponse>

    @POST("/api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("/api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @GET("/api/auth/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("/api/auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>

    // Device
    @POST("/api/device/link")
    suspend fun linkDevice(@Body request: PairingRequest): Response<PairingResponse>

    @GET("/api/device/list")
    suspend fun getDevices(): Response<List<Any>>

    // AI Chat
    @POST("/api/ai/chat")
    suspend fun sendChat(@Body request: ChatRequest): Response<ChatResponse>

    // Cloud Sync, Analytics, Backup/Restore, History
    @GET("/api/history")
    suspend fun getHistory(
        @Query("deviceId") deviceId: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): Response<List<HistoryItem>>

    @GET("/api/analytics")
    suspend fun getAnalytics(): Response<AnalyticsResponse>

    @POST("/api/backup")
    suspend fun backupSettings(@Body request: BackupRequest): Response<BackupResponse>

    @POST("/api/restore")
    suspend fun restoreSettings(@Body request: RestoreRequest): Response<BackupResponse>

    @POST("/api/sync")
    suspend fun syncOfflineEvents(@Body request: SyncRequest): Response<MessageResponse>

    @GET("/api/device/all")
    suspend fun getAllDevices(): Response<List<CloudDeviceItem>>
}

// ===== Cloud Sync Request/Response Models =====
data class HistoryItem(
    val _id: String,
    val deviceId: String? = "dev_local",
    val deviceName: String? = "Vivo V2250",
    val command: String? = null,
    val intent: String? = null,
    val status: String? = "Completed",
    val executionTimeMs: Int = 120,
    val errorMessage: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class PopularIntent(
    val _id: String,
    val count: Int
)

data class AnalyticsResponse(
    val totalCommands: Int,
    val successRate: Int,
    val failedCommands: Int,
    val avgSpeedMs: Int,
    val popularIntents: List<PopularIntent>
)

data class BackupRequest(
    val deviceId: String,
    val deviceName: String,
    val settingsPayload: String
)

data class RestoreRequest(
    val deviceId: String
)

data class BackupResponse(
    val _id: String,
    val deviceId: String,
    val deviceName: String,
    val settingsPayload: String,
    val createdAt: String
)

data class SyncEvent(
    val deviceId: String,
    val deviceName: String,
    val command: String,
    val intent: String,
    val status: String,
    val executionTimeMs: Int,
    val errorMessage: String?
)

data class SyncRequest(
    val events: List<SyncEvent>
)

data class CloudDeviceItem(
    val _id: String,
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val battery: Int,
    val network: String,
    val status: String,
    val lastSeen: String?
)

