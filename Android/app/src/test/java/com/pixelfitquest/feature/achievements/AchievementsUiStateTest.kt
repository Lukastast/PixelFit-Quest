package com.pixelfitquest.feature.achievements

import com.pixelfitquest.feature.achievements.model.AchievementCatalog
import com.pixelfitquest.feature.achievements.model.AchievementCategory
import com.pixelfitquest.feature.achievements.model.AchievementItem
import com.pixelfitquest.feature.achievements.model.AchievementProgress
import com.pixelfitquest.feature.achievements.model.AchievementStatusFilter
import com.pixelfitquest.feature.achievements.model.AchievementsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsUiStateTest {

    private val sampleItems: List<AchievementItem> = AchievementCatalog.all.mapIndexed { index, def ->
        AchievementItem(
            definition = def,
            progress = AchievementProgress(
                achievementId = def.id,
                currentValue = if (index % 2 == 0) def.threshold else def.threshold / 2,
                unlockedAtEpochMs = if (index % 2 == 0) 123456789L else null,
            ),
        )
    }

    @Test
    fun visibleItemsFiltersByCategoryCorrectly() {
        val state = AchievementsUiState(
            items = sampleItems,
            selectedCategory = AchievementCategory.WORKOUTS,
            statusFilter = AchievementStatusFilter.ALL,
        )

        assertTrue(state.visibleItems.isNotEmpty())
        assertTrue(state.visibleItems.all { it.definition.category == AchievementCategory.WORKOUTS })
    }

    @Test
    fun visibleItemsFiltersByStatusCorrectly() {
        val stateUnlocked = AchievementsUiState(
            items = sampleItems,
            selectedCategory = null,
            statusFilter = AchievementStatusFilter.UNLOCKED,
        )
        assertTrue(stateUnlocked.visibleItems.isNotEmpty())
        assertTrue(stateUnlocked.visibleItems.all { it.isUnlocked })

        val stateInProgress = AchievementsUiState(
            items = sampleItems,
            selectedCategory = null,
            statusFilter = AchievementStatusFilter.IN_PROGRESS,
        )
        assertTrue(stateInProgress.visibleItems.isNotEmpty())
        assertTrue(stateInProgress.visibleItems.all { !it.isUnlocked })
    }

    @Test
    fun selectedItemPrefersVisibleWhenSelectedIdNotInVisible() {
        // Find a workout item and a streak item
        val workoutItem = sampleItems.first { it.definition.category == AchievementCategory.WORKOUTS }
        val streakItem = sampleItems.first { it.definition.category == AchievementCategory.STREAK }

        // When selectedId is workoutItem, but category is filtered to STREAK:
        val state = AchievementsUiState(
            items = sampleItems,
            selectedCategory = AchievementCategory.STREAK,
            statusFilter = AchievementStatusFilter.ALL,
            selectedId = workoutItem.definition.id,
        )

        // selectedItem should fall back to first visible item in STREAK category
        assertNotNull(state.selectedItem)
        assertEquals(AchievementCategory.STREAK, state.selectedItem?.definition?.category)
    }

    @Test
    fun selectedItemHonorsSelectedIdWhenPresentInVisible() {
        val workoutItem = sampleItems.first { it.definition.category == AchievementCategory.WORKOUTS }

        val state = AchievementsUiState(
            items = sampleItems,
            selectedCategory = AchievementCategory.WORKOUTS,
            statusFilter = AchievementStatusFilter.ALL,
            selectedId = workoutItem.definition.id,
        )

        assertEquals(workoutItem.definition.id, state.selectedItem?.definition?.id)
    }

    @Test
    fun unlockedAndTotalCountsAccurate() {
        val state = AchievementsUiState(items = sampleItems)
        assertEquals(sampleItems.size, state.totalCount)
        assertEquals(sampleItems.count { it.isUnlocked }, state.unlockedCount)
    }

    @Test
    fun selectedItemShowsFirstUnlockedBeforeTapping() {
        val lockedFirst = AchievementItem(
            definition = AchievementCatalog.all[0],
            progress = AchievementProgress(
                achievementId = AchievementCatalog.all[0].id,
                currentValue = 0,
                unlockedAtEpochMs = null,
            ),
        )
        val unlockedSecond = AchievementItem(
            definition = AchievementCatalog.all[1],
            progress = AchievementProgress(
                achievementId = AchievementCatalog.all[1].id,
                currentValue = AchievementCatalog.all[1].threshold,
                unlockedAtEpochMs = 1000L,
            ),
        )

        val stateBeforeTap = AchievementsUiState(
            items = listOf(lockedFirst, unlockedSecond),
            selectedId = null,
        )

        // Before tapping: should show the first unlocked achievement
        assertEquals(unlockedSecond.definition.id, stateBeforeTap.selectedItem?.definition?.id)

        // After tapping the locked first achievement: should show the tapped achievement
        val stateAfterTap = stateBeforeTap.copy(selectedId = lockedFirst.definition.id)
        assertEquals(lockedFirst.definition.id, stateAfterTap.selectedItem?.definition?.id)
    }

    @Test
    fun selectedItemFallsBackToFirstWhenNoUnlockedBeforeTapping() {
        val lockedFirst = AchievementItem(
            definition = AchievementCatalog.all[0],
            progress = AchievementProgress(
                achievementId = AchievementCatalog.all[0].id,
                currentValue = 0,
                unlockedAtEpochMs = null,
            ),
        )
        val lockedSecond = AchievementItem(
            definition = AchievementCatalog.all[1],
            progress = AchievementProgress(
                achievementId = AchievementCatalog.all[1].id,
                currentValue = 0,
                unlockedAtEpochMs = null,
            ),
        )

        val state = AchievementsUiState(
            items = listOf(lockedFirst, lockedSecond),
            selectedId = null,
        )

        // If none unlocked, falls back to the first visible achievement
        assertEquals(lockedFirst.definition.id, state.selectedItem?.definition?.id)
    }
}
