package com.aimobile.managers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(@ApplicationContext private val context: Context) {
    // Stub for runtime permission checking logic
    fun checkPermissions(): Boolean {
        return true
    }
}
