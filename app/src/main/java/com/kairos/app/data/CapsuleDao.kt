package com.devsoftware.kairos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsuleDao {
    @Query("SELECT * FROM capsules ORDER BY unlockAtEpochMillis ASC")
    fun observeAll(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CapsuleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(capsule: CapsuleEntity)

    @Query("DELETE FROM capsules WHERE id = :id")
    suspend fun deleteById(id: String)
}
