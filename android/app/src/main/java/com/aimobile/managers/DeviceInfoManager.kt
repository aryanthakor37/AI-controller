package com.aimobile.managers

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    fun getDeviceRegistrationPayload(): JSONObject {
        val payload = JSONObject()
        payload.put("deviceName", Build.MODEL ?: "Unknown Device")
        payload.put("androidVersion", Build.VERSION.RELEASE ?: "Unknown")
        payload.put("manufacturer", Build.MANUFACTURER ?: "Unknown")
        payload.put("model", Build.MODEL ?: "Unknown")
        payload.put("batteryPercentage", getBatteryPercentage())
        payload.put("ram", "4GB (Stub)")
        payload.put("storage", "64GB (Stub)")
        payload.put("wifiStatus", "Connected")
        payload.put("ipAddress", "192.168.1.100 (Stub)")
        payload.put("appVersion", "1.0.0")
        return payload
    }

    fun getBatteryPercentage(): Int {
        return 100 // Stub
    }
}
