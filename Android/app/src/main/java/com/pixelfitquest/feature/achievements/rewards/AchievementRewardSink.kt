package com.pixelfitquest.feature.achievements.rewards

import com.pixelfitquest.feature.achievements.model.AchievementReward
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies coins/XP when an achievement unlocks.
 *
 * Default implementation is a no-op that returns false so
 * [com.pixelfitquest.feature.achievements.model.AchievementProgress.rewardGranted]
 * stays false until a local wallet is bound. Must not call Firebase.
 */
fun interface AchievementRewardSink {
    suspend fun grant(achievementId: String, reward: AchievementReward): Boolean
}

@Singleton
class DeferredCoinsXpRewardSink @Inject constructor() : AchievementRewardSink {
    override suspend fun grant(achievementId: String, reward: AchievementReward): Boolean = false
}
