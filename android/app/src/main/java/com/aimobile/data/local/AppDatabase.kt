package com.aimobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DeviceInfoEntity::class, SettingsEntity::class, CommandHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
