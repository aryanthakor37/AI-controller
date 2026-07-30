package com.aimobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerPhrase: String,
    val createdAt: Long = System.currentTimeMillis(),
    val stepsJson: String
)

data class MacroStep(
    val stepOrder: Int? = 0,
    val packageName: String? = "",
    val actionType: String? = "", // CLICK_ID, CLICK_TEXT, CLICK_DESC, INPUT_TEXT, SCROLL_DOWN, SCROLL_UP, SLEEP
    val targetValue: String? = "",
    val delayMs: Long? = 800L
)
