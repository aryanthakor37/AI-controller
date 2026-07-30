package com.aimobile.workers

import android.content.Context
import com.aimobile.data.OfflineCommandQueue
import com.aimobile.router.IntentRouter
import com.aimobile.utils.AiLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommandSyncWorker(private val context: Context) {

    fun performSync() {
        CoroutineScope(Dispatchers.IO).launch {
            AiLogger.logServiceStart("CommandSyncWorker")
            try {
                val queue = OfflineCommandQueue(context)
                val pendingCommands = queue.getAll()
                if (pendingCommands.isNotEmpty()) {
                    val router = IntentRouter(context)
                    for (cmd in pendingCommands) {
                        router.route(cmd)
                    }
                    queue.clear()
                    AiLogger.logCommandExecuted("FLUSH_OFFLINE_QUEUE", "Success", 0)
                }
            } catch (e: Exception) {
                AiLogger.logError("CommandSyncWorker", "Work execution failed", e)
            }
        }
    }
}
