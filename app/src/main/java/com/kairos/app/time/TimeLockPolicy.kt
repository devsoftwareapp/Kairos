package com.kairos.app.time

/**
 * UI-level policy placeholder only.
 * Device wall-clock checks are not a cryptographic time lock.
 * Integrate and audit a real threshold/time-lock protocol before claiming
 * that a capsule cannot be opened early.
 */
object TimeLockPolicy {
    fun isDue(unlockAtEpochMillis: Long, trustedNowEpochMillis: Long): Boolean =
        trustedNowEpochMillis >= unlockAtEpochMillis
}
