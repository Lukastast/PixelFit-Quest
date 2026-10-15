# Plan: Home Screen UI Redesign (Landscape-First, Single RetroPanel, Dwelling Scene)

- **Issue:** none yet. Umbrella issue plus one issue per PR (H1 to H3 below) once this plan is approved.
- **Status:** draft, waiting for Lukas's approval.
- **Author:** Antigravity / PixelFit bot.
- **Builder:** Antigravity (`agy`) with **Gemini 3.8 Flash High**.
- **Branch:** `feat/home-<slug>` per PR, starting with `feat/home-h1`.
- **Expected size:** 3 code PRs, each under ~500 changed lines (~1,050 lines total).
- **Tests before push:** Every PR carries device-free Compose UI tests (Robolectric semantics) + Roborazzi screenshots in landscape (`en-rUS-w891dp-h411dp-land-xxhdpi`) AND portrait (`en-rUS-w411dp-h891dp-xxhdpi`) using `ScreenshotTestBase` qualifiers; full-screen captures.

## Goal

Redesign the home screen into a sleek, gamified, landscape-first (lay-flat default) dashboard that puts training action first while preserving the relaxing pixel-art atmosphere. The lifter rests in the app's existing bedroom-gym dwelling scene (`dwelling_gym_landscape.png`). A single high-contrast retro slate panel provides everything needed to jump into training: streak and level status pills, the "Next up" workout template, last completed time, a prominent pixel-art Start button, a "Choose another template" link, and "This week" summary statistics (Workouts, Sets, and km run when available from Health Connect). The bottom bar / landscape rail streamlines navigation into four core destinations: Home, Templates, History, and Health. Shop/coins and settings clutter are removed from Home; Settings moves into the Health Center screen header.

## Product direction & Design Spec

1. **Landscape-first (lay-flat default):** Follows `/workspace/mockups/home_landscape_v2.png` in landscape (`w891dp-h411dp-land`); portrait fallback follows `/workspace/mockups/home.png` (`w411dp-h891dp`).
2. **Dwelling background:** The background MUST be the app's existing bedroom-gym dwelling scene drawable (`dwelling_gym_landscape.png`, `DwellingTier.GYM`), NOT the workout gym background (`gym.png`). The lifter relaxes/sleeps in bed (`CharacterPose.LYING`), cycling poses (sitting on bench, standing on floor) on tap.
3. **ONE single RetroPanel:**
   - **Streak + level pills:** Top status pills (`RetroPill`) showing weekly streak (`WEEK STREAK 6`) with flame icon and current level with an XP progress bar (`LV 14`).
   - **Next up header:** Cyan accent title (`NEXT UP`), large white pixel template name (`PUSH DAY`), and subtitle with relative completed time (`Last done Thu, 52 min`).
   - **Primary Start action:** Large pixel-art button (`button_unclicked.png` / `button_clicked.png`, label `START`), launching the workout directly.
   - **Alternative template action:** Underlined/styled link `Choose another template` opening the Templates tab.
   - **This week summary:** Cyan header (`THIS WEEK`) with recessed slate stat cards:
     - `WORKOUTS` (count of workouts completed this week)
     - `SETS` (total sets completed this week)
     - `KM RUN` (distance from Health Connect; deferred to H3 as distance is not yet read anywhere in the app). In H1, only Workouts and Sets are displayed.
4. **Bottom navigation (4 destinations):**
   - `Home` (`HOME_SCREEN`)
   - `Templates` (`WORKOUTS_HISTORY_SCREEN?tab=templates`)
   - `History` (`WORKOUTS_HISTORY_SCREEN?tab=history`)
   - `Health` (`HEALTH_CENTER_SCREEN`)
5. **Clutter removal & Settings relocation:**
   - Remove coins, shop/missions button, and old HUD bar from the home screen.
   - **Settings relocation (Recommended):** Moves to the `HealthCenterScreen` header (a top-right pixel gear button). Health Center is already the hub for account, quest, and progression features, accessible in one tap from the bottom bar.
6. **Shared component creation:** H1 creates `RetroPanel` and `RetroPill` in `Android/app/src/main/java/com/pixelfitquest/components/molecules/RetroPanel.kt`, which will be reused by the workout overhaul plan's W2.

## Context: audit of current home screen

### Current implementation on `main` / `feat/home-h1`:
- `feature/home/HomeScreen.kt` (247 lines):
  - Uses `DwellingScene` which defaulted to `DwellingTier.resolve(...)` (resolving to `DwellingTier.TARP` for level 1 users instead of the bedroom gym).
  - Top HUD uses `StatsHudBar.kt` (262 lines): shows coins, streak, level text, and a segmented XP bitmap bar (`xp_0_percent`..`xp_100_percent`).
  - Bottom corner has a floating `achievement_button.png` opening `MissionsDialog`.
  - Multiple modals: `WeeklyStreakDialog`, `MissionsDialog`, `XpStatusDialog`, `LevelUpDialog`.
- `feature/home/HomeViewModel.kt` (402 lines):
  - Manages `userData`, `characterData`, `workouts`, `healthMetrics`, `weeklyBoard`.
  - Workouts are fetched via `workoutRepository.getAllCompletedWorkouts()`, but there is no helper aggregating this week's workout count and set count, nor resolving the "Next up" template.
- `ui/navigation/AppScaffold.kt` (673 lines) & `BottomNavItem.kt`:
  - 5-item navigation bar / rail: `Home`, `Customization`, `Workouts`, `HealthCenter`, `Settings`.
  - Both templates and history are sub-tabs inside `WorkoutsHistoryScreen`.
- `health/` package:
  - `HealthMetrics.kt` tracks `steps`, `stepGoal`, `heartRateBpm`, `sleepMinutes`, `caloriesBurned`, `weeklyHeartPoints`, `weeklySteps`.
  - There is NO distance measurement: `distanceMeters` is stubbed as `null` in `HealthConnectMetricsSource.kt`. Distance records are not yet read from Health Connect.

## PR split (each under ~500 changed lines)

| PR | Scope | Main files touched | Est. lines | Tests | Acceptance |
|---|---|---|---|---|---|
| **H1** | **Shared RetroPanel & Home Screen Overhaul (Core)**<br>Creates `RetroPanel.kt` (`RetroPanel`, `RetroPill`, `RetroStatBox`). Replaces `HomeScreen.kt` with the single retro panel layout (Next up template, last done, START button, Choose another template, This week Workouts/Sets stats), top streak + level pills, and the bedroom-gym dwelling scene (`dwelling_gym_landscape`, lifter relaxing in bed). Removes coins and missions floating button from Home. Adds pure Kotlin `HomeStatsCalculator` for week stats and next up selection. | `components/molecules/RetroPanel.kt` (new)<br>`feature/home/HomeScreen.kt`<br>`feature/home/model/HomeDisplayState.kt` (new)<br>`feature/home/HomeViewModel.kt`<br>`res/values/strings.xml` | ~420 | `HomeDisplayStateTest` (pure JVM unit test: week date filtering, sets sum, next up fallback)<br>`RetroPanelTest` (Robolectric semantics + Roborazzi captures)<br>`HomeScreenTest` (Robolectric semantics + Roborazzi full-screen screenshot tests in landscape & portrait) | Full-screen Roborazzi captures match `home_landscape_v2.png` and `home.png` (bedroom-gym background, single panel, pills, Start button). Tests green. |
| **H2** | **Bottom Navigation (4 tabs) & Settings Relocation**<br>Updates `BottomNavItem` and `AppScaffold` (both bottom bar and landscape side rail) to 4 items: `Home`, `Templates`, `History`, `Health`. Moves Settings access to a gear button in `HealthCenterScreen` header. Preserves all routes. | `ui/navigation/BottomNavItem.kt`<br>`ui/navigation/AppScaffold.kt`<br>`feature/health/HealthCenterScreen.kt`<br>`res/values/strings.xml` | ~340 | `AppScaffoldNavigationTest` (verifies 4 tabs route to Home, Templates, History, Health)<br>`HealthCenterHeaderTest` (verifies Settings gear button exists and triggers navigation to `SETTINGS_SCREEN`)<br>Roborazzi captures of AppScaffold with 4 tabs | 4 tabs render cleanly in portrait navbar and landscape sidebar. Settings accessible from Health Center. No broken routes. |
| **H3** | **Health Connect Distance Reading & "KM RUN" Stat Box**<br>Extends `HealthConnectRepository` and `HealthMetrics` to read `DistanceRecord` for the current week. Updates `HomeDisplayState` and the "THIS WEEK" section on the Home retro panel to display the 3rd stat box (`KM RUN`). | `health/HealthMetrics.kt`<br>`health/HealthConnectRepository.kt`<br>`health/HealthPermissions.kt`<br>`feature/home/HomeScreen.kt`<br>`feature/home/model/HomeDisplayState.kt` | ~290 | `HealthConnectDistanceTest` (unit tests for distance aggregation from Health Connect records)<br>`HomeScreenScreenshotTest` (Roborazzi capture with 3 stat boxes in landscape and portrait) | "THIS WEEK" displays Workouts, Sets, and KM Run when Health Connect is available, with graceful fallback when permissions or records are missing. |

## Detailed Steps for PR H1

1. **Create shared `RetroPanel.kt`** in `com.pixelfitquest.components.molecules`:
   - `RetroPanel`: Slate background (`SlateDeep.copy(alpha = 0.94f)`), rounded corners, crisp bevel border (`SlateBorder`), inner padding. Supports caller-specified modifiers and column content.
   - `RetroPill`: Pill container with rounded shape, slate background, bevel border, optional click handler, row layout. Used for Streak pill, Level pill, and future W2/W3 mount chip.
   - `RetroStatBox`: Sub-card for stat counters (value in bold text, label in small cyan/slate font).
2. **Implement pure Kotlin domain logic (`HomeDisplayState.kt`)**:
   - Computes workouts completed in the current calendar week and total sets.
   - Determines the "Next Up" template: selects from available user templates (or starter fallback template if none exists).
   - Formats relative "Last done" text (e.g. "Last done Thu, 52 min" or "No recent workouts").
3. **Update `HomeViewModel.kt`**:
   - Exposes `StateFlow<HomeDisplayState>` combining `workouts`, `templates`, `healthMetrics`, and `weeklyStreak`.
4. **Update `HomeScreen.kt`**:
   - Background: `DwellingScene` with `tier = DwellingTier.GYM` (`dwelling_gym_landscape`) and character relaxing on the bed (`CharacterPose.LYING`).
   - Top pills: `RetroPill` for Weekly Streak (flame icon + count) and Level (level text + cyan progress bar). Tapping streak opens `WeeklyStreakDialog`; tapping level opens `XpStatusDialog`.
   - Single `RetroPanel`:
     - "NEXT UP" label in `CrystalCyan`
     - Template name in bold white pixel font
     - "Last done..." subtitle in `SilverSlate`
     - Big `PixelArtButton` with `R.drawable.button_unclicked` / `R.drawable.button_clicked` labelled "START", invoking the workout start callback.
     - "Choose another template" link button navigating to templates.
     - "THIS WEEK" label in `CrystalCyan`
     - Stat row with Workouts and Sets boxes.
   - Layout:
     - Landscape (`useTwoPane = true` or `isLandscape`): Panel positioned on the right half, character visible on the left bed, pills top-right.
     - Portrait: Pills at top, character centered above panel, single panel in lower section.
   - Remove `StatsHudBar` (coins/shop) and floating mission button.
5. **Add Tests**:
   - `HomeDisplayStateTest.kt`: JVM unit test verifying date filtering for "This week" and fallback logic.
   - `RetroPanelTest.kt`: Semantics and Roborazzi snapshot of `RetroPanel` & `RetroPill`.
   - `HomeScreenTest.kt`: Device-free Robolectric semantics + Roborazzi full-screen screenshot tests in both landscape (`QUALIFIER_LANDSCAPE`) and portrait (`QUALIFIER_PORTRAIT`).
6. **Verify and Record Baselines**:
   - Run `./gradlew :app:recordRoborazziDebug`.
   - Visually inspect the generated PNGs to verify parity with mockups.
   - Run full local gate: `./gradlew :app:testDebugUnitTest :app:lintDebug :app:compileReleaseKotlin :app:verifyRoborazziDebug`.

## Database changes

None. H1 to H3 require no Room database migrations or schema modifications. Existing `templates`, `workouts`, and `user_profile` tables are read as-is.

## Tests

- **Unit tests**: Pure Kotlin unit tests in `feature/home/model/HomeDisplayStateTest.kt` verifying this-week workout filtering, sets sum, and template resolution without Android dependencies.
- **Compose UI tests (Robolectric)**: `HomeScreenTest.kt` verifying:
  - Streak pill shows correct streak count and is clickable.
  - Level pill shows correct level.
  - "NEXT UP" displays template name and last done text.
  - Start button exists and triggers workout start with expected plan and template name.
  - "Choose another template" exists and triggers navigation.
  - "THIS WEEK" displays correct workout count and sets count.
  - No coin or shop HUD elements appear on Home.
- **Roborazzi Screenshot Tests**:
  - Full-screen screenshots using `ScreenshotTestBase` with `QUALIFIER_LANDSCAPE` (`en-rUS-w891dp-h411dp-land-xxhdpi`) and `QUALIFIER_PORTRAIT` (`en-rUS-w411dp-h891dp-xxhdpi`).
  - Recorded with `./gradlew :app:recordRoborazziDebug`, verified with `./gradlew :app:verifyRoborazziDebug` and `./gradlew :app:testDebugUnitTest`.

## Acceptance Criteria

- [ ] Home screen background is the app's existing bedroom-gym dwelling scene (`dwelling_gym_landscape.png`), NOT the workout gym background (`gym.png`).
- [ ] Lifter character is shown relaxing/sleeping in bed (`CharacterPose.LYING`), with tap to cycle poses.
- [ ] ONE single `RetroPanel` contains the "Next up" template name, last completed timestamp, big pixel-art START button, "Choose another template", and "THIS WEEK" stats (Workouts, Sets).
- [ ] Top status pills (`RetroPill`) display streak and level with progress bar.
- [ ] Start button starts the next-up workout; "Choose another template" navigates to the templates tab.
- [ ] Coins, shop/customization button, and old `StatsHudBar` are completely removed from Home.
- [ ] Shared `RetroPanel` and `RetroPill` components are created in `components/molecules/RetroPanel.kt` for reuse by W2.
- [ ] Device-free Robolectric Compose UI tests and Roborazzi full-screen screenshot tests pass in landscape and portrait.
- [ ] `./gradlew :app:testDebugUnitTest :app:lintDebug :app:compileReleaseKotlin :app:verifyRoborazziDebug` passes with zero errors and no lint-baseline growth.
- [ ] Follows `AGENTS.md` and `GEMINI.md` (pixel-art buttons, no flat boxes, responsive padding).

## Risks & Mitigations

- **Screen real estate on compact landscape devices**: Mitigated by constraining `RetroPanel` width (`widthIn(max = 420.dp)`) and keeping vertical padding compact, allowing the bedroom-gym scene and lifter to breathe on the left.
- **Empty state for new users**: Mitigated by providing starter fallback template ("Full Body") and friendly empty state ("No workouts yet this week") so the UI never displays broken or null values.
- **Visual clipping in portrait**: Mitigated by testing on pinned portrait qualifier (`w411dp-h891dp`) and scaling character and panel spacing proportionally.

## Open Questions & Recommended Answers

1. **Where should Settings move when bottom nav changes to 4 items?**
   - *Recommended:* Move Settings to a gear button in the header of `HealthCenterScreen` (built in H2). Health Center is already the hub for account, quests, and profile progression, keeping Settings easily accessible without cluttering core navigation.
2. **What should "Next Up" show if the user has no saved templates?**
   - *Recommended:* Provide a default starter template (e.g. "Full Body" with basic barbell/dumbbell exercises) so a new user can tap START immediately on first launch.
3. **How should "Choose another template" behave?**
   - *Recommended:* Navigate directly to the Templates tab (`WORKOUTS_HISTORY_SCREEN?tab=templates`), allowing the user to select or create a template.
4. **Health Connect distance availability:**
   - *Recommended:* Show only Workouts and Sets in H1 as 2 clean stat boxes. Add the 3rd stat box ("KM RUN") in H3 once `DistanceRecord` reading is integrated into `HealthConnectRepository`.
