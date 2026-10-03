# Original avatar skins + #172 hero poses

## Summary
Adds **four original** PixelFit archetypes (no trademarked clones) with idle + sitting sheets, wires them into the cosmetics / customization / home sprite path, and ships **static hero poses** for the exercise picker (#172).

## Skins
| Skin | Unlock | Signature |
|---|---|---|
| **Iron Oak** | L10 | Navy singlet, oak-leaf badge, sweatband |
| **Cape Hero** | L15 / 420 coins | Blue suit, cape, gold buckle, red boots |
| **Ember Sparrow** | L18 | Amber hoodie, feather crest, teal shorts (not a duck IP) |
| **Coil Shade** | L22 | Charcoal wraps, lower-face wrap, teal sash |

Assets: `art/characters/` + `drawable-nodpi/*_{idle,sitting,preview}.png`  
Manifest: `art/characters/CharacterSkinManifest.json` + `res/raw/character_skin_manifest.json`  
Design notes (agy): `docs/avatars/DESIGN_NOTES.md`

## #172 hero poses
`hero_pose_{squat,bench,deadlift,overhead_press,row}.png` → `HeroPoseVisuals` → thumbnail in `ExerciseCatalogPicker`.

## Hook-up
- `CosmeticCatalog`, `CustomizationCatalog`, `AvatarSkinBridge`
- `IdleAnimation` / `CharacterIdleAnimation`, `DwellingCharacterSprite`, `HomeThemeVisuals`
- Unit tests: `AvatarSkinBridgeTest`, `HeroPoseVisualsTest`

## Out of scope
- Play Store release
- Path B Sheet sync
- Full multi-clip walk/workout animator (still hybrid per #141)

## Test plan
- [ ] Customization: equip Iron Oak / Cape Hero / Ember Sparrow / Coil Shade (god mode or level)
- [ ] Home: idle + sitting frames for new skins
- [ ] Workout builder: squat/bench/deadlift/OHP/row rows show hero pose thumbs
- [ ] `./gradlew :app:testDebugUnitTest` (AvatarSkinBridge / HeroPose / CosmeticUnlocker)
