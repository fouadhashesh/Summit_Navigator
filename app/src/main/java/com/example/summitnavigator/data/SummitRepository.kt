package com.example.summitnavigator.data

import com.example.summitnavigator.data.local.SessionDao
import com.example.summitnavigator.data.local.SessionEntity
import com.example.summitnavigator.data.local.SpeakerDao
import com.example.summitnavigator.data.local.SpeakerEntity
import com.example.summitnavigator.data.model.Session
import com.example.summitnavigator.data.model.Speaker
import com.example.summitnavigator.data.remote.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SummitRepository(
    private val firestoreDataSource: FirestoreDataSource,
    private val sessionDao: SessionDao,
    private val speakerDao: SpeakerDao
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                firestoreDataSource.getSessionsFlow().collect { remoteSessions ->
                    val localSessions = sessionDao.getAllSessions().firstOrNull() ?: emptyList()
                    val localBookmarkMap = localSessions.associateBy({ it.sessionId }, { it.isBookmarked })
                    
                    val entities = remoteSessions.map { session ->
                        SessionEntity(
                            sessionId = session.sessionId,
                            speakerOwnerId = session.speakerOwnerId,
                            title = session.title,
                            roomLocation = session.roomLocation,
                            timestamp = session.timestamp,
                            isVipOnly = session.isVipOnly,
                            isBookmarked = localBookmarkMap[session.sessionId] ?: false
                        )
                    }
                    sessionDao.insertSessions(entities)
                }
            }
            launch {
                firestoreDataSource.getSpeakersFlow().collect { remoteSpeakers ->
                    val entities = remoteSpeakers.map { speaker ->
                        SpeakerEntity(
                            speakerId = speaker.speakerId,
                            name = speaker.name,
                            biography = speaker.biography,
                            company = speaker.company
                        )
                    }
                    speakerDao.insertSpeakers(entities)
                }
            }
        }
    }

    val fullSchedule: Flow<List<Session>> = sessionDao.getAllSessions().map { entities -> 
        entities.map { it.toDomainModel() } 
    }
    
    fun searchSchedule(query: String): Flow<List<Session>> = sessionDao.searchSessions(query).map { entities ->
        entities.map { it.toDomainModel() }
    }
    
    val bookmarkedSchedule: Flow<List<Session>> = sessionDao.getBookmarkedSessions().map { entities -> 
        entities.map { it.toDomainModel() } 
    }

    fun getSessionById(sessionId: String): Flow<Session?> = sessionDao.getSessionById(sessionId).map { it?.toDomainModel() }
    
    fun getSpeakerById(speakerId: String): Flow<Speaker?> = speakerDao.getSpeakerById(speakerId).map { it?.toDomainModel() }

    fun getAllSpeakers(): Flow<List<Speaker>> = speakerDao.getAllSpeakers().map { entities -> 
        entities.map { it.toDomainModel() } 
    }

    fun getSessionsForSpeaker(speakerId: String): Flow<List<Session>> = sessionDao.getAllSessions().map { entities ->
        entities.filter { it.speakerOwnerId == speakerId }.map { it.toDomainModel() }
    }

    suspend fun updateSpeaker(speaker: Speaker) = firestoreDataSource.updateSpeaker(speaker)

    suspend fun toggleBookmark(sessionId: String, userId: String, isBookmarked: Boolean) {
        sessionDao.updateBookmarkStatus(sessionId, isBookmarked)
        firestoreDataSource.toggleBookmark(sessionId, userId, isBookmarked)
    }
    
    suspend fun getUserRole(userId: String): String = firestoreDataSource.getUserRole(userId)
    
    suspend fun clearLocalBookmarks() {
        sessionDao.clearAllBookmarks()
    }
    
    suspend fun syncUserBookmarks(userId: String) {
        sessionDao.clearAllBookmarks()
        val bookmarks = firestoreDataSource.getUserBookmarks(userId)
        bookmarks.forEach { sessionId ->
            sessionDao.updateBookmarkStatus(sessionId, true)
        }
    }
    
    suspend fun createSession(session: Session) = firestoreDataSource.addSession(session)
    suspend fun updateSession(session: Session) = firestoreDataSource.updateSession(session)
    suspend fun deleteSession(sessionId: String) = firestoreDataSource.deleteSession(sessionId)
    
    private fun SessionEntity.toDomainModel() = Session(
        sessionId = sessionId,
        speakerOwnerId = speakerOwnerId,
        title = title,
        roomLocation = roomLocation,
        timestamp = timestamp,
        isVipOnly = isVipOnly,
        isBookmarked = isBookmarked
    )

    private fun SpeakerEntity.toDomainModel() = Speaker(
        speakerId = speakerId,
        name = name,
        biography = biography,
        company = company
    )
}
