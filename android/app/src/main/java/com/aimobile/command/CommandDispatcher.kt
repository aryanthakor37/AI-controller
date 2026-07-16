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

class CommandDispatcher(private val context: Context) {

    private val intentRouter = IntentRouter(context)
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun dispatchCommand(jsonString: String, onResult: (CommandResult) -> Unit) {
        scope.launch {
            try {
                Log.d("CommandDispatcher", "Received JSON: \$jsonString")
                val request = gson.fromJson(jsonString, CommandRequest::class.java)
                
                if (request != null && !request.intent.isNullOrEmpty()) {
                    val result = intentRouter.route(request)
                    onResult(result)
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
