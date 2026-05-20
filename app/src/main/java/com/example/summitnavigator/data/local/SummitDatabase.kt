package com.example.summitnavigator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class, SpeakerEntity::class], version = 3, exportSchema = false)
abstract class SummitDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun speakerDao(): SpeakerDao
}
