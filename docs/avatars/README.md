# PixelFit original character skins

**Branch:** `feat/performance-and-workout-notes`  
**Issues:** [#141](https://github.com/Lukastast/PixelFit-Quest/issues/141) skins + sprite system, [#172](https://github.com/Lukastast/PixelFit-Quest/issues/172) static hero poses  
**Rule:** Original archetypes only — **no** Superman / Donald Duck / Arnold (or other trademarked) likenesses.

## Inventory (existing, pre-this-PR)

| Asset | Path | Format | Notes |
|---|---|---|---|
| Wanderer idle M/F | `drawable-nodpi/character_{male,woman}_idle.png` | 6240×480, 13×480 frames, RGBA | Default skin |
| Wanderer sit/lie | `character_*_sitting.png` (8f), `*_lying*.png` (6f) | 480-tall strips | Home poses |
| Gym Fit idle/workout | `fitness_character_*_idle.png`, `*_workout.png` | idle 13f / workout 8f | Coin skin |
| Shadow (silhouette) | `locked_*_character_idle.png` | 13f | Level skin |
| Cape Hero walk (detailed) | `cape_hero_walk.png` | 5504×768, 8×688 | Gemini walk strip; SpriteSheetPlayer |
| Coaching / clip poses | `clip_clip_pose*.png`, `character_*_workout.png` | 8f OHP loops | Form feedback |

Wiring today: `AvatarSkinBridge`, `IdleAnimation` / `CharacterIdleAnimation`, `DwellingCharacterSprite`, `CustomizationCatalog`, `CosmeticCatalog`.

## New skins in this pack (original)

| Id | Display | Idle | Sitting | Preview | Unlock |
|---|---|---|---|---|---|
| `cape_hero` | Cape Hero | `cape_hero_idle.png` | `cape_hero_sitting.png` | `cape_hero_preview.png` | L15 / 420 coins |
| `iron_oak` | Iron Oak | `iron_oak_idle.png` | `iron_oak_sitting.png` | `iron_oak_preview.png` | L10 |
| `ember_sparrow` | Ember Sparrow | `ember_sparrow_idle.png` | `ember_sparrow_sitting.png` | `ember_sparrow_preview.png` | L18 |
| `coil_shade` | Coil Shade | `coil_shade_idle.png` | `coil_shade_sitting.png` | `coil_shade_preview.png` | L22 |

Copies live in:

- `art/characters/` (source of truth + `CharacterSkinManifest.json`)
- `Android/app/src/main/res/drawable-nodpi/` (app resources)
- Manifest also at `Android/app/src/main/res/raw/character_skin_manifest.json`
- Design notes: `docs/avatars/DESIGN_NOTES.md`

Idle = **13** frames @ 480×480. Sitting = **8** frames. Transparent BG. Render with `FilterQuality.None`.

## #172 hero poses (static)

| File | Exercise family |
|---|---|
| `hero_pose_squat.png` | Squat / front squat |
| `hero_pose_bench.png` | Bench family |
| `hero_pose_deadlift.png` | Deadlift family |
| `hero_pose_overhead_press.png` | OHP (uses existing workout sheet peak frame) |
| `hero_pose_row.png` | Row family |

Also under `art/hero_poses/` and `drawable-nodpi/`. Hooked via `HeroPoseVisuals` → `ExerciseCatalogPicker` row thumbnail.

## How to hook a new skin

1. Drop `skinid_idle.png` (6240×480) + optional `_sitting.png` / `_preview.png` into `drawable-nodpi/`.
2. Add id to `CosmeticCatalog`, `CustomizationCatalog.characters`, `AvatarSkinBridge`, `IdleAnimation` / `CharacterIdleAnimation`, `DwellingCharacterSprite` (sitting), `HomeThemeVisuals.previewRes`.
3. Update `art/characters/CharacterSkinManifest.json`.
4. Prefer Antigravity (`agy`) for UI-facing design notes under `docs/avatars/`.

## Out of scope here

- Play Store shipping  
- Path B Google Sheet sync  
- Full walk/workout multi-clip animator (still hybrid Compose bob/slide + optional sheets per #141 comment)
