package com.example.summitnavigator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp ASC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :id")
    fun getSessionById(id: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE title LIKE '%' || :searchQuery || '%' ORDER BY timestamp ASC")
    fun searchSessions(searchQuery: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE isBookmarked = 1 ORDER BY timestamp ASC")
    fun getBookmarkedSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Query("UPDATE sessions SET isBookmarked = :isBookmarked WHERE sessionId = :sessionId")
    suspend fun updateBookmarkStatus(sessionId: String, isBookmarked: Boolean)
    
    @Query("UPDATE sessions SET isBookmarked = 0")
    suspend fun clearAllBookmarks()
    
    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
