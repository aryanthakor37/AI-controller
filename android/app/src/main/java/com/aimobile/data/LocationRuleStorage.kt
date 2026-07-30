package com.aimobile.data

import android.content.Context
import android.content.SharedPreferences
import com.aimobile.models.LocationRule
import org.json.JSONArray
import org.json.JSONObject

class LocationRuleStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("location_rules_prefs", Context.MODE_PRIVATE)

    fun saveRules(rules: List<LocationRule>) {
        val jsonArray = JSONArray()
        rules.forEach { rule ->
            val obj = JSONObject().apply {
                put("id", rule.id)
                put("name", rule.name)
                put("latitude", rule.latitude)
                put("longitude", rule.longitude)
                put("radiusMeters", rule.radiusMeters.toDouble())
                put("enterAction", rule.enterAction)
                put("exitAction", rule.exitAction)
                put("isEnabled", rule.isEnabled)
                put("isInside", rule.isInside)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_rules", jsonArray.toString()).apply()
    }

    fun loadRules(): List<LocationRule> {
        val jsonStr = prefs.getString("saved_rules", null) ?: return defaultRules()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<LocationRule>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    LocationRule(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", "Custom Location"),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        radiusMeters = obj.optDouble("radiusMeters", 150.0).toFloat(),
                        enterAction = obj.optString("enterAction", "SILENT"),
                        exitAction = obj.optString("exitAction", "NORMAL_SOUND"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        isInside = obj.optBoolean("isInside", false)
                    )
                )
            }
            if (list.isEmpty()) defaultRules() else list
        } catch (e: Exception) {
            defaultRules()
        }
    }

    private fun defaultRules(): List<LocationRule> {
        return listOf(
            LocationRule(
                id = "default_1",
                name = "My Main Location",
                latitude = 23.0225,
                longitude = 72.5714,
                radiusMeters = 150f,
                enterAction = "SILENT",
                exitAction = "NORMAL_SOUND",
                isEnabled = true
            )
        )
    }
}
