package com.pixelfitquest.feature.achievements.unlock

import com.pixelfitquest.feature.achievements.model.AchievementCatalog
import com.pixelfitquest.feature.achievements.model.AchievementDefinition
import com.pixelfitquest.feature.achievements.model.AchievementEvaluation
import com.pixelfitquest.feature.achievements.model.AchievementMetric
import com.pixelfitquest.feature.achievements.model.AchievementProgress
import com.pixelfitquest.feature.achievements.model.LocalFitnessSnapshot
import kotlin.math.max

object AchievementUnlocker {
    fun evaluate(
        existing: Map<String, AchievementProgress>,
        snapshot: LocalFitnessSnapshot,
        nowEpochMs: Long,
        definitions: List<AchievementDefinition> = AchievementCatalog.all,
    ): AchievementEvaluation {
        val newlyUnlocked = mutableListOf<String>()
        val updated = definitions.map { definition ->
            val previous = existing[definition.id]
            val snapshotValue = snapshot.valueOf(definition.metric)
            val currentValue = when (definition.metric) {
                AchievementMetric.CURRENT_STREAK,
                AchievementMetric.LEVEL_REACHED,
                -> snapshotValue
                else -> max(previous?.currentValue ?: 0L, snapshotValue)
            }
            val alreadyUnlockedAt = previous?.unlockedAtEpochMs
            val unlockedAt = alreadyUnlockedAt
                ?: if (currentValue >= definition.threshold) nowEpochMs else null
            if (alreadyUnlockedAt == null && unlockedAt != null) {
                newlyUnlocked += definition.id
            }
            AchievementProgress(
                achievementId = definition.id,
                currentValue = currentValue,
                unlockedAtEpochMs = unlockedAt,
                rewardGranted = previous?.rewardGranted == true,
            )
        }
        return AchievementEvaluation(
            updated = updated,
            newlyUnlockedIds = newlyUnlocked,
        )
    }
}
