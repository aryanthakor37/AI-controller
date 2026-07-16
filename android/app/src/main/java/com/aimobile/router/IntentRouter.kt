package com.aimobile.router

import android.content.Context
import android.content.Intent
import com.aimobile.handlers.*
import com.aimobile.models.CommandRequest
import com.aimobile.models.CommandResult

class IntentRouter(private val context: Context) {
    
    private val flashlightHandler = FlashlightHandler(context)
    private val openAppHandler = OpenAppHandler(context)
    private val volumeHandler = VolumeHandler(context)
    private val alarmHandler = AlarmHandler(context)
    private val deviceInfoHandler = DeviceInfoHandler(context)
    private val callHandler = CallHandler(context)
    private val smsHandler = SMSHandler(context)

    suspend fun route(request: CommandRequest): CommandResult {
        return try {
            when (request.intent) {
                "FLASHLIGHT_ON" -> flashlightHandler.turnOn()
                "FLASHLIGHT_OFF" -> flashlightHandler.turnOff()
                
                "OPEN_CHROME", "OPEN_CAMERA", "OPEN_GALLERY", "OPEN_YOUTUBE",
                "OPEN_MAPS", "OPEN_GMAIL", "OPEN_CALCULATOR", "OPEN_SETTINGS",
                "OPEN_CONTACTS", "OPEN_DIALER", "OPEN_PLAY_STORE", "OPEN_CLOCK",
                "OPEN_FILES", "OPEN_PHOTOS" -> openAppHandler.openApp(request.intent)
                
                "OPEN_APP" -> {
                    val appName = request.message ?: ""
                    if (appName.equals("WhatsApp", ignoreCase = true) || request.intent.contains("WhatsApp")) {
                        openAppHandler.openApp("OPEN_WHATSAPP")
                    } else {
                        // Attempt to open settings as a fallback or search launch intent
                        val pkgIntent = context.packageManager.getLaunchIntentForPackage(appName)
                        if (pkgIntent != null) {
                            pkgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(pkgIntent)
                            CommandResult("Success", "Opened $appName")
                        } else {
                            CommandResult("Failed", "App package not found for: $appName")
                        }
                    }
                }

                "SEND_WHATSAPP_MESSAGE", "MESSAGE_WHATSAPP" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    if (service == null) {
                        CommandResult("Permission Required", "Accessibility Service is disabled. Please enable it in Settings.")
                    } else {
                        com.aimobile.accessibility.automation.AppAutomations.runWhatsAppAutomation(
                            service,
                            context,
                            request.number ?: request.message ?: "Mom",
                            request.message ?: "Hello from AgentAI"
                        )
                    }
                }

                "SEARCH_YOUTUBE", "YOUTUBE_SEARCH" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    if (service == null) {
                        CommandResult("Permission Required", "Accessibility Service is disabled. Please enable it in Settings.")
                    } else {
                        com.aimobile.accessibility.automation.AppAutomations.runYouTubeAutomation(
                            service,
                            context,
                            request.message ?: "AI news"
                        )
                    }
                }

                "OPEN_WEBSITE", "CHROME_SEARCH" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    if (service == null) {
                        CommandResult("Permission Required", "Accessibility Service is disabled. Please enable it in Settings.")
                    } else {
                        com.aimobile.accessibility.automation.AppAutomations.runChromeAutomation(
                            service,
                            context,
                            request.message ?: "google.com"
                        )
                    }
                }
                
                "INCREASE_VOLUME" -> volumeHandler.increaseVolume()
                "DECREASE_VOLUME" -> volumeHandler.decreaseVolume()
                "MUTE_VOLUME", "MUTE_PHONE" -> volumeHandler.muteVolume()
                
                "SET_ALARM" -> alarmHandler.setAlarm(request.hour ?: 0, request.minute ?: 0)
                "START_TIMER", "SET_TIMER" -> alarmHandler.setTimer((request.minute ?: 0) * 60)
                
                "BATTERY_STATUS" -> deviceInfoHandler.getBatteryStatus()
                "NETWORK_STATUS", "NETWORK_STATUS_CHECK" -> deviceInfoHandler.getNetworkStatus()
                "DEVICE_INFO" -> deviceInfoHandler.getBatteryStatus() // fallback to basic status
                
                "CALL_NUMBER", "CALL_CONTACT" -> {
                    val targetNum = request.number ?: request.message
                    if (targetNum != null) callHandler.callNumber(targetNum)
                    else CommandResult("Failed", "No contact or number provided")
                }
                
                "SEND_SMS" -> {
                    val targetNum = request.number
                    val smsMsg = request.message
                    if (targetNum != null && smsMsg != null) {
                        smsHandler.sendSMS(targetNum, smsMsg)
                    } else {
                        CommandResult("Failed", "Number or message missing")
                    }
                }
                
                else -> CommandResult(status = "Unsupported", message = "Unknown intent: ${request.intent}")
            }
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Router error: ${e.message}")
        }
    }
}
