package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionBonusResolverTest {

    private val store = InMemorySessionBonusStore()
    private val resolver = SessionBonusResolver(store) { "2026-09-08" }

    @Test
    fun firstQualifyingSession_isNewAwardAndPersisted() {
        val snapshot = HealthSnapshot(sleepMinutes = 480, steps = 10_000, stepGoal = 10_000)
        val result = resolver.resolve("w1", snapshot)
        assertTrue(result.isNewAward)
        assertEquals(
            listOf(BonusKind.GOOD_SLEEP, BonusKind.STEP_GOAL),
            result.bonuses.map { it.kind },
        )
        assertEquals(result.bonuses, store.loadAward("w1"))
        assertEquals(
            setOf(BonusKind.GOOD_SLEEP, BonusKind.STEP_GOAL),
            store.claimedKinds("2026-09-08"),
        )
    }

    @Test
    fun secondOpenOfSameWorkout_doesNotReAward() {
        val snapshot = HealthSnapshot(sleepMinutes = 480)
        resolver.resolve("w1", snapshot)
        val again = resolver.resolve("w1", snapshot)
        assertFalse(again.isNewAward)
        assertEquals(listOf(BonusKind.GOOD_SLEEP), again.bonuses.map { it.kind })
    }

    @Test
    fun secondWorkoutSameDay_doesNotRepeatClaimedKinds() {
        val snapshot = HealthSnapshot(sleepMinutes = 480, steps = 10_000, stepGoal = 10_000)
        resolver.resolve("w1", snapshot)
        val second = resolver.resolve("w2", snapshot)
        assertFalse(second.isNewAward)
        assertTrue(second.bonuses.isEmpty())
        assertEquals(null, store.loadAward("w2"))
    }

    @Test
    fun secondWorkoutCanClaimRemainingKinds() {
        resolver.resolve("w1", HealthSnapshot(sleepMinutes = 480))
        val second = resolver.resolve(
            "w2",
            HealthSnapshot(sleepMinutes = 480, steps = 10_000, stepGoal = 10_000),
        )
        assertTrue(second.isNewAward)
        assertEquals(listOf(BonusKind.STEP_GOAL), second.bonuses.map { it.kind })
    }

    @Test
    fun emptyMetrics_notPersistedSoLaterDataCanGrant() {
        val empty = resolver.resolve("w1", HealthSnapshot.EMPTY)
        assertFalse(empty.isNewAward)
        assertEquals(null, store.loadAward("w1"))

        val later = resolver.resolve("w1", HealthSnapshot(hadRunningSession = true))
        assertTrue(later.isNewAward)
        assertEquals(listOf(BonusKind.RUNNING), later.bonuses.map { it.kind })
    }

    @Test
    fun blankWorkoutId_neverAwards() {
        val result = resolver.resolve("", HealthSnapshot(sleepMinutes = 500))
        assertFalse(result.isNewAward)
        assertTrue(result.bonuses.isEmpty())
    }

    @Test
    fun encodeDecodeRoundTrip() {
        val original = listOf(
            SessionBonus(BonusKind.GOOD_SLEEP, 25, 5),
            SessionBonus(BonusKind.STEP_GOAL, 20, 5),
        )
        val encoded = PrefsSessionBonusStore.encodeBonuses(original)
        assertEquals(original, PrefsSessionBonusStore.decodeBonuses(encoded))
    }
}

private class InMemorySessionBonusStore : SessionBonusStore {
    private val awards = mutableMapOf<String, List<SessionBonus>>()
    private val claimed = mutableMapOf<String, MutableSet<BonusKind>>()

    override fun loadAward(workoutId: String): List<SessionBonus>? = awards[workoutId]

    override fun saveAward(workoutId: String, bonuses: List<SessionBonus>) {
        awards[workoutId] = bonuses
    }

    override fun claimedKinds(day: String): Set<BonusKind> =
        claimed[day]?.toSet() ?: emptySet()

    override fun claimKinds(day: String, kinds: Collection<BonusKind>) {
        claimed.getOrPut(day) { mutableSetOf() }.addAll(kinds)
    }
}
