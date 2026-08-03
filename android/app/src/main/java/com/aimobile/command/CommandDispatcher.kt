package com.aimobile.command

import android.content.Context
import android.util.Log
import com.aimobile.models.CommandRequest
import com.aimobile.models.CommandResult
import com.aimobile.router.IntentRouter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandDispatcher @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val intentRouter = IntentRouter(context)
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun dispatchCommand(jsonString: String, onResult: (CommandResult) -> Unit) {
        scope.launch {
            try {
                Log.d("CommandDispatcher", "Received JSON: \$jsonString")
                val request = gson.fromJson(jsonString, CommandRequest::class.java)
                
                if (request != null && !request.intent.isNullOrEmpty()) {
                    if (request.intent == "MULTI_COMMAND" && request.commands != null) {
                        val results = mutableListOf<String>()
                        var anyFailed = false
                        for (cmd in request.commands) {
                            val res = intentRouter.route(cmd)
                            results.add("${cmd.intent}: ${res.status}")
                            if (res.status == "Failed") anyFailed = true
                            
                            // Delay to allow UI interactions (like Quick Settings) to settle before the next command
                            kotlinx.coroutines.delay(1500)
                        }
                        val finalStatus = if (anyFailed) "Partial/Failed" else "Success"
                        onResult(CommandResult(finalStatus, results.joinToString(" | ")))
                    } else {
                        val result = intentRouter.route(request)
                        onResult(result)
                    }
                } else {
                    onResult(CommandResult("Failed", "Invalid or missing intent field in JSON"))
                }
            } catch (e: Exception) {
                Log.e("CommandDispatcher", "Failed to parse JSON: \${e.message}")
                onResult(CommandResult("Failed", "JSON Parse Error: \${e.message}"))
            }
        }
    }
}
