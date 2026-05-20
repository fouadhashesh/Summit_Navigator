package com.example.summitnavigator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class], version = 2, exportSchema = false)
abstract class SummitDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
