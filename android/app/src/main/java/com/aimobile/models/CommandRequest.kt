package com.aimobile.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a command sent from the backend to the Android device.
 * For example: {"intent":"OPEN_CHROME"} or {"intent":"CALL_NUMBER", "number":"+911234567890"}
 */
data class CommandRequest(
    @SerializedName("intent")
    val intent: String,
    
    @SerializedName("number")
    val number: String? = null,
    
    @SerializedName("hour")
    val hour: Int? = null,
    
    @SerializedName("minute")
    val minute: Int? = null,
    
    @SerializedName("message")
    val message: String? = null
)
