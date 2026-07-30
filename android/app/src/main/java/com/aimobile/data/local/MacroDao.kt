package com.aimobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY createdAt DESC")
    suspend fun getAllMacros(): List<MacroEntity>

    @Query("SELECT * FROM macros WHERE LOWER(triggerPhrase) = LOWER(:triggerPhrase) LIMIT 1")
    suspend fun findMacroByTrigger(triggerPhrase: String): MacroEntity?

    @Query("SELECT * FROM macros WHERE LOWER(name) LIKE :query OR LOWER(triggerPhrase) LIKE :query LIMIT 1")
    suspend fun searchMacro(query: String): MacroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacro(id: Int)
}
