package com.example.summitnavigator.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val role: String = "attendee", // attendee, vip, speaker, admin
    val bookmarkedSessions: List<String> = emptyList()
)
