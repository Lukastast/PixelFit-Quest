package com.pixelfitquest.feature.home.model

import com.pixelfitquest.R
import com.pixelfitquest.feature.customization.CustomizationViewModel
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.local.db.entity.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeGymUpgradeTest {

    @Test
    fun gymDwellingTier_hasCorrectMetadata() {
        assertEquals("dwelling_gym", DwellingTier.GYM.id)
        assertEquals("Iron Gym", DwellingTier.GYM.displayName)
        assertEquals(70, DwellingTier.GYM.minLevel)
        assertEquals(2000, DwellingTier.GYM.coinPrice)
        assertEquals(null, DwellingTier.GYM.legacyFreeLevel)
    }

    @Test
    fun gymDwellingTier_notGrantedByLegacyMigration() {
        for (lvl in 1..100) {
            assertFalse(DwellingTier.legacyGrantedIds(lvl).contains(DwellingTier.GYM.id))
        }
    }

    @Test
    fun gymDwellingVisuals_mapsToLandscapeGymAsset() {
        assertEquals(R.drawable.dwelling_gym_landscape, DwellingVisuals.landscapeBgRes(DwellingTier.GYM))
        assertEquals(R.drawable.dwelling_gym_landscape, DwellingVisuals.portraitBgRes(DwellingTier.GYM))
        org.junit.Assert.assertNull(DwellingVisuals.furnitureRes(DwellingTier.GYM, isLandscape = true))
    }

    @Test
    fun gymDwellingVisuals_alignsPosesWithEquipment() {
        val stand = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.STANDING, isLandscape = true)
        val sit = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.SITTING, isLandscape = true)
        val lie = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.LYING, isLandscape = true)

        // Standing anchor is on the gym floor
        assertEquals(0.50f, stand.first, 0.01f)
        assertEquals(0.88f, stand.second, 0.01f)

        // Sitting anchor is on top of the bench cushion on the right
        assertEquals(0.64f, sit.first, 0.01f)
        assertEquals(0.66f, sit.second, 0.01f)

        // Lying/resting anchor is on the new bed on the left
        assertEquals(0.20f, lie.first, 0.01f)
        assertEquals(0.72f, lie.second, 0.01f)

        assertNotEquals(stand, sit)
    }

    @Test
    fun homeScreenDwellingSelection_prioritizesEquippedGymUpgrade() {
        // When equippedHomeUpgrade == "dwelling_gym", Home Screen displays DwellingTier.GYM
        val characterWithGym = CharacterData(
            equippedHomeUpgrade = CustomizationViewModel.GYM_UPGRADE_ID,
            unlockedHomeUpgrades = listOf(CustomizationViewModel.GYM_UPGRADE_ID),
        )
        val selectedTier = DwellingTier.resolve(
            characterWithGym.equippedHomeUpgrade,
            characterWithGym.unlockedHomeUpgrades,
        )
        assertEquals(DwellingTier.GYM, selectedTier)

        val characterWithoutGym = CharacterData(equippedHomeUpgrade = null)
        val defaultTier = DwellingTier.resolve(
            characterWithoutGym.equippedHomeUpgrade,
            characterWithoutGym.unlockedHomeUpgrades,
        )
        assertEquals(DwellingTier.TARP, defaultTier)
    }

    @Test
    fun userProfileEntity_persistsAndRestoresHomeUpgrade() {
        val entity = UserProfileEntity(
            equippedHomeUpgrade = CustomizationViewModel.GYM_UPGRADE_ID,
            unlockedHomeUpgradesCsv = "${CustomizationViewModel.GYM_UPGRADE_ID},other_upgrade",
        )
        val character = entity.toCharacter()
        assertEquals(CustomizationViewModel.GYM_UPGRADE_ID, character.equippedHomeUpgrade)
        assertTrue(character.unlockedHomeUpgrades.contains(CustomizationViewModel.GYM_UPGRADE_ID))
        assertTrue(character.unlockedHomeUpgrades.contains("other_upgrade"))
    }

    @Test
    fun customizationViewModelConstants_valid() {
        assertEquals("dwelling_gym", CustomizationViewModel.GYM_UPGRADE_ID)
        assertTrue(CustomizationViewModel.GYM_UPGRADE_PRICE > 0)
    }
}
