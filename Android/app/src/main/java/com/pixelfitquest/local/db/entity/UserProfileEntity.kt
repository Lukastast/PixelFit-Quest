package com.pixelfitquest.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.firebase.model.UserData

const val LOCAL_PROFILE_ID = "local"

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = LOCAL_PROFILE_ID,
    val height: Int = 178,
    val armLength: Float? = null,
    val musicVolume: Int = 50,
    val level: Int = 1,
    val coins: Int = 0,
    val exp: Int = 0,
    val streak: Int = 0,
    val lastActivityDate: String = "",
    val lastStepsRewardDate: String = "",
    val lastSleepRewardDate: String = "",
    val lastWeeklyHeartRewardWeek: String = "",
    val lastStreakUpdateDate: String = "",
    val characterGender: String = "male",
    val characterVariant: String = "basic",
    val unlockedVariantsCsv: String = "basic",
    val equippedHomeUpgrade: String = "",
    val unlockedHomeUpgradesCsv: String = "",
) {
    fun toUserData(): UserData = UserData(
        height = height,
        armLength = armLength,
        musicVolume = musicVolume,
        level = level,
        coins = coins,
        exp = exp,
        streak = streak,
    )

    fun toCharacter(): CharacterData = CharacterData(
        gender = characterGender,
        variant = characterVariant,
        unlockedVariants = unlockedVariantsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("basic") },
        equippedHomeUpgrade = equippedHomeUpgrade.takeIf { it.isNotBlank() },
        unlockedHomeUpgrades = unlockedHomeUpgradesCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
    )

    companion object {
        fun default(): UserProfileEntity = UserProfileEntity()

        fun variantsCsv(variants: List<String>): String =
            variants.filter { it.isNotBlank() }.distinct().joinToString(",")
                .ifBlank { "basic" }
    }
}
