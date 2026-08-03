package com.aimobile.managers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
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
        payload.put("ram", getRamInfo())
        payload.put("storage", getStorageInfo())
        payload.put("wifiStatus", getWifiStatus())
        payload.put("ipAddress", getIpAddress())
        payload.put("appVersion", "1.0.0")
        return payload
    }

    fun getBatteryPercentage(): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        return if (level == -1 || scale == -1) {
            100
        } else {
            (level * 100 / scale.toFloat()).toInt()
        }
    }

    fun getRamInfo(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalMemGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            "%.1f GB".format(totalMemGb)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getStorageInfo(): String {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val totalBytes = stat.blockSizeLong * stat.blockCountLong
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            "%.1f GB".format(totalGb)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getWifiStatus(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "Wi-Fi"
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "Mobile Data"
                else "Connected"
            } else {
                "Disconnected"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in Collections.list(interfaces)) {
                for (addr in Collections.list(intf.inetAddresses)) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "Unknown"
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
