package com.pixelfitquest.local

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSyncPolicyTest {
    @Test
    fun stubKeepsCloudSyncAndLeaderboardsOff() {
        val policy = StubCloudSyncPolicy()
        assertFalse(policy.isCloudSyncEnabled())
        assertFalse(policy.isLeaderboardEnabled())
    }

    @Test
    fun cloudBackupStubFailsClosedForFreeTier() = runBlocking {
        val backup = CloudBackup(StubCloudSyncPolicy())
        assertFalse(backup.isAvailable())
        val result = backup.syncNow()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ProRequiredException)
    }
}
