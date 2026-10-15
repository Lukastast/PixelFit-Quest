# Plan: Remove body metrics (height and arm length): first slice split out of #176

- **Issue:** none yet. This is slice 176a of splitting PR #176 (`feat/performance-and-workout-notes`). An issue is opened only after approval.
- **Status:** approved by Lukas 2026-10-09 (option A via #185, removal as one PR)
- **Author:** PixelFit bot
- **Builder:** Grok Build
- **Branch:** `feat/remove-body-metrics`, from latest `origin/main` (at time of writing `f014376`, "Merge pull request #178")
- **Expected size:** about 1,130 changed lines in total, about 930 of them deleted. 689 of those lines are whole-file deletions of orphaned or unused UI. Without those, about 440 lines need real review (see "Size" below).

Everything below was checked against `origin/main` @ `f014376` and #176's head `41424c1` on 2026-10-09. Line numbers refer to `origin/main`.

## Goal

Remove height and arm length from the app: the screen, the customization "Body Stats" card and dialog, the data model, the Room columns and the analyzer input. None of it affects rep tracking, and it's personal body data we don't need to keep. Existing users keep the rest of their profile, and the migration drops the two columns safely, with a test.

## Context

What exists today on `main`, and what the builder must re-check before editing:

**Tracking does not use these fields (verified)**
- `feature/workout/WorkoutViewModel.kt:194-201` builds `AnalyzerUser(heightCm = user?.height ?: 178, armLengthCm = user?.armLength)` and passes it to `setAnalyzer.analyzeSet(...)`.
- `feature/workout/analysis/SetAnalysis.kt:5-8` defines `data class AnalyzerUser(heightCm: Int, armLengthCm: Float?)`.
- `feature/workout/analysis/SetAnalyzer.kt:16` takes `user: AnalyzerUser`, but nothing in `SetAnalyzer.kt` reads `user`. Grepping `user` matches only the parameter line.
- `feature/workout/analysis/ExerciseProfile.kt:52` `val usesArmLength: Boolean = false`. It's set to `true` in `ExerciseProfiles.kt:59, 90, 106` (curl, lat pulldown, tricep extension) and never read anywhere.
- #176's analysis drops `AnalyzerUser`, the `user` parameter and `usesArmLength` entirely.
- `ANALYSIS_VERSION` (`SetAnalysis.kt:3`, currently 3) does **not** change, because analysis output is identical.

**Where the values are stored: Room only, not Firestore**
- `firebase/repository/UserRepository.kt:49-51`: `updateUserData(updates)` only calls `localStore.updateUserData(updates)` (`LocalPixelFitStore`, Room). Despite the `firebase` package name, nothing writes `height` or `armLength` to Firestore on `main`.
- The only Firestore read of user docs is `getLeaderboard()` (`UserRepository.kt:130-150`). It's gated by `cloudSyncPolicy.isLeaderboardEnabled()`, which is `false` in `StubCloudSyncPolicy` (`local/CloudSyncPolicy.kt`).
- Writers of the two keys: `bodyMetrics/BodyMetricsViewModel.kt:46, 61, 76, 83`, `customization/CustomizationViewModel.kt:368, 376`, `settings/SettingsViewModel.kt:157-168` (`setHeight`, no callers).
- The Room column mapping is in `local/LocalPixelFitStore.kt:88-89` (`getUserField`), `:401-402` (`applyUpdates`) and `:455` (`floatValue`, used only for `armLength`).
- History: height existed before offline-first (#122, 2026-09-08), so builds before #122 may have written `height` into Firestore `users/{uid}` for signed-in users. `armLength` came with #128 (2026-09-13), after #122, so it never went to Firestore.

**Model and entity**
- `firebase/model/UserData.kt:4-5`: `height: Int = 178`, `armLength: Float? = null`.
- `local/db/entity/UserProfileEntity.kt:13-14` holds the columns, and `:45-46` maps them in `toUserData()`.
- `local/export/LocalExportFormatter.kt:40-41` writes `"height"` and `"armLength"` into the JSON export's `profile`. No importer exists in `local/export/`.

**UI**
- `feature/bodyMetrics/BodyMetricsScreen.kt` (261 lines) and `BodyMetricsViewModel.kt` (98 lines). **There is no nav entry on `main`.** Nothing outside `feature/bodyMetrics/` references `BodyMetricsScreen`, and `ui/navigation/` has no route for it, so the screen is already unreachable.
- `feature/customization/ui/StatsCustomizationPanel.kt` (152 lines) has **no callers** on `main`.
- `feature/customization/ui/StatsDialog.kt` (178 lines) is opened from `CustomizationScreen.kt:145` when `uiState.isStatsDialogOpen` is true.
- `feature/customization/ui/CharacterCustomizationPanel.kt:62-63, 69` has the `heightCm`, `armLengthCm` and `onOpenStats` params. `BodyCalibrationCard` is at `:370-420` and is used at `:589` (two-pane) and `:623` (single-pane), next to the equip `ActionButton`.
- `CustomizationScreen.kt:93-94, 146-147` passes the values through, and the header has a "📏 Stats" quick action that calls `openStatsDialog`.
- `CustomizationViewModel.kt:134-135` loads the values, `:364-386` holds `setHeight`, `setArmLength`, `openStatsDialog` and `closeStatsDialog`, and `:395-399` the `HEIGHT_CM_*`/`ARM_CM_*` constants.
- `customization/model/CustomizationScreenUiState.kt:27-28` has `heightCm` and `armLengthCm`, plus `isStatsDialogOpen`.
- Strings in `Android/app/src/main/res/values/strings.xml`: lines 41 (`customization_tab_stats`, unused), 48-50, 58-60, 303-311. There's also a stray copy at `Android/app/src/res/values/strings.xml` (`current_height`, `enter_height_hint`, `set_height`), which #176 also removes. No other `values-*` locales exist.
- Health Connect does not read height (no `HeightRecord` on `main`).

**Room**
- `local/db/PixelFitDatabase.kt`: `version = 11`, `exportSchema = false`. Migrations 6→7 … 10→11 live in the companion.
- `firebase/di/AppModule.kt:60-73` registers `MIGRATION_6_7`…`MIGRATION_10_11` and still calls `.fallbackToDestructiveMigration()` (legacy).
- **No exported schema JSON exists** (no `schemas/` dir, no Room Gradle plugin or `room.schemaLocation`). No `androidTest` source set exists, and unit tests only have `junit:junit:4.13.2` and coroutines (`Android/app/build.gradle.kts`). Room is 2.8.4 (`gradle/libs.versions.toml:23`). `minSdk = 29`.
- No `ForeignKey`, index or trigger references `user_profile`.

**Privacy page**
- `docs/privacy.html:58`: `<li>Body / profile metrics you enter (e.g. height)</li>`, and `:28` "Last updated: 24 September 2026".

**Overlapping open PRs (hotspots: `PixelFitDatabase.kt`, `AppModule.kt`, `LocalPixelFitStore.kt`)**
- **#176** contains this exact removal plus its own `MIGRATION_11_12` (`PixelFitDatabase.kt` +71/-1). Once 176a merges, #176's copies must be dropped on rebase.
- **#177** (draft) also contains the body-metrics deletion, a `version = 12` / `MIGRATION_11_12`, and `isMinifyEnabled = true`. It has to drop all of those too, or it will collide on version 12.
- **#181** touches `SetAnalysis.kt`, `SetAnalyzer.kt`, `SetAnalyzerTest.kt` and `WorkoutViewModel.kt`, which overlaps with removing `AnalyzerUser`. **#183** (tracking rebuild, Phase 0) will also rebase onto these files.
- **#180** and **#182** edit `docs/privacy.html`. Small, textual conflict only.
- **#184** (unmerged) adds `AGENTS.md` and this template. Its module map lists `bodyMetrics/`, and its database rules say turning on `exportSchema` + `MigrationTestHelper` is "a planned separate PR". See open question 1.

## Files touched

Size estimates are taken from #176's actual diff (`git diff --numstat origin/main...41424c1`). Every one of these hunks in #176 is body-metrics-only, except where noted.

| File | Change | Est. lines |
|---|---|---|
| `Android/app/src/main/java/com/pixelfitquest/feature/bodyMetrics/BodyMetricsScreen.kt` | delete file (orphaned) | -261 |
| `.../feature/bodyMetrics/BodyMetricsViewModel.kt` | delete file | -98 |
| `.../feature/customization/ui/StatsDialog.kt` | delete file | -178 |
| `.../feature/customization/ui/StatsCustomizationPanel.kt` | delete file (no callers) | -152 |
| `.../feature/customization/CustomizationScreen.kt` | remove the Stats quick action, `onOpenStats`, the height/arm args and the `StatsDialog` block | -34 |
| `.../feature/customization/CustomizationViewModel.kt` | remove load of height/arm, `setHeight`, `setArmLength`, open/close dialog, the `HEIGHT_CM_*`/`ARM_CM_*` constants and the `roundToInt` import. #176's +4 skin IDs are **not** part of this slice | ~-30 |
| `.../feature/customization/model/CustomizationScreenUiState.kt` | remove `heightCm`, `armLengthCm`, `isStatsDialogOpen` | -3 |
| `.../feature/customization/ui/CharacterCustomizationPanel.kt` | remove params and `BodyCalibrationCard`; the equip `ActionButton` takes the full row width in both layouts (same as #176) | +7 / -84 |
| `.../feature/settings/SettingsViewModel.kt` | remove the unused `setHeight` (`:157-168`) only. #176's other changes to this file are out of scope | ~-12 |
| `.../feature/workout/WorkoutViewModel.kt` | remove `AnalyzerUser` import, `val user` and the `user = AnalyzerUser(...)` argument | ~-6 |
| `.../feature/workout/analysis/SetAnalysis.kt` | delete `AnalyzerUser` | -5 |
| `.../feature/workout/analysis/SetAnalyzer.kt` | drop the `user` parameter | -1 |
| `.../feature/workout/analysis/ExerciseProfile.kt` | drop `usesArmLength` | -1 |
| `.../feature/workout/analysis/ExerciseProfiles.kt` | drop the 3 `usesArmLength = true` lines | -3 |
| `.../firebase/model/UserData.kt` | drop `height`, `armLength` | -2 |
| `.../local/db/entity/UserProfileEntity.kt` | drop the 2 columns and their `toUserData()` mapping | -4 |
| `.../local/LocalPixelFitStore.kt` (**hotspot**) | drop the `"height"`/`"armLength"` branches and `floatValue` | -10 |
| `.../local/export/LocalExportFormatter.kt` | drop `"height"`/`"armLength"` from `profile` | -2 |
| `.../local/db/PixelFitDatabase.kt` (**hotspot**) | `version = 12` and `MIGRATION_11_12` (table rebuild, below) | +71 / -1 |
| `.../firebase/di/AppModule.kt` (**hotspot**) | register `MIGRATION_11_12` | +1 |
| `Android/app/src/main/res/values/strings.xml` | delete `customization_tab_stats`, `stats_title`, `height_label`, `stats_arm_length`, `current_height`, `enter_height_hint`, `set_height`, and the `<!-- Body Metrics Screen -->` block (lines 303-311) | ~-16 |
| `Android/app/src/res/values/strings.xml` (stray file) | delete the same 3 height strings, as #176 does | -3 |
| `Android/app/src/test/java/com/pixelfitquest/feature/workout/analysis/SetAnalyzerTest.kt` | drop `private val user` (`:10`) and the `user` argument at ~28 `analyzeSet` call sites | ~±30 |
| `Android/app/src/test/.../local/db/Migration11To12Test.kt` (new; exact location depends on question 1) | migration test | ~+100 |
| `Android/app/build.gradle.kts`, `Android/gradle/libs.versions.toml` | test deps (`androidx.room:room-testing:2.8.4`, Robolectric, `androidx.test:core`), `testOptions.unitTests.isIncludeAndroidResources = true`, plus schema location/test assets if question 1 = A in this PR | ~+10 to +15 |
| `Android/app/schemas/com.pixelfitquest.local.db.PixelFitDatabase/11.json`, `12.json` | generated by Room (excluded from the line budget per `AGENTS.md`) | generated |
| `docs/privacy.html` | remove the `:58` bullet and bump "Last updated" (`:28`) | +1 / -2 |
| `AGENTS.md` (only if #184 has merged first) | remove `bodyMetrics/` from the module map | ±1 |

### Size

- Total: about 200 added and 930 deleted, so about 1,130 changed lines.
- 689 of the deleted lines are four whole-file deletions (`BodyMetricsScreen`, `BodyMetricsViewModel`, `StatsDialog`, `StatsCustomizationPanel`). The screen and the panel are already unreachable. Leaving them out, about 440 lines need real review.
- My recommendation is to keep this as one PR. Splitting "delete UI" from "drop columns" would only create an intermediate state, where the dialog is gone but the data stays, with nothing to gain. If you'd rather keep to the 500-line rule strictly, the split would be 176a-1 (UI deletion, about 870 lines, all deletions) and 176a-2 (model + Room v12 + test, about 260 lines).

## Steps

1. Create the branch from latest `origin/main` in its own worktree (`docs/plans/README.md` §3). Don't run this in parallel with any other plan touching `PixelFitDatabase.kt`, `AppModule.kt` or `LocalPixelFitStore.kt`.
2. **Schema baseline (depends on question 1).** Before changing any entity, enable schema export and build once at `version = 11` so Room writes `11.json`, then commit it. The v11 schema can only be generated from the pre-change code.
3. Delete `feature/bodyMetrics/` (both files), `StatsDialog.kt` and `StatsCustomizationPanel.kt`.
4. Strip the stats UI from `CustomizationScreen.kt`, `CharacterCustomizationPanel.kt`, `CustomizationViewModel.kt` and `CustomizationScreenUiState.kt`. Copy only the body-metrics hunks from #176 for these files, not the skin/cosmetic hunks.
5. Remove `AnalyzerUser`, the `user` parameter of `SetAnalyzer.analyzeSet`, its use in `WorkoutViewModel`, and `usesArmLength`. Update `SetAnalyzerTest`. Leave `ANALYSIS_VERSION` at 3.
6. Remove `height`/`armLength` from `UserData`, `UserProfileEntity` (+ `toUserData`), `LocalPixelFitStore` (both `when`s and `floatValue`), `LocalExportFormatter`, and `SettingsViewModel.setHeight`.
7. Bump `PixelFitDatabase` to `version = 12`, add `MIGRATION_11_12` (exact SQL below), and register it in `AppModule.addMigrations(...)`. Do not touch `.fallbackToDestructiveMigration()` in this PR (`AGENTS.md`: it's removed in its own PR).
8. Build once so Room writes `12.json`, if schema export is on.
9. Add the migration test (below).
10. Remove the strings and update `docs/privacy.html`.
11. Run `grep -rn -E "armLength|heightCm|AnalyzerUser|usesArmLength|bodyMetrics|BodyMetrics|StatsDialog|isStatsDialogOpen" Android/ docs/`. It must return nothing (except `computeSpacingScale(heightDp=…)`-style layout names, which are unrelated).
12. Run the gate `./gradlew :app:testDebugUnitTest :app:lintDebug` from `Android/`, then `/review`, rebase and open the PR.

## Database changes

**Version 11 → 12. Drop `user_profile.height` and `user_profile.armLength`.**

SQLite on `minSdk 29` predates `ALTER TABLE … DROP COLUMN` (added in SQLite 3.35), so the migration rebuilds the table. Use #176's updated version (`41424c1`, `PixelFitDatabase.kt` lines added after `MIGRATION_10_11`), which I checked column by column against `UserProfileEntity` on `main`. The 28 remaining columns match exactly:

```kotlin
val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS user_profile_new")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS user_profile_new (
                id TEXT NOT NULL PRIMARY KEY,
                musicVolume INTEGER NOT NULL, level INTEGER NOT NULL, coins INTEGER NOT NULL,
                exp INTEGER NOT NULL, streak INTEGER NOT NULL,
                lastActivityDate TEXT NOT NULL, lastStepsRewardDate TEXT NOT NULL,
                lastSleepRewardDate TEXT NOT NULL, lastWeeklyHeartRewardWeek TEXT NOT NULL,
                lastVitalityBonusDate TEXT NOT NULL, lastStreakUpdateDate TEXT NOT NULL,
                characterGender TEXT NOT NULL, characterVariant TEXT NOT NULL,
                unlockedVariantsCsv TEXT NOT NULL, equippedHomeUpgrade TEXT NOT NULL,
                unlockedHomeUpgradesCsv TEXT NOT NULL, equippedGym TEXT NOT NULL,
                unlockedGymsCsv TEXT NOT NULL, equippedAppBackground TEXT NOT NULL,
                unlockedAppBackgroundsCsv TEXT NOT NULL, dwellingLegacyMigrated INTEGER NOT NULL,
                skillForm INTEGER NOT NULL, skillIron INTEGER NOT NULL, skillVitality INTEGER NOT NULL,
                skillRespecDate TEXT NOT NULL, rewardedSetsDate TEXT NOT NULL,
                rewardedSetsCount INTEGER NOT NULL
            )""".trimIndent())
        db.execSQL("INSERT INTO user_profile_new (<same 28 columns>) SELECT <same 28 columns> FROM user_profile")
        db.execSQL("DROP TABLE user_profile")
        db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
    }
}
```

(The builder uses #176's full text, which lists all 28 columns explicitly in both the INSERT and SELECT lists.)

Notes:
- **Builder must verify:** the column list matches `UserProfileEntity` after the change. If 12.json is generated, compare against its `createSql`; otherwise compare against `PixelFitDatabase_Impl.createAllTables` under `build/generated/ksp`. The migration test enforces this anyway, because Room fails validation on any mismatch.
- The v6→v11 migrations added columns with `DEFAULT` clauses, and the rebuilt table has none. That's fine: the entity declares no `@ColumnInfo(defaultValue)`, so Room's validation ignores defaults. Room runs migrations in a transaction, so a failure rolls back.
- Registered in `AppModule` after `MIGRATION_10_11`. No new destructive path.
- **Exported schema JSON:** none exists today (`exportSchema = false`). `MigrationTestHelper` needs `11.json` and `12.json`, so see question 1.

## Tests

**New: `Migration11To12Test`** (JVM, Robolectric, `androidx.room.testing.MigrationTestHelper`):
1. `migrate11To12_keepsProfileAndDropsBodyColumns`: create the DB at v11 and insert one `user_profile` row with non-default values in every column (e.g. `level=17`, `coins=420`, `characterVariant='iron_oak'`, `skillIron=3`, `rewardedSetsCount=5`, `height=185`, `armLength=71.5`). Run `runMigrationsAndValidate(name, 12, true, MIGRATION_11_12)`, then assert:
   - `PRAGMA table_info(user_profile)` has no `height`/`armLength` and has the 28 other columns.
   - Every other value of the row is unchanged.
   - The row count is still 1.
2. `migrate11To12_emptyProfileTable`: same flow with no row; the table exists and validates.
3. `migrate11To12_leftoverTempTable`: pre-create `user_profile_new` at v11 (simulating an interrupted earlier attempt); the migration still succeeds. This covers the `DROP TABLE IF EXISTS`.
4. `openAtV12_withAllMigrations`: build the real DB via `Room.databaseBuilder(...).addMigrations(<6_7 … 11_12>)` on the migrated file and read the profile through `UserProfileDao`. This proves the Room validation that `AppModule` relies on.

**Updated:**
- `SetAnalyzerTest`: compiles without `AnalyzerUser`. The expected counts and scores in every existing assertion stay **unchanged**, which proves the analyzer never used the fields.
- `LocalExportFormatterTest`: add one assertion that the JSON `profile` has no `height`/`armLength` keys.

**Run from `Android/`:** `./gradlew :app:testDebugUnitTest :app:lintDebug`

- **Recommended 2026-10-09 by PR Scout and Torben, pending Lukas's approval:** option A (schema export + test setup lands first as its own small PR) and this slice ships as ONE PR.
- **Required migration assertion:** the 11→12 rebuild must copy every remaining `user_profile` column unchanged. Seed a v11 row with non-default values in every column (especially XP, level, coins, streak), migrate, and assert each value is identical; asserting only that height/arm length are gone is not enough.

## Manual test steps (debug build on a phone)

1. **Upgrade path with real data:** install the current `main` debug build (DB v11) and complete one workout. Open Customization and set height/arm in the Stats dialog. Use **Export workout log (JSON)** and keep the file.
2. Install this branch's debug build **over** it (do not uninstall).
3. Open the app and confirm:
   - Level, XP, coins, streak, equipped character, gym, background, skill points, workout history and templates are all unchanged.
   - No crash at startup (a failed Room validation would crash here).
4. **Customization:** there's no "📏 Stats" header action and no "Body Stats" card in portrait or landscape (two-pane). The equip/buy button fills its row and nothing overlaps. Take screenshots of both orientations for the PR (`GEMINI.md`).
5. **Workout:** do a set of bench press and a set of bicep curl. Rep counts and set review behave the same as on `main`.
6. **Export:** export JSON again. `profile` has no `height`/`armLength`, and everything else matches step 1.
7. **Fresh install:** uninstall, install this build, and complete onboarding and one set without crashing.
8. Optional: use the Database Inspector (Android Studio → App Inspection) to confirm `user_profile` has 28 columns and `user_version = 12`.

## Acceptance criteria

- [ ] No code, string or UI references height/arm length (grep in step 11 is clean).
- [ ] Upgrading from v11 keeps all other profile data. `Migration11To12Test` passes.
- [ ] `SetAnalyzerTest` assertions are unchanged and green, and `ANALYSIS_VERSION` is still 3.
- [ ] JSON export no longer contains `height`/`armLength`.
- [ ] `docs/privacy.html` no longer lists body metrics, and "Last updated" is bumped.
- [ ] Tests and lint are green locally and in CI, with no lint-baseline growth.
- [ ] Customization screenshots in portrait and landscape are in the PR.
- [ ] Product rules still hold: there's no cloud call, export stays free, and nothing touches login or Firebase DI except adding one migration to the existing Room builder in `AppModule`.

## Firestore decision

- **Stop writing:** already true on `main`. Nothing writes these keys to Firestore (`UserRepository.updateUserData` → Room only). After this PR the keys don't exist in `UserData` at all.
- **Existing cloud data: leave it, no Firestore calls in this PR.** Only `height` could exist, and only in `users/{uid}` docs written by builds before #122. Deleting it would need a Firestore write, and that's gated as Pro (`AGENTS.md` product rule 2). If `getLeaderboard()` is ever enabled, Firestore's `toObject<UserData>()` logs and ignores the unknown `height` field rather than failing. Legacy cloud cleanup belongs to the data-deletion work (#182) or Pro sync, not here.

## Risks

- **Room validation mismatch crashes on startup.** Mitigated by test 1/4 and manual step 3. The test is the main reason this split exists (PR Scout's finding on #176).
- **Rollback wipes the whole DB on a downgrade.** `AppModule` still has `.fallbackToDestructiveMigration()`, so installing an older v11 build over a v12 DB recreates every table: workouts, templates, progression, everything. Never test rollback by installing an older build on a phone with real data. Export first.
- **Version-12 collision.** #176 and #177 each have their own `MIGRATION_11_12`. If either merges before 176a, this plan must move to 12→13 and drop the collision. After 176a merges, both must delete their copies on rebase. Only one agent may bump the DB at a time.
- **Merge conflicts** in `SetAnalysis.kt`, `SetAnalyzer.kt`, `SetAnalyzerTest.kt` and `WorkoutViewModel.kt` with #181 and #183, and in `privacy.html` with #180 and #182. All are mechanical (one parameter, one bullet).
- **Data loss is intended:** stored height/arm values are gone after the upgrade. The user agreed (2026-10-09).
- **Robolectric + SDK 36:** the builder must pick a Robolectric version that supports `compileSdk 36` and, if needed, pin `@Config(sdk = [35])`. Verify in the PR rather than assume.
- **Outside the repo:** if the Play Console Data safety form declares body measurements, it should be updated after this ships. I couldn't check this from the repo.

## Rollback

- **Before release:** revert the PR. No device has v12 except test phones, which can be reinstalled.
- **After a v12 build is on devices:** don't revert to v11 (destructive fallback, see Risks). Roll forward with a v12→13 migration: `ALTER TABLE user_profile ADD COLUMN height INTEGER NOT NULL DEFAULT 178` and `ADD COLUMN armLength REAL`, then restore the code. The old values can't be recovered, except from a JSON export the user made before upgrading.

## Out of scope

- R8 / `isMinifyEnabled` / `isShrinkResources` (#176, #177), and **everything else** in #176: rest timer, analysis v4, avatars/skins, hero poses, set notes, rep-edit log, settings cards, ROM calibration, the missions-button removal in `HomeScreen.kt`/`StatsHudBar.kt`, drawable moves.
- Removing `.fallbackToDestructiveMigration()` (its own PR per `AGENTS.md`).
- Any Firestore read, write or cleanup.
- Range-of-motion or tracking changes (tracking rebuild plans 1-9).
- Bumping the export `schemaVersion` (no importer exists; removing two keys doesn't break any reader in the repo).

## Open questions

1. **How do we get the migration test, given there's no exported schema today?** `MigrationTestHelper` needs `11.json` + `12.json`, and #184's `AGENTS.md` says enabling `exportSchema` + `MigrationTestHelper` is "a planned separate PR, do not mix into feature work". Options:
   - **A (recommended):** a tiny prerequisite PR **176a-0** first: enable schema export at v11, commit `11.json`, and add `room-testing` + Robolectric test setup (~20 handwritten lines + generated JSON, no behavior change). 176a then adds `12.json` and the `MigrationTestHelper` test above.
   - **B:** do the schema-export setup inside 176a (one PR, but breaks the `AGENTS.md` rule if #184 merges first).
   - **C:** skip exported schemas. A Robolectric test creates the v11 table from hand-copied SQL, runs the migration, then opens the DB with Room to validate. That's weaker, because the v11 SQL is hand-maintained.
