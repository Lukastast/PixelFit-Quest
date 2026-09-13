package com.pixelfitquest.feature.healthbonuses

import android.content.Context
import android.content.SharedPreferences
import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.SessionBonus

/**
 * Local, on-device record of which session extras were granted.
 * Phone is source of truth — not Firestore.
 */
interface SessionBonusStore {
    fun loadAward(workoutId: String): List<SessionBonus>?
    fun saveAward(workoutId: String, bonuses: List<SessionBonus>)
    fun claimedKinds(day: String): Set<BonusKind>
    fun claimKinds(day: String, kinds: Collection<BonusKind>)
}

class PrefsSessionBonusStore(
    private val prefs: SharedPreferences,
) : SessionBonusStore {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

    override fun loadAward(workoutId: String): List<SessionBonus>? {
        if (workoutId.isBlank()) return null
        val raw = prefs.getString(awardKey(workoutId), null) ?: return null
        return decodeBonuses(raw).takeIf { it.isNotEmpty() }
    }

    override fun saveAward(workoutId: String, bonuses: List<SessionBonus>) {
        if (workoutId.isBlank() || bonuses.isEmpty()) return
        prefs.edit().putString(awardKey(workoutId), encodeBonuses(bonuses)).apply()
    }

    override fun claimedKinds(day: String): Set<BonusKind> {
        val raw = prefs.getString(claimKey(day), null) ?: return emptySet()
        return raw.split(KIND_SEP)
            .mapNotNull { token -> parseKind(token) }
            .toSet()
    }

    override fun claimKinds(day: String, kinds: Collection<BonusKind>) {
        if (kinds.isEmpty()) return
        val merged = claimedKinds(day) + kinds
        val encoded = merged.joinToString(KIND_SEP) { it.name }
        prefs.edit().putString(claimKey(day), encoded).apply()
    }

    private fun awardKey(workoutId: String) = "award_$workoutId"
    private fun claimKey(day: String) = "claimed_$day"

    companion object {
        const val PREFS_NAME = "health_training_bonuses"
        private const val KIND_SEP = ","
        private const val BONUS_SEP = "|"
        private const val FIELD_SEP = ":"

        fun encodeBonuses(bonuses: List<SessionBonus>): String {
            return bonuses.joinToString(BONUS_SEP) { bonus ->
                "${bonus.kind.name}$FIELD_SEP${bonus.xp}$FIELD_SEP${bonus.coins}"
            }
        }

        fun decodeBonuses(raw: String): List<SessionBonus> {
            if (raw.isBlank()) return emptyList()
            return raw.split(BONUS_SEP).mapNotNull { token ->
                val parts = token.split(FIELD_SEP)
                if (parts.size != 3) return@mapNotNull null
                val kind = parseKind(parts[0]) ?: return@mapNotNull null
                val xp = parts[1].toIntOrNull() ?: return@mapNotNull null
                val coins = parts[2].toIntOrNull() ?: return@mapNotNull null
                SessionBonus(kind, xp, coins)
            }
        }

        fun parseKind(raw: String): BonusKind? {
            return try {
                BonusKind.valueOf(raw.trim())
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
