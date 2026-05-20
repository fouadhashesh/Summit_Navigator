package com.example.summitnavigator.data.model

import com.google.firebase.firestore.DocumentId

data class Speaker(
    @DocumentId
    val speakerId: String = "",
    val name: String = "",
    val biography: String = "",
    val company: String = ""
)
