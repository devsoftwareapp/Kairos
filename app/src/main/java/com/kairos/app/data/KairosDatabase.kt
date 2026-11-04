package com.kairos.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CapsuleEntity::class], version = 1, exportSchema = true)
abstract class KairosDatabase : RoomDatabase() {
    abstract fun capsuleDao(): CapsuleDao
}
