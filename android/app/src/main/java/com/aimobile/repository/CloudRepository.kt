package com.aimobile.repository

import com.aimobile.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getHistory(deviceId: String? = null, status: String? = null, search: String? = null): List<HistoryItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistory(deviceId, status, search)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAnalytics(): AnalyticsResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAnalytics()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun backupSettings(deviceId: String, deviceName: String, settingsJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.backupSettings(BackupRequest(deviceId, deviceName, settingsJson))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreSettings(deviceId: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.restoreSettings(RestoreRequest(deviceId))
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.settingsPayload
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncOfflineEvents(events: List<SyncEvent>): Boolean = withContext(Dispatchers.IO) {
        if (events.isEmpty()) return@withContext true
        try {
            val response = apiService.syncOfflineEvents(SyncRequest(events))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllDevices(): List<CloudDeviceItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllDevices()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
