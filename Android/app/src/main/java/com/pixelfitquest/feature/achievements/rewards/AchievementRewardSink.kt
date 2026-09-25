package com.pixelfitquest.feature.achievements.rewards

import android.util.Log
import com.pixelfitquest.feature.achievements.model.AchievementReward
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.local.LocalPixelFitStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies coins and XP directly to the local wallet and profile when an achievement unlocks.
 */
fun interface AchievementRewardSink {
    suspend fun grant(achievementId: String, reward: AchievementReward): Boolean
}

@Singleton
class DeferredCoinsXpRewardSink @Inject constructor(
    private val localStore: LocalPixelFitStore,
    private val localXpPort: LocalXpPort,
) : AchievementRewardSink {
    override suspend fun grant(achievementId: String, reward: AchievementReward): Boolean {
        return try {
            if (reward.xp > 0) {
                localXpPort.awardXp(reward.xp, "achievement_$achievementId")
            }
            if (reward.coins > 0) {
                val current = (localStore.getUserField("coins") as? Number)?.toInt() ?: 0
                localStore.updateUserData(mapOf("coins" to current + reward.coins))
            }
            true
        } catch (e: Exception) {
            Log.e("AchievementReward", "Failed to grant reward for $achievementId", e)
            false
        }
    }
}
