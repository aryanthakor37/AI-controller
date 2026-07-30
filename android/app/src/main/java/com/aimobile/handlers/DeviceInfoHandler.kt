package com.aimobile.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DeviceInfoHandler(private val context: Context) {

    suspend fun getBatteryStatus(): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }

            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = (level * 100 / scale.toFloat()).toInt()
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val chargingLabel = if (isCharging) " (Charging ⚡)" else ""

            val json = JSONObject().apply {
                put("battery_level", batteryPct)
                put("is_charging", isCharging)
            }
            
            CommandResult(status = "Success", message = "🔋 Battery Level: $batteryPct%$chargingLabel", data = json.toString())
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Failed to get battery status: ${e.message}")
        }
    }

    suspend fun getDeviceInfo(): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val model = android.os.Build.MODEL ?: "Android Device"
            val brand = android.os.Build.MANUFACTURER ?: "Generic"
            val androidVer = android.os.Build.VERSION.RELEASE ?: "14"
            val message = "📱 Device: $brand $model | Android $androidVer"
            CommandResult(status = "Success", message = message)
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Failed to get device info: ${e.message}")
        }
    }

    suspend fun getNetworkStatus(): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork?.isConnectedOrConnecting == true
            val typeName = activeNetwork?.typeName ?: "Dis-connected"
            
            val json = JSONObject().apply {
                put("connected", isConnected)
                put("type", typeName)
            }
            
            CommandResult(status = "Success", message = "📶 Network: $typeName (${if (isConnected) "Connected" else "Offline"})", data = json.toString())
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Failed to get network status: ${e.message}")
        }
    }
}
