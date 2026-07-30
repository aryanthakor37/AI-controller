package com.aimobile.data

import android.content.Context
import com.aimobile.models.CommandRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OfflineCommandQueue(context: Context) {

    private val prefs = context.getSharedPreferences("aimobile_offline_queue", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "pending_commands"

    @Synchronized
    fun enqueue(request: CommandRequest) {
        val current = getAll()
        current.add(request)
        saveAll(current)
    }

    @Synchronized
    fun getAll(): MutableList<CommandRequest> {
        val json = prefs.getString(key, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<CommandRequest>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(key).apply()
    }

    private fun saveAll(list: List<CommandRequest>) {
        val json = gson.toJson(list)
        prefs.edit().putString(key, json).apply()
    }
}
