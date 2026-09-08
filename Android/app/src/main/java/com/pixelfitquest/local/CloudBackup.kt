package com.pixelfitquest.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pro-only Firebase/Firestore backup + multi-device sync.
 *
 * Free users never hit this path. When Play Billing is wired, flip
 * [CloudSyncPolicy] and implement Room <-> Firestore here.
 */
@Singleton
class CloudBackup @Inject constructor(
    private val policy: CloudSyncPolicy,
) {
    fun isAvailable(): Boolean = policy.isCloudSyncEnabled()

    suspend fun syncNow(): Result<Unit> {
        if (!isAvailable()) {
            return Result.failure(ProRequiredException())
        }
        // TODO(pro): push/pull Room <-> Firestore when billing entitlements exist.
        return Result.failure(
            NotImplementedError("Pro cloud sync is stubbed until Play Billing")
        )
    }
}
