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
    
    @SerializedName("time")
    val time: String? = null,
    
    @SerializedName("duration")
    val duration: Int? = null,
    
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("app")
    val app: String? = null,

    @SerializedName("origin")
    val origin: String? = null,

    @SerializedName("destination")
    val destination: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("repeat")
    val repeat: String? = null,

    @SerializedName("contact")
    val contact: String? = null,

    @SerializedName("steps")
    val steps: List<com.aimobile.data.local.MacroStep>? = null,

    @SerializedName("commands")
    val commands: List<CommandRequest>? = null
)
