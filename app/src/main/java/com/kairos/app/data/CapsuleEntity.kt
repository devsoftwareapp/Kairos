package com.devsoftware.kairos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capsules")
data class CapsuleEntity(
    @PrimaryKey val id: String,
    val encryptedTitle: ByteArray,
    val encryptedPayloadPath: String?,
    val payloadType: String,
    val sealedAtEpochMillis: Long,
    val unlockAtEpochMillis: Long,
    val wrappedContentKey: ByteArray,
    val schemaVersion: Int = 1
)
