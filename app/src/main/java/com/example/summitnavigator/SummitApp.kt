// AI-assisted
package com.example.summitnavigator

import android.app.Application
import androidx.room.Room
import com.example.summitnavigator.data.SummitRepository
import com.example.summitnavigator.data.local.SummitDatabase
import com.example.summitnavigator.data.remote.FirestoreDataSource

class SummitApp : Application() {

    // Lazy instantiation of Repository
    val repository: SummitRepository by lazy {
        val database = Room.databaseBuilder(
            this,
            SummitDatabase::class.java,
            "summit_database"
        ).fallbackToDestructiveMigration()
        .build()
        val firestoreDataSource = FirestoreDataSource()
        SummitRepository(firestoreDataSource, database.sessionDao(), database.speakerDao())
    }
}
