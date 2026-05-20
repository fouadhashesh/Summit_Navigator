package com.example.summitnavigator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speakers")
data class SpeakerEntity(
    @PrimaryKey
    val speakerId: String,
    val name: String,
    val biography: String,
    val company: String
)
