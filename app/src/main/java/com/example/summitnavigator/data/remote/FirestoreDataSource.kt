package com.example.summitnavigator.data.remote

import com.example.summitnavigator.data.model.Session
import com.example.summitnavigator.data.model.Speaker
import com.example.summitnavigator.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()

    fun getSessionsFlow(): Flow<List<Session>> {
        return db.collection("sessions")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Session::class.java)
            }
    }

    suspend fun getSessionById(sessionId: String): Session? {
        return try {
            val snapshot = db.collection("sessions").document(sessionId).get().await()
            snapshot.toObject(Session::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSpeakerById(speakerId: String): Speaker? {
        return try {
            val snapshot = db.collection("speakers").document(speakerId).get().await()
            snapshot.toObject(Speaker::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserRole(userId: String): String {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                snapshot.getString("role") ?: "attendee"
            } else {
                db.collection("users").document(userId).set(User(uid = userId)).await()
                "attendee"
            }
        } catch (e: Exception) {
            "attendee"
        }
    }

    suspend fun getUserBookmarks(userId: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            user?.bookmarkedSessions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleBookmark(sessionId: String, userId: String, isBookmarked: Boolean) {
        val userRef = db.collection("users").document(userId)
        try {
            if (isBookmarked) {
                userRef.update("bookmarkedSessions", com.google.firebase.firestore.FieldValue.arrayUnion(sessionId)).await()
            } else {
                userRef.update("bookmarkedSessions", com.google.firebase.firestore.FieldValue.arrayRemove(sessionId)).await()
            }
        } catch (e: Exception) {
            // Document might not exist, ensure it does
            db.collection("users").document(userId).set(User(uid = userId, bookmarkedSessions = if (isBookmarked) listOf(sessionId) else emptyList())).await()
        }
    }

    suspend fun addSession(session: Session) {
        db.collection("sessions").document(session.sessionId).set(session).await()
    }

    suspend fun updateSession(session: Session) {
        db.collection("sessions").document(session.sessionId).set(session).await()
    }

    suspend fun deleteSession(sessionId: String) {
        db.collection("sessions").document(sessionId).delete().await()
    }
}
