package com.aimobile.utils

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecurePreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("aimobile_secure_prefs", Context.MODE_PRIVATE)
    private val keySpec: SecretKeySpec

    init {
        val packageName = context.packageName
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(packageName.toByteArray(Charsets.UTF_8))
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    fun saveSecureString(key: String, value: String) {
        try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val base64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            prefs.edit().putString(key, base64).apply()
        } catch (e: Exception) {
            AiLogger.logError("SecurePrefs", "Failed to encrypt key $key", e)
            prefs.edit().putString(key, value).apply()
        }
    }

    fun getSecureString(key: String, defaultValue: String? = null): String? {
        val raw = prefs.getString(key, null) ?: return defaultValue
        return try {
            val decoded = Base64.decode(raw, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            String(cipher.doFinal(decoded), Charsets.UTF_8)
        } catch (_: Exception) {
            raw
        }
    }

    fun removeKey(key: String) {
        prefs.edit().remove(key).apply()
    }
}
