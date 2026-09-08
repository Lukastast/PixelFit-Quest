package com.pixelfitquest.local

/**
 * Product rule (#122): Firebase cloud sync, automatic backup, multi-device,
 * and leaderboards are PixelFit Pro. Free/default is local-only (Room).
 *
 * Play Billing is not wired yet, so this stub keeps cloud features off.
 */
interface CloudSyncPolicy {
    fun isCloudSyncEnabled(): Boolean
    fun isLeaderboardEnabled(): Boolean
}

class StubCloudSyncPolicy : CloudSyncPolicy {
    override fun isCloudSyncEnabled(): Boolean = false
    override fun isLeaderboardEnabled(): Boolean = false
}

class ProRequiredException : IllegalStateException(
    "Cloud backup, multi-device sync, and leaderboards require PixelFit Pro"
)
