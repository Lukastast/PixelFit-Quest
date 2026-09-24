package com.pixelfitquest.ui.theme

import androidx.compose.ui.graphics.Color

// 16-bit SNES Tier & Quest Center Palettes
val BronzeCopper = Color(0xFFE0884C)   // Warm copper highlight
val BronzeRust = Color(0xFFB85A28)     // Deep rust / leather
val SilverSteel = Color(0xFFD8E4F0)    // Polished moon silver
val SilverSlate = Color(0xFF8CA0B4)    // Cool chainmail slate
val SlateDeep = Color(0xFF1C2430)      // Midnight slate shadow (primary background)
val SlateSurface = Color(0xFF243040)   // Elevated cool slate (cards, unlocked items, tiles)
val SlateGroove = Color(0xFF121822)    // Sunken dark slate (progress grooves, icon recesses)
val SlateBorder = Color(0xFF384A5C)    // Crisp blue-slate bevel border
val SlateBorderSubtle = Color(0xFF2A3648) // Subtle card border
val ImperialGold = Color(0xFFFFCC28)   // Radiant quest gold
val TorchAmber = Color(0xFFDC9410)     // Warm torch flame
val EmberOrange = Color(0xFFFF7814)    // Fiery ember accent
val PlatinumWhite = Color(0xFFF2F6EB)  // Gleaming white-gold
val CrystalCyan = Color(0xFF70E0F8)    // Luminous crystal blue
val NightIndigo = Color(0xFF1E1A38)    // Celestial midnight indigo
val HeartRuby = Color(0xFFE84242)      // Vital cardio ruby red
val SleepAmethyst = Color(0xFF8A6CD8)  // Restful sleep purple

// Slate-mapped aliases (eliminates all brown from cards, borders, and grooves)
val LeatherDark = SlateGroove          // Color(0xFF121822) - Sunken dark slate recess
val ParchmentDark = SlateSurface       // Color(0xFF243040) - Elevated cool slate surface
val ParchmentBorder = SlateBorder      // Color(0xFF384A5C) - Blue-slate bevel border
val DarkStone = SlateDeep              // Color(0xFF1C2430) - Midnight slate shadow
val QuestBrown = SlateBorder           // Color(0xFF384A5C) - Blue-slate border
val VitalGreen = Color(0xFF3FA34D)     // Health, vitality, energy
val FireOrange = TorchAmber            // Color(0xFFDC9410) - Warm torch flame / XP / progress
val RewardGold = ImperialGold          // Color(0xFFFFCC28) - Radiant quest gold / coins
val LightGray = SilverSteel            // Color(0xFFD8E4F0) - Polished moon silver
val QuestBlue = Color(0xFF6688A4)      // Cool steel blue (matches nav buttons & icons)
val white = Color(0xFFFFFFFF)

// Light theme colors
val LightStone = Color(0xFFF5F5F5)     // Light neutral background
val LightBrown = SlateBorder           // Clean blue-slate border
val LightGreen = Color(0xFF66BB6A)     // Softer green for light
val LightOrange = TorchAmber           // Softer orange for light
val SoftGold = ImperialGold            // Radiant gold
val DarkText = SlateDeep               // Slate text/icons on light
val LightQuestBlue = Color(0xFF6688A4) // Cool steel blue