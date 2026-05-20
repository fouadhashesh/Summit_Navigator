package com.example.summitnavigator.data.model

import com.google.firebase.firestore.DocumentId

data class Session(
    @DocumentId
    val sessionId: String = "",
    val speakerOwnerId: String = "",
    val title: String = "",
    val roomLocation: String = "",
    val timestamp: Long = 0L,
    val bookmarkedBy: List<String> = emptyList(),
    val isVipOnly: Boolean = false,
    val isBookmarked: Boolean = false
)
