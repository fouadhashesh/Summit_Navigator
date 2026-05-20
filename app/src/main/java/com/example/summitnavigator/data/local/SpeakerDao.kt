package com.example.summitnavigator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeakerDao {
    @Query("SELECT * FROM speakers ORDER BY name ASC")
    fun getAllSpeakers(): Flow<List<SpeakerEntity>>

    @Query("SELECT * FROM speakers WHERE speakerId = :id")
    fun getSpeakerById(id: String): Flow<SpeakerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakers(speakers: List<SpeakerEntity>)

    @Query("DELETE FROM speakers")
    suspend fun clearAll()
}
