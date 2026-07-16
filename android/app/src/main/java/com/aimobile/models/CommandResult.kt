package com.aimobile.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the result of a command execution.
 */
data class CommandResult(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("data")
    val data: String? = null
)
