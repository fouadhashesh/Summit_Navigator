package com.example.summitnavigator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val speakerOwnerId: String,
    val title: String,
    val roomLocation: String,
    val timestamp: Long,
    val isVipOnly: Boolean,
    val isBookmarked: Boolean
)
