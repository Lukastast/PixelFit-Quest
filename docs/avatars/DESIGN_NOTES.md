## Sprite bible (v1)

- **Canvas & Sheets:**
  - `idle`: 6240×480 px (13 horizontal frames @ 480×480 px).
  - `sitting`: 3840×480 px (8 horizontal frames @ 480×480 px).
  - Format: Transparent RGBA PNG.
  - Rendering: `FilterQuality.None` (nearest-neighbor point sampling, no bicubic filtering).
- **Proportions:** Chibi 1:1.5 head-to-body ratio; solid 1–2px outer black outline (`#000000`); default base peach skin (`#FFE0BD`); 2×4 vertical rectangular pixel eyes; 3/4 view facing right for home screen idle.
- **Palette Discipline:** Max 8 flat colors per skin + 1 shared boundary black (`#000000`). Zero airbrush gradients; strict flat shade blocks.

---

## Four original archetypes

### 1. Cape Hero
- **Unlock Pitch:** Level 15 milestone or 4,500 FitCoins (Quest reward tier).
- **Palette (8+1):** `#000000` (Outline), `#2A6FDB` (Hero Blue), `#163E82` (Blue Shadow), `#D62828` (Cape/Boots), `#961616` (Red Shadow), `#F7B801` (Gold Buckle), `#FFE0BD` (Peach), `#DDA177` (Skin Shade), `#FFFFFF` (Specularity).
- **Signature Silhouette:** Trailing dual-tail quest cape billowing over shoulders with knee-high folded boot cuffs.

### 2. Iron Oak
- **Unlock Pitch:** Level 10 Strength Quest milestone.
- **Palette (8+1):** `#000000` (Outline), `#1B2A4A` (Navy Singlet), `#0F1829` (Singlet Shadow), `#2D6A4F` (Oak Badge), `#52361B` (Leather Boots), `#E76F51` (Headband Accent), `#FFE0BD` (Peach), `#DDA177` (Skin Shade), `#FFFFFF` (Band Stripe/Eyes).
- **Signature Silhouette:** Broad barrel chest, oak-leaf belt emblem, and dual-stripe forehead sweatband.

### 3. Ember Sparrow
- **Unlock Pitch:** Level 8 Streak Challenge (Cardio speed focus).
- **Palette (8+1):** `#000000` (Outline), `#F3722C` (Amber Hoodie), `#CA5310` (Hoodie Shadow), `#2EC4B6` (Teal Shorts), `#F9C74F` (Crest Tuft), `#333333` (Sneaker Sole), `#FFE0BD` (Peach), `#DDA177` (Skin Shade), `#FFFFFF` (Laces/Eyes).
- **Signature Silhouette:** Upward-swept feathered hair tuft poking through an oversized workout hoodie cowl.

### 4. Coil Shade
- **Unlock Pitch:** Level 20 Mastery or 30-day workout streak.
- **Palette (8+1):** `#000000` (Outline), `#25282A` (Charcoal Wraps), `#181A1B` (Wrap Shadow), `#00A896` (Teal Sash), `#028090` (Sash Shadow), `#A0AAB2` (Wrap Highlights), `#FFE0BD` (Peach), `#DDA177` (Skin Shade), `#FFFFFF` (Glint).
- **Signature Silhouette:** Snug ninja cowl with full lower-face wrap and floating sash-tails hanging off hip.

---

## #172 static hero poses

- **Specs:** 480×480 px single-frame static transparent PNGs; non-animated; exercise picker UI use only.
- **Style:** Same chibi proportions, outline weight, and palette as main sheets.
- **Required Poses:**
  1. `hero_pose_squat.png`: Deep parallel squat, flat back, hands clasped at chest.
  2. `hero_pose_bench.png`: Supine bench press, bar touching lower chest.
  3. `hero_pose_deadlift.png`: Barbell mid-shin liftoff, hip hinge, chest up.
  4. `hero_pose_overhead_press.png`: Full lockout overhead barbell press, head tilted forward.
  5. `hero_pose_row.png`: 45-degree bent-over row, barbell pulling to lower abdomen.

---

## Hook-up checklist

1. **`app/src/main/res/drawable-nodpi/`**:
   - `char_cape_hero_idle.png`, `char_cape_hero_sit.png`
   - `char_iron_oak_idle.png`, `char_iron_oak_sit.png`
   - `char_ember_sparrow_idle.png`, `char_ember_sparrow_sit.png`
   - `char_coil_shade_idle.png`, `char_coil_shade_sit.png`
   - `hero_pose_squat.png`, `hero_pose_bench.png`, `hero_pose_deadlift.png`, `hero_pose_overhead_press.png`, `hero_pose_row.png`
2. **`CosmeticCatalog.kt`**: Append new skin identifiers (`CAPE_HERO`, `IRON_OAK`, `EMBER_SPARROW`, `COIL_SHADE`), display names, rarities, and coin/level costs.
3. **`CustomizationCatalog.kt`**: Register unlock conditions and catalog category groups for cosmetic picker UI.
4. **`AvatarSkinBridge.kt`**: Bind each `SkinId` to its corresponding idle/sitting drawable resource IDs and picker icons.
5. **`IdleAnimation.kt` / `CharacterIdleAnimation.kt`**: Verify 13-frame horizontal offset calculations (stride = 480 px) for idle and 8-frame (stride = 480 px) for sitting.
6. **`HomeThemeVisuals.kt`**: Configure sitting anchor offsets and z-indexes for home scene furniture interaction.
7. **`app/src/main/assets/CharacterSkinManifest.json`** *(Optional)*: Add registry metadata for remote fallback (asset ID, unlock criteria, frame counts).
