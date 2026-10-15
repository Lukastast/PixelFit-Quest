# Plan: Workout screen UI overhaul (measurement first, mount chip, per-state layouts)

- **Issue:** none yet. One umbrella issue plus one issue per PR (W1 to W6 below) once this plan is approved.
- **Status:** draft, waiting for Lukas's approval (asked for 2026-10-09). On 15 Oct Lukas decided open questions 6, 7 and 8 (see the end). Nothing is built until he approves the rest. Open questions are at the end.
- **Author:** PixelFit bot. Claude writes the plans only; it doesn't build.
- **Builder:** Antigravity (`agy`) with **Gemini 3.8 Flash High** (decided by Lukas, 15 Oct). Grok Build stays the builder for non-UI plans.
- **Branch:** one `feat/workout-ui-<slug>` per PR, from latest `origin/main`. They are serial, not parallel, because they all touch `WorkoutScreen.kt` / `SetReviewOverlay.kt`.
- **Expected size:** 6 code PRs, about 320 to 480 changed lines each (about 2,390 in total). There's also a mockup round first (step 0) that produces images and no code.
- **Tests before push (decided by Lukas, 15 Oct):** every PR carries its own device-free Compose UI tests (Robolectric + Roborazzi screenshots, set up in W1), and they must pass locally before the branch is pushed.

Checked against `origin/main` @ `f014376` ("Merge pull request #178"), #176 `feat/performance-and-workout-notes` @ `41424c1`, #183 `logic/rep-tracking-rebuild` @ `4b95587`, #179 `compliance/a11y-claims` @ `6e19c89`, #177 @ `f5dce89` and #184 `chore/ai-coding-loop` @ `f760565` on 2026-10-09. Line numbers are `origin/main` unless a ref is named. Read-only: nothing was changed in any checkout.

Re-checked on 2026-10-15 against `origin/main` @ `86f19f8` (#184 merged): no app source changed since `f014376` (only docs, `AGENTS.md` and the lint CI job), so every `main` line number above still holds. There's still no Robolectric on `main`.

## Goal

The workout screen should show what the phone actually measured and nothing it can't measure. Each set gets a big, honest rep count. Failed reps are visible but left out of the total. Uncertain reps are counted and flagged. Range of motion, tempo and bar quality appear only on phones with a gyro. Tilt and twist appear only when the sleeve side is known. It also adds the one-tap mount chip Lukas asked for ("Phone: left sleeve" with a tiny bar drawing), and gives each state of the flow (ready, recording, review, rest, next exercise) its own clean layout. Pixel art stays as the skin. It isn't the product.

## Product direction (already agreed with Lukas, do not change in the build)

1. PixelFit is a measurement tool first. Pixel art is a skin, not the product.
2. The screen shows what the new tracking measures:
   - **Rep count.** The number in the total is the accepted reps.
   - **Failed / partial reps:** visible, **not** in the total. One tap counts one (`acceptCandidate` already exists).
   - **Uncertain reps:** **counted and flagged** ("check this one"). One tap removes one.
   - **Range of motion, tempo, bar quality:** only on **Tier A** (phone has a gyro). On **Tier B** they are **hidden, not shown as 0**, and the review says why in one line.
   - **Tilt / twist:** hidden when the sleeve side is unknown.
3. **No live rep counter** until the recording gate passes (tracking plan item 10c). The layout keeps an empty slot for it.
4. **Mount chip** on the workout screen: "Phone: left sleeve" with a tiny bar drawing showing which end is which. One tap flips left/right. Unset means tilt and twist stay hidden.

## Context: audit of the current workout screen

### What runs today on `main` (`f014376`)

`feature/workout/WorkoutScreen.kt` (433 lines) is one `BoxWithConstraints` with every state layered on top of a full-bleed gym image. Phases are `Idle, Countdown, Recording, Reviewing` (`model/WorkoutPhase.kt`).

| # | Element | Where (main) | Verdict |
|---|---|---|---|
| A1 | Full-bleed gym background (`CustomizationCatalog.gymDrawable`) | `WorkoutScreen.kt:201-212` | **Skin.** Keep, but put a dark scrim behind every number; text sits on a busy image today. |
| A2 | Status line: "Set: 1 / 3 · tap play · 60.0 Kg" (and "· recording · … · 12s") | `:214-255`, strings `workout_status_*` | **Clutter.** Four facts in one 16sp line. Weight is repeated in A5, and "tap play" repeats the button. Split it into a set counter and an exercise header. |
| A3 | Control row: play/pause, exercise name + `IMU`/`LOG` badge, stop | `:257-318` | **Hazard + jargon.** **Stop ends the whole workout with no confirmation** (`viewModel.leaveWorkout()`, `:313`; `WorkoutViewModel.leaveWorkout` finishes or discards). It sits right next to pause during recording. "IMU" means nothing to users. On `main` the buttons' TalkBack label is "Button" (`PixelArtButton.kt` default); #179 fixes that. |
| A4 | Pause button = "end set" while recording | `:270-279` | **Wrong icon.** It doesn't pause, it ends the set and analyses it. Use a stop-square or "Done" icon. |
| A5 | `BetweenSetsWeightHud`: weight + 6 plate chips (−5 … +5) | `:320-326`, `BetweenSetsWeightHud.kt:58-91` | **Keep, shrink.** Chips are about 30 dp tall (`padding 7/4` around 11sp text), under the 48 dp touch target. Six chips are a lot; propose −/+ with a step selector (open question 4). |
| A6 | Character sprite with an infinite bob, also while recording | `:330-362` | **Skin.** It's fine in ready/rest. While recording it costs frames for nothing (the screen faces the floor). #176 already turns it off during recording. |
| A7 | 3-2-1-GO countdown | `:115-127`, `:364-380` | Removed on #176 (no `Countdown` phase there). Don't bring it back. |
| A8 | "PERFECT / GREAT / MISS" pop after confirm, from the mean **form score** | `:129-145`, `:382-394`; `WorkoutViewModel.kt:590-598` | **Contradicts measurement first.** It grades a set on a blended score, even on sets where ROM isn't measured. #176 drops the live Good/Miss overlay (`e492c72`); this pop must not come back. |
| A9 | "RECORDING" badge, center-end | `:397-413` | Duplicates A2. Keep one recording indicator. |
| A10 | Set review overlay | `:415-431` → `SetReviewOverlay.kt` | See below. |
| A11 | Screen-on flag and orientation lock | `orientation/WorkoutOrientationLock.kt`, `WorkoutOrientation.kt:32-44` | Keep. **Lay-flat landscape is ON by default** (`DEFAULT_ENABLED = true`), so landscape is the primary layout and portrait is the fallback. Mockups and screenshots must cover both. |

`SetReviewOverlay.kt` (501 lines on main):

| # | Element | Where (main) | Verdict |
|---|---|---|---|
| R1 | Title "Set 2 · Bench Press" + "8 reps accepted" in **13sp** | `:66-82` | **The key number is the smallest text on screen.** Make the count the hero. |
| R2 | Clip-pose warning | `:83-89` | Keep as a set-level flag line. |
| R3 | One row per rep: index, **form score /100**, ROM override (−/+/50%/100%), ecc/con seconds, level/twist, tags, Accept/Remove/Merge | `:91-102`, `RepRow :303-383`, `RomOverrideRow :385-414` | **Densest clutter.** Each rep shows 4 edit chips and 3 links at once, all in 11 to 12sp. Move editing behind "Edit" per rep. Form score is a blend (`formScoreFrom`, `SetAnalysis.kt`) and can't be true on Tier B. |
| R4 | **Level/twist prints "even" when the value is null** | `:352-362`: `levelLabel(rep.levelDeg ?: 0f)`, `twistLabel(rep.twistDeg ?: 0f)` | **Bug against direction #2.** If only one of the two is null, the missing one shows as "even" (a fake 0). Must show hidden instead. |
| R5 | Actions panel: "Form NN/100", **"N samples"**, weight ±2.5, Add rep / Redo / Confirm | `SetReviewActions :157-301` | "N samples" is debug info. The weight row hard-codes `"WEIGHT"`, `"-2.5"`, `"+2.5"` and `"kg"` (`:213, :231, :249, :234`), so it isn't localisable and duplicates A5 with different steps. |
| R6 | Log-only state ("Log only / No IMU profile yet") | `:175-188` | Keep the behaviour, reword ("Not tracked: add reps by hand"). |
| R7 | Colours are raw hex (`0xFF2A3A4A`, `0xFF4A2A2A`, `0xFF1565C0` …) and the buttons are flat coloured `Text`s | throughout | Conflicts with `GEMINI.md` §1 (pixel-art button assets, never flat boxes). Use theme tokens (`SlateDeep`, `ImperialGold`, `VitalGreen`, …) and `PixelArtButton`. |

Data facts the UI must respect (main):
- The saved set count is accepted reps only (`WorkoutViewModel.kt` `saveConfirmedSet` → `reps = accepted.size`). Weight-up suggestions read the max reps per session (pixelfit-grok-review §6 d). **So the review is where the user corrects the count. Make that easy.**
- Set-level flags `no_gyro` and `no_rotation_vector` already exist (`SetAnalyzer.kt:41-42`) and are hidden by `CoachingTags` (`CoachingTags.kt:13-14`). The review can use `no_gyro` to decide Tier B per set **today**.
- `DetectedRep.romScore` and `formScore` are non-null `Float`s (`analysis/SetAnalysis.kt`). Tier B "hidden, not zero" is enforced in the UI (W1 rules) until the tracking work makes ROM nullable (item 10b).
- `levelDeg`/`twistDeg` are nullable, but today they measure the wrong axis: elevation of device X, not the bar (pixelfit-grok-review §2 "Mount frame"; `Attitude.kt:71-72`). On curls the "tilt" is really the curl roll.

### What #176 changes (`feat/performance-and-workout-notes` @ `41424c1`; #177 @ `f5dce89` carries the same workout-UI diff)

- `WorkoutPhase` becomes `Idle, Recording, Reviewing, Resting`. The countdown is gone.
- `WorkoutScreen.kt` (472 lines on #176):
  - The status line moves into a leaf composable, `WorkoutStatusHud` (`:371-420`), which alone collects the throttled `recordingHud` flow. **Keep that performance split.**
  - Play is shown in `Resting` too (`:222`).
  - Rest is shown by `RestTimerHud` → `RestTimerCard.kt:25-74` (42sp mm:ss, Pause/Resume, Stop autostart, "Next set starts when this hits zero").
  - There's a **unilateral `SidePicker` "Left / Right"** for which arm/leg (`:268-274`, `:444-472`, strings `set_side_left/right`).
  - The bob is turned off while recording (`:291-308`).
- `SetReviewOverlay.kt` (628 lines on #176):
  - Per-rep **Assist** toggle (`:426`).
  - The same unilateral L/R chips in the review header (`:110-128`).
  - **Set notes** card, `SetNotesCard` (`:557-628`, `OutlinedTextField`, 2 to 4 lines, char count).
  - The `?: 0f` "even" bug is still there (`:404`).
- **Naming clash:** #176's unilateral "Left/Right" (which limb) and the new mount chip (which sleeve the phone is on) are different things. The UI has to keep them apart (see "Mount chip").

### What #183 changes (`logic/rep-tracking-rebuild` @ `4b95587`, on top of #176's older `74a65a7`)

- `sensor/MountSidePrefs.kt` (new, 32 lines) stores `MountSide { LEFT, RIGHT, UNKNOWN }` (`sensor/ImuSample.kt:38-42`) under key `bar_sleeve_side` in `pixelfitquest_prefs`. The default is `UNKNOWN`. Its KDoc says "unset hides tilt and twist", but **analysis doesn't read it yet** (`BarImbalance.LATERAL_SIGN` is still −1).
- `components/molecules/BarTrackingSettingsCards.kt` (new): `BarSleeveCard` (Not set / Left / Right chips) and `DeveloperTraceCard`. Both are shown in Settings (`SettingsScreen.kt:214, :394` on #183).
- `SetReviewOverlay` gains an optional "Share trace" button (`onShareTrace`).
- `WorkoutScreen.kt:401-432` (#183) builds the trace with a **hard-coded empty `TraceLabel`** (`:416-420`). It reads `MountSidePrefs` **at share time** (`:415`), not at record time, so flipping the sleeve after a set would mislabel that trace. W3 fixes this by snapshotting the mount side at set start.
- Tracking plan 1 (finish #183, being written now) adds the **label UI** to the review: true count, failed indices, notes, start type `rack/floor/hang`, and which way the top of the phone points. That block lands in `SetReviewOverlay` before this overhaul restyles it (see Dependencies).

### #179 (a11y, `6e19c89`, stacked on #176)

- It adds `contentDescription` to `PixelArtButton` and labels play/pause/stop on the workout screen (`workout_play_desc`, `workout_pause_desc`, `workout_stop_desc`). The overhaul keeps those strings and that parameter, and extends the same treatment to every new control.

### Test infrastructure today

- JVM unit tests only, with `junit:junit:4.13.2` and coroutines (`Android/app/build.gradle.kts:196-197`). `ui-test-manifest` is a debug dependency (`:194`), but there's no `ui-test-junit4` and no Robolectric on `main`.
- **176a-0 (schema-export PR, building now) adds Robolectric + `androidx.test:core`.** W1 reuses that if it has merged; if not, W1 adds Robolectric + `androidx.test:core` itself (about 10 lines, and 176a-0 then drops them on rebase). W1 also adds `androidx.compose.ui:ui-test-junit4` (version from the existing Compose BOM `2026.03.00`). So Compose UI tests run inside `./gradlew :app:testDebugUnitTest` in CI, with no emulator.
- **Screenshot tests: decided by Lukas (15 Oct).** Device-free screenshot tests are required. The plan uses **Roborazzi** (Recommended over Paparazzi: it runs on the same Robolectric runtime as the semantics tests, so there's one setup and one Gradle task, while Paparazzi brings its own layoutlib runtime that's known to clash with Robolectric in the same module). W1 adds the Roborazzi Gradle plugin and libraries to `libs.versions.toml`, `testOptions.unitTests.isIncludeAndroidResources = true`, `@GraphicsMode(NATIVE)` in a small shared test base, and wires verification so the existing CI command `./gradlew :app:testDebugUnitTest` fails on a screenshot diff (for example `roborazzi.test.verify=true` in `Android/gradle.properties`; the builder confirms the exact switch for the pinned Roborazzi version). Baselines are recorded with `./gradlew :app:recordRoborazziDebug` and committed under `Android/app/src/test/screenshots/` (binaries, not counted in PR size). The CI workflow file isn't changed. Manual PR screenshots (`GEMINI.md`, `AGENTS.md`) are still attached on top.

## Step 0: settle the look with a mockup round (Torben's suggestion), before any code

Generate images with Antigravity's image tool (Nano Banana) or Midjourney. **This plan doesn't generate them.** Lukas or Antigravity runs the prompts, Lukas picks one direction per state, and the chosen PNGs are committed to `docs/plans/mockups/workout-overhaul/` (once #184 is merged; until then `/workspace/plans/mockups/`). Each code PR then names the mockup it implements.

Shared style block, appended to every prompt below:

> Style: Android phone app UI, 16-bit pixel-art skin over a clean, readable data layout. Dark slate panels (#1E2430-ish) with thin 1px lighter borders, gold accent (#E2B33C) for headings and the primary number, green (#7BC67B) only for "counted", muted red-brown only for "not counted". Pixel font for headings only; body numbers in a crisp, highly legible pixel font, no tiny text. A dim pixel-art gym in the background behind a 70% dark scrim. Flat, no glossy gradients, no stock photos, no hands, no real brand logos. Show it as a straight-on screenshot, no device frame. 1920x1080 landscape unless stated.

**Prompt M1: Ready to lift (landscape)**
> A workout screen for a strength-training app, in the "ready" state before a set. Top-left: exercise name "BENCH PRESS" in gold, under it "Set 2 of 4". Top-right: a small square "menu" button (three dots) for End workout. Centre: one large square pixel-art PLAY button labelled "Start set". Below it a weight stepper: "−" button, "80 kg" in large gold digits, "+" button, and a small "step 2.5" toggle. Left side: a small chip reading "Phone: left sleeve" with a tiny pixel drawing of a horizontal barbell with plates on both ends and a small phone rectangle on the left sleeve, an arrow on the phone pointing toward the left plates. Bottom-right: a small idle pixel-art character, about 15% of the screen height, clearly decorative. An empty dashed outline box to the right of the play button labelled "live count (coming later)" in faint grey. Lots of empty space, nothing else.

**Prompt M2: Recording (landscape, glanceable)**
> The same app while a set is being recorded. The phone will be clipped to the barbell face-down, so the screen is mostly black to save battery. Centre: a big red pixel "DONE" square button labelled "End set". Above it: "RECORDING · 0:23" in large white pixel digits with a small pulsing red dot. Top-left in small text: "Bench press · Set 2 of 4 · 80 kg". No character, no gym background, no other buttons. A faint hint line at the bottom: "Clip the phone on, lift, then tap End set." There is no End workout button on this screen.

**Prompt M3: Set review, gyro phone, sleeve known (landscape)**
> Set review screen. Left 60%: header "Set 2 · Bench press" and a huge gold number "8" with "reps counted" under it. Under the number, a red-brown pill: "1 not counted: half rep (rep 9) · Count it". Then a compact list of reps, one line each: "1  ROM 98%  2.1s down / 0.9s up", "2  ROM 96% …", rep 5 has a small yellow "?" badge reading "check: first rep, no pause · Don't count". A small line: "Bar level: even · Twist: even". Right 40%: a slate panel with "Weight 80 kg (− / +)", a "Notes" text box with placeholder "RPE, cues…", and three pixel-art buttons stacked: green "Confirm set" (largest, bottom), "Add rep", "Redo set". No form score, no sample counts.

**Prompt M4: Set review, phone without gyro, sleeve not set (landscape)**
> Same set review layout as before, but for a phone without a gyroscope. Huge gold "8 reps counted". Directly under it a calm info line with a small "i" icon: "Count only on this phone: range of motion, tempo and bar balance need a gyroscope." The rep list shows only rep numbers and a tick per rep, no ROM, no tempo, no percentages, no zeros, no dashes. No bar level or twist line at all. The notes box and Confirm / Add rep / Redo buttons are on the right as before.

**Prompt M5: Rest and up next (landscape)**
> Rest screen between sets. Centre: a large pixel timer "1:24" with a thin progress ring, "Pause" and "+30 s" small buttons under it, and the text "Next set starts at 0:00" in small grey. Left panel "Up next": "Set 3 of 4 · Bench press · 80 kg" with the weight stepper (− 80 kg +) and the chip "Phone: left sleeve" with the tiny barbell drawing. A second variant on the right half of the image shows the panel when the exercise changes: "Next exercise: Barbell row · 4 sets · 60 kg" with a gold border, and the sleeve chip reading "Phone: sleeve not set — tilt hidden" in grey. Small decorative character in a corner.

**Prompt M6: Mount chip close-ups (square, 1024x1024, sprite-sheet style)**
> A sprite sheet of one small UI chip in three states, each about 260x64 px, pixel-art style, on a dark slate background, labelled underneath in small text. (1) "Phone: left sleeve": icon of a horizontal barbell seen from above with plates at both ends, the phone rectangle on the LEFT sleeve, an arrow on the phone pointing outward toward the left plates, a tiny "L" next to the left plate. (2) "Phone: right sleeve": mirror image, phone on the right sleeve, "R" by the right plate. (3) "Phone: sleeve not set": the barbell icon with a dashed empty phone outline and a small question mark, text grey. Below the three chips, a tiny caption: "Tap to switch side". Crisp 1px outlines, readable at 1x.

**Prompt M7: Portrait fallback (1080x1920)**
> Portrait version of the set review from M3 for when lay-flat landscape is off: big "8 reps counted" at the top, the not-counted pill and the compact rep list in a scrolling middle, then the notes box and a sticky bottom bar with "Redo", "Add rep" and a large green "Confirm set". The sleeve chip and weight row sit in a single 48dp-tall line above the bottom bar.

Mockup acceptance: one image per state is chosen and the hero number reads at arm's length. Tier B (M4) shows **no** zeros or dashes where ROM/tempo would be. The chip drawing makes left vs right obvious without reading the text.

## Proposed layout per state

Landscape is primary (lay-flat on by default), and portrait is the stacked version of the same blocks. Shared frame on every non-recording state:
- **Header** (top-left): exercise name (heading font, gold) and "Set 2 of 4". It replaces A2's status sentence. Weight is **not** in the header.
- **Overflow menu** (top-right, a 48dp pixel button with three dots): "End workout…" opens a confirm dialog ("End workout? 3 sets saved." / "Discard: no reps yet"). That's where Stop moves (A3). Back goes to the same dialog instead of leaving instantly.
- The **mount chip** shows in ready, rest and next-exercise states for bar exercises (see "Mount chip").
- The **character** is decorative: max about 15% of the height, bottom corner, never overlapping numbers, no bob while recording.

### 1. Ready (`Idle`, first set or rest skipped)
- Primary action: a large Start-set button (`PixelArtButton`, play asset, 96dp landscape / 80dp portrait, label "Start set").
- Weight stepper below it (see open question 4), one shared `WeightStepper` component (also used in rest and review).
- Unilateral exercises: #176's limb picker, relabelled **"Working side: Left / Right"**, so it can't be confused with the phone chip.
- Not tracked (`imuSupported == false`): the badge reads "Not tracked: log reps after the set", the mount chip is hidden, and the Start button reads "Log set".
- **Live-count slot:** a fixed-size empty area next to the Start button (`LiveCountSlot`, renders nothing today, `Modifier.size` reserved). It's filled only by the gated live-counter plan.

### 2. Recording (`Recording`)
The phone is clipped **screen toward the floor** for the whole set, so this state is for the seconds before clipping and after unclipping.
- Pure black background, no gym image, no character, no animations (battery and frame budget; the `recordingHud` leaf keeps only the timer recomposing).
- Centre: a large **"End set"** button (stop-square icon, not pause, per A4), at least 120dp, and "Recording · 0:23" in big digits.
- One small line: "Bench press · Set 2 of 4 · 80 kg", plus the snapshot of the mount side ("Phone: left sleeve") in grey, read-only.
- **No End-workout button in this state.** It's in the overflow menu only, and the menu is hidden while recording.
- Live-count slot reserved (empty).

### 3. Set review (`Reviewing`)
Two panes in landscape (60/40) and stacked in portrait with a sticky action bar.
- **Hero:** accepted count in about 72sp gold, "reps counted". TalkBack reads this first, as a polite live region.
- **Not counted** (reps with `accepted == false`, excluding `setup`): one pill per rep, "Rep 9: half rep · Count it". `Count it` = `acceptCandidate`. Setup/rerack cycles collapse into one grey line, "2 unrack/rerack movements ignored".
- **Uncertain** (counted but flagged; needs the tracking item-6 tag, see Dependencies): yellow "?" badge on the rep line, with the reason and "Don't count" (= `removeRep`). Until item 6 ships, no rep gets this badge. The component exists, and a test feeds it a fake tag.
- **Rep list:** one compact line per rep with the index plus Tier-A-only fields (ROM %, down/up seconds, bar line). Tapping a rep expands **Edit** with today's actions: ROM −/+/50/100 (Tier A only), Assist (#176), Merge next (only when `shouldOfferMerge`), Remove.
- **Set line** (Tier A + sleeve known + profile has `PATH_TILT`): "Bar level: even · Twist: 3° toward head". Otherwise the line is absent.
- **Tier B line:** "Count only on this phone: range of motion, tempo and bar balance need a gyroscope." Shown when the set has the `no_gyro` flag.
- Clip-pose flag (R2) kept as one line.
- **Right pane:** `WeightStepper` (corrects this set's logged weight), Notes (#176 `SetNotesCard`, restyled), **labels block from tracking plan 1** (dev/recording builds only, collapsed by default), then the actions: **Confirm set** (primary, largest), Add rep, Redo set, and Share trace (only if #183's gate is true).
- Removed: per-rep form score /100, the set "Form NN/100", "N samples" (moved to the developer card / trace metadata), the PERFECT/MISS pop (A8).

### 4. Rest (`Resting`, #176)
- Centre: #176 `RestTimerCard`, restyled: big mm:ss, Pause/Resume, Stop autostart, and the autostart hint. Plus a **"+30 s"** button (decided by Lukas, 15 Oct; built in W6): each tap adds 30 seconds to the running rest, works while paused too, and the autostart target moves with it.
- Left "Up next": "Set 3 of 4 · 80 kg", `WeightStepper`, and the mount chip.
- The Start-set button stays available (start early), same as #176's `:222`.

### 5. Between exercises (no separate phase today)
Today `confirmSet` silently swaps to the next exercise and lands in Rest/Idle (`WorkoutViewModel.confirmSet`, main `:326-342`). The proposal adds no new phase, only state: when `currentSetNumber == 1 && currentExerciseIndex > 0`, the Up-next panel becomes **"Next exercise"** with a gold border: name, sets, planned weight, and the mount chip **highlighted** if the new exercise is a bar-sleeve lift and the side is unset. That's the moment to check the clip. After the last exercise, nothing changes: it goes to the workout summary as today.

## Mount chip

**Source of truth:** `MountSidePrefs` (from #183 / tracking plan 1), one device setting, not per set. The Settings `BarSleeveCard` stays as the full editor (including "Not set").

**Shown when:**
- The current exercise is `imuSupported` **and** its profile `mount == Mount.BAR_SLEEVE`.
- The state is ready, rest or next-exercise.
- Hidden for `CABLE_HANDLE`, `DUMBBELL`, `MACHINE_PAD` and log-only.
- For `BAR_CENTER` see open question 3.

**Looks:**
- A 48dp-tall pill: `[bar glyph] Phone: left sleeve`.
- The glyph is a horizontal barbell with plates at both ends, a phone rectangle on the chosen sleeve, an arrow on the phone pointing toward that sleeve's plates, and a tiny "L" or "R" by that plate. "Left" means **the lifter's left hand** (true for bench, squat and OHP alike), and the subtitle in Settings says so.
- The glyph is drawn with Compose `Canvas` in pixel steps (no new bitmap needed), or as a small pixel drawable if the mockup round produces a better one.

**States and tap behaviour:**

| Pref | Chip text | Glyph | Tap | Effect on review |
|---|---|---|---|---|
| `LEFT` | "Phone: left sleeve" | phone on left sleeve | flips to `RIGHT` | tilt/twist may show (see visibility table) |
| `RIGHT` | "Phone: right sleeve" | phone on right sleeve | flips to `LEFT` | same |
| `UNKNOWN` | "Phone: sleeve not set" (grey, "tilt hidden" in the next-exercise variant) | dashed phone + "?" | opens a 2-button popup **Left / Right** (no guessing) | tilt/twist hidden |

- **Long-press** (and a TalkBack custom action) offers "Not set" to clear it. See open question 2 if Lukas prefers a pure tap-cycle.
- The chip is **not tappable while recording**. The mount side is **snapshotted at set start** into `WorkoutState` (`mountSideAtStart`). The review and the trace metadata use the snapshot, not the live pref. This fixes #183's read-at-share-time (`WorkoutScreen.kt:415` on #183).
- Flipping the chip changes **only how tilt and twist are signed and displayed**. It never changes the rep count. Counting must be sign-free (tracking item 5). The chip copy and the PR must not imply otherwise.
- **Caveat from both reviews:** left/right is only enough if the clip forces the phone's top to point the same way on each sleeve. A 180° turn about the screen axis flips the bar axis. The glyph's arrow states the assumption ("top of phone toward the plates"). Tracking plan 1's label "which way the top of the phone points" will tell us whether a second toggle is ever needed. Not in this plan.

## What each element shows (visibility rules, implemented once in W1 as pure Kotlin)

Tier A = the phone has a gyroscope (`SensorManager.getDefaultSensor(TYPE_GYROSCOPE) != null`) for pre-set UI. For a recorded set, use the set's analysis: Tier B when `"no_gyro" in analysis.flags` (`SetAnalyzer.kt:42`). The set flag wins over the device check.

| Element | Tier A, sleeve known | Tier A, sleeve unknown | Tier B (any sleeve) | Log-only exercise |
|---|---|---|---|---|
| Reps counted (hero) | shown | shown | shown | shown (manual reps) |
| Not-counted / failed reps | shown, "Count it" | shown | shown | n/a |
| Uncertain badge | shown when tagged | shown when tagged | shown when tagged | n/a |
| ROM % per rep + ROM edit chips | shown | shown | **hidden** (no 0, no dash) | hidden |
| Down/up seconds (tempo) | shown | shown | **hidden** | hidden |
| Bar quality / stability, "dropped" tag | shown | shown | **hidden** | hidden |
| Bar level / twist line + tilt/twist tags | shown **only if** the profile has `PATH_TILT` and the value is non-null (see open question 1) | **hidden** | **hidden** | hidden |
| `short_rom` tag | shown | shown | **hidden** | hidden |
| Form score (any) | removed everywhere | removed | removed | removed |
| Tier B explainer line | – | – | shown once per set | – |
| Mount chip | shown (bar-sleeve lifts) | shown, "not set" | shown (it still feeds labels and traces) | hidden |
| Live-count slot | reserved, empty | reserved, empty | reserved, empty | absent |

Per-field nulls: if a field is null on a Tier A rep, it's hidden for that rep. Never substitute 0 or "even" (fixes R4).

## Accessibility

- Keep #179's `PixelArtButton(contentDescription = …)` and its three strings. Every new control gets a real label: "Start set", "End set", "More options", "Count rep 9", "Don't count rep 5", "Edit rep 3", "Add 30 seconds of rest".
- **Mount chip:** `Role.Button`, `stateDescription` = "Phone on left sleeve", `onClickLabel` = "Switch to right sleeve", plus custom accessibility actions "Left sleeve", "Right sleeve" and "Not set". The glyph is `contentDescription = null` (the text already says it).
- **Review:** the hero count is a polite live region announced as "8 reps counted, 1 not counted". Counted vs not counted is never colour-only (icon + text). Expanded edit rows use `semantics { stateDescription = "expanded" }`.
- **Touch targets** are at least 48dp (today's plate chips and text links are about 24 to 30dp). Text never goes below 12sp. The pixel heading font is for headings only; numbers use the most legible face.
- **Contrast:** every text block sits on the slate scrim. Check with Accessibility Scanner on the gym backgrounds and attach the result to W2.
- **Recording:** the screen faces the floor, so nothing visual matters mid-set. Audio or haptic cues belong to the later live-counter plan, not here.
- All strings go in `strings.xml`. That includes R5's hard-coded "WEIGHT", "±2.5" and "kg", and the `"${text.length} chars"` in #176's notes card.

## Dependencies on other work

| Overhaul piece | Waits for | Why |
|---|---|---|
| Everything (W2 to W6) | **#176 split:** the rest timer, notes, assist and unilateral-side slices merged to `main` | They're only on #176/#177 today. Building on `main` first would mean redoing the screen after #176 lands, with heavy conflicts in `WorkoutScreen.kt`, `SetReviewOverlay.kt` and `WorkoutViewModel.kt`. See open question 5. |
| W1 (rules + test setup) | Nothing blocking. Uses **176a-0**'s Robolectric if merged, otherwise adds it | W1 touches none of the hotspot files (only new files, `build.gradle.kts`, `libs.versions.toml`, `gradle.properties`, tests). Recommended: build W1 **now**, ahead of the #176 split, because the home screen redesign plan (`home-screen-redesign.md`) also needs its test scaffolding. |
| W2 shared retro panel | `components/molecules/RetroPanel.kt` (`RetroPanel`, `RetroPill`), **created by whichever of W2 or home H1 merges first** | Both plans use the same slate pixel panel and pill (see the home plan). Most likely H1 lands first (it isn't blocked by #176), and W2 only imports it. If W2 goes first and adding the panel pushes it over ~500 lines, split it off as **W2a** (panel + its screenshot test, ~120 lines). |
| W3 mount chip | **Tracking plan 1** (finish #183: `MountSidePrefs`, `BarSleeveCard`, labels, last-N exports, privacy) merged | The pref and the enum come from #183. |
| W4/W5 review | Tracking plan 1's **label block** merged | The overhaul restyles and places it rather than racing it in the same file. |
| "Uncertain" badge data | Tracking **item 6** (rep acceptance: min-leg amplitude, clear failed vs uncertain) | Today there's no uncertain tag. The UI ships the component, and it lights up when the tag exists. "Not counted" works today from `accepted == false`. |
| Tilt/twist values being *true* | Tracking **item 9** (bar metrics: bar-axis elevation, sleeve sign, twist about world-up) | Today's `levelDeg` measures the wrong axis. Open question 1: hide tilt/twist entirely until item 9. |
| ROM null at the data level | Tracking **item 10b** (Tier B count-only) | Until then the UI enforces hiding from the `no_gyro` flag. |
| Live-count slot content | Tracking **item 10c** (live counter behind the replay gate) | Slot only, until then. |
| Nothing here | Item 8 (held-out gate) | The overhaul changes no counting, `ANALYSIS_VERSION` stays, no DB change. |

## PR split (each under ~500 changed lines, tests included)

Run them serially in this order. Each one rebases on `origin/main` before its PR. Hotspot files for this series: `WorkoutScreen.kt`, `SetReviewOverlay.kt` and `WorkoutViewModel.kt`. Don't run any of these in parallel with #181, tracking plan 1, or anything else touching those files. The home screen redesign plan touches none of them, so it can run alongside W2 to W6 once W1 has merged (only `strings.xml` and the shared `RetroPanel.kt` overlap; rebase resolves the first, and the second is created once, see Dependencies).

**Every PR carries its own tests and they pass before push (decided by Lukas, 15 Oct).** Each row's Compose tests are Robolectric semantics tests plus at least one Roborazzi screenshot per new state, in landscape (`w891dp-h411dp-land`, the default because lay-flat is on) and portrait (`w411dp-h891dp`).

| PR | Scope | Main files | Est. lines | Tests | Manual check |
|---|---|---|---|---|---|
| **W0** | Mockups (no code). Run M1 to M7, Lukas picks, commit PNGs + a short `README` naming the pick per state | `docs/plans/mockups/workout-overhaul/*` | ~20 text (+ binaries) | – | Lukas picks |
| **W1** | Display rules + **device-free test setup** (the first PR of the series sets it up, so W2 onward only add tests), **no visible UI change**. `MeasurementTier` (device gyro check + `no_gyro` set flag), a pure `RepDisplay`/`SetDisplay` model implementing the visibility table (incl. the null-not-zero rule), `ui-test-junit4`, Robolectric (if 176a-0 hasn't merged), Roborazzi plugin + verify switch, a shared screenshot test base (`@GraphicsMode(NATIVE)`, landscape/portrait qualifiers), one smoke Compose + screenshot test of the existing `PixelArtButton` | new `feature/workout/ui/WorkoutDisplayRules.kt`, `MeasurementTier.kt`, `build.gradle.kts`, `libs.versions.toml`, `gradle.properties`, new `src/test/.../testing/ScreenshotTestBase.kt`, tests | ~350 | `WorkoutDisplayRulesTest`: every cell of the table (Tier A/B × sleeve L/R/unknown × PATH_TILT yes/no × log-only); `levelDeg = null, twistDeg = 3f` → level hidden, twist shown (R4 regression); `no_gyro` flag beats device tier; uncertain tag → badge. Smoke: `PixelArtButton` renders and clicks under Robolectric; its Roborazzi baseline verifies; a deliberately changed baseline makes `testDebugUnitTest` fail (checked once by hand, noted in the PR) | none (no UI change) |
| **W2** | Screen shell + ready + recording. Header, overflow menu with End-workout confirm (Back goes to the same dialog), Start-set / End-set buttons (end-set icon), black recording state, `LiveCountSlot`, scrim, character size cap, remove the status sentence and duplicate badge, keep the `WorkoutStatusHud` leaf. Uses the shared `RetroPanel` for the header/scrim panels (from home H1, or W2a if W2 goes first) | `WorkoutScreen.kt`, new `ui/WorkoutHeader.kt`, `ui/EndWorkoutDialog.kt`, strings | ~450 | Compose (Robolectric): ready shows "Start set" with a11y label; recording shows "End set", no End-workout, no weight stepper; overflow → dialog → confirm calls `leaveWorkout` once, cancel doesn't; Back opens the dialog. Roborazzi: ready and recording, landscape + portrait | Bench set on a phone in landscape and portrait; confirm Stop can't be hit by accident; TalkBack walk-through; screenshots |
| **W3** | Mount chip + glyph + snapshot. `MountChip` composable (Canvas glyph), tap flip, unset → popup, long-press/custom action → not set, shown only for `BAR_SLEEVE`; `WorkoutState.mountSideAtStart` set in `startSet`; trace metadata uses the snapshot | new `ui/MountChip.kt`, `WorkoutScreen.kt`, `WorkoutViewModel.kt` (small), `MountSidePrefs` usage, strings | ~320 | Compose: tap on LEFT → pref RIGHT and text updates; UNKNOWN tap → popup, no change until a choice; custom actions present; hidden for a curl (`DUMBBELL`/non-sleeve) and log-only; VM: flipping after `startSet` doesn't change `mountSideAtStart`. Roborazzi: the chip in all 3 states | Flip on the chip, check Settings' `BarSleeveCard` matches and the reverse; TalkBack actions; screenshots of all 3 states |
| **W4** | Review part 1: hero count, not-counted pills ("Count it"), setup line, uncertain badge component, compact rep lines with tap-to-edit (ROM chips, Assist, Merge, Remove inside), Tier B line, bar line per the rules, remove form score / samples / PERFECT-MISS | `SetReviewOverlay.kt` (split into `ui/review/*.kt` to keep files small) | ~480 | Compose: Tier B set shows no "ROM"/"%"/"s" text anywhere and shows the explainer; sleeve unknown → no "level"/"twist" text; `accepted=false` rep → pill + "Count it" calls `acceptCandidate(index)`; uncertain tag → "?" + "Don't count" calls `removeRep`; hero count = accepted size; no "/100" anywhere. Roborazzi: Tier A and Tier B review, landscape + portrait | Bench set with a forced half rep: shows as not counted, Count it updates the hero; gyro-less check (debug flag or a phone without a gyro) |
| **W5** | Review part 2: right pane. Shared `WeightStepper`, notes restyle (localised char count), tracking-plan-1 label block placed and collapsed, action buttons as pixel-art (Confirm primary), Share trace slot, portrait sticky bar | `SetReviewOverlay.kt` / `ui/review/ReviewActions.kt`, new `ui/WeightStepper.kt`, `BetweenSetsWeightHud.kt` (replaced by `WeightStepper`), strings | ~400 | Compose: weight ± changes logged weight via `adjustWeight`; notes text survives rotation (`rememberSaveable`); Confirm is the last focus target and has the largest touch area; no hard-coded strings (lint `HardcodedText` clean). Roborazzi: right pane + portrait sticky bar | Full set: edit weight in review, add a note, confirm; check history shows both |
| **W6** | Rest + next exercise. `RestTimerCard` restyle, Up-next panel with `WeightStepper` + mount chip, next-exercise variant with highlighted chip when unset, **"+30 s" button** (decided by Lukas, 15 Oct): `WorkoutViewModel.extendRest(seconds = 30)` adds to the remaining rest (paused or running) and moves the autostart target | `RestTimerCard.kt`, `WorkoutScreen.kt`, new `ui/UpNextPanel.kt`, `WorkoutViewModel.kt` (`extendRest`), strings | ~390 | Compose: rest shows timer + Up next; when `currentSetNumber == 1 && exerciseIndex > 0` the "Next exercise" variant shows; unset sleeve on a bar lift → chip highlighted; autostart text matches the pref; "+30 s" has its a11y label and calls `extendRest` once per tap. VM (fake clock): 0:45 left + one tap → 1:15; works while paused; autostart fires at the new zero, not the old one. Roborazzi: rest and next-exercise, landscape + portrait | 2-exercise workout (bench → curl): chip shows for bench, hidden for curl; rest autostart works; +30 s twice → autostart 60 s later; screenshots |
| *later* | Live count in `LiveCountSlot` | its own plan after tracking item 10c | – | – | – |

**Totals:** about 2,390 changed lines over 6 code PRs (plus ~120 for W2a only if W2 has to create the shared panel), plus mockups. Generated files, screenshot baselines and PNGs aren't counted (`AGENTS.md`).

## Steps (per PR, for the builder)

1. Read this plan, the chosen mockup for the state, `AGENTS.md` and `GEMINI.md`.
2. Create a worktree from the latest `origin/main` (`docs/plans/README.md` §3).
3. Re-verify every file:line cited here against current `main`, because #176 and tracking plan 1 will have moved things.
4. Implement only that PR's row. No analysis, counting, DB or prefs-key changes (W3 reads the existing key).
5. Add the listed tests (JVM, Robolectric Compose semantics and Roborazzi screenshots), record the new baselines with `./gradlew :app:recordRoborazziDebug`, look at every new PNG, then run `./gradlew :app:testDebugUnitTest :app:lintDebug` from `Android/`. **Don't push until that's green** (decided by Lukas, 15 Oct). Never re-record someone else's baseline to make a test pass; if an old baseline changes, say why in the PR.
6. Take screenshots (landscape + portrait, plus a TalkBack walk-through note for W2/W3/W4), run `/review`, rebase, re-run the gate, and only then push and open a **draft** PR linking this plan. PR Scout reviews, and Lukas merges.

## Database changes

None. No Room change and no prefs-key change. W3's `mountSideAtStart` is in-memory `WorkoutState` only.

## Tests

- New JVM unit tests: `WorkoutDisplayRulesTest`, `MeasurementTierTest`, a `WorkoutViewModel` mount-snapshot test, and a `WorkoutViewModel` `extendRest` test with a fake clock (the VM is testable with fakes as today; if it isn't, put the snapshot and rest-extension logic in pure helpers).
- New Compose UI tests under Robolectric (`createComposeRule`) for W1 (smoke) and W2 to W6, using semantics (`onNodeWithText`, `onNodeWithContentDescription`, `assertDoesNotExist`). They need `@Config(qualifiers = "w891dp-h411dp-land")` for landscape cases. **Key negative assertions:** no "ROM", "%" or tempo text on Tier B; no "level"/"twist" text when the sleeve is unknown; no "/100" form score; no "PERFECT"/"MISS".
- **Device-free screenshot tests, decided by Lukas (15 Oct):** Roborazzi on Robolectric, set up in W1 (the first PR of the series). Every PR adds baselines for the states it changes, landscape + portrait, and every PR's tests must pass locally before it's pushed. No emulator, no instrumented tests.
- Run from `Android/`: `./gradlew :app:testDebugUnitTest :app:lintDebug`.

## Acceptance criteria

- [ ] The review's biggest element is the accepted rep count. Failed reps are visible and not in it, and uncertain reps are in it and flagged.
- [ ] On a set with `no_gyro`, no ROM, tempo, bar quality, tilt or twist text appears anywhere: not 0, not "even", not "–". The explainer line shows.
- [ ] With the sleeve unknown, no tilt/twist text appears. With it known, it appears only under the visibility table's conditions.
- [ ] The mount chip shows on bar-sleeve lifts in ready, rest and next-exercise. One tap flips L/R. Unset doesn't guess. It stays in sync with Settings. The set uses the side snapshotted at start.
- [ ] End workout is never one tap away during a set and always asks for confirmation.
- [ ] Nothing about counting changes: `ANALYSIS_VERSION` and `SetAnalyzerTest` are untouched.
- [ ] TalkBack labels exist for every control. Touch targets are at least 48dp. No hard-coded user-facing strings.
- [ ] Tests and lint are green locally and in CI, with no lint-baseline growth. Every PR added its own Robolectric Compose tests and Roborazzi baselines (landscape + portrait) and was green before it was pushed.
- [ ] "+30 s" on the rest screen extends the rest by 30 s per tap (paused or running) and autostart follows the new end.
- [ ] Screenshots in landscape + portrait, matching the chosen mockup, follow `GEMINI.md` (pixel-art buttons, no flat boxes, constrained plaques).
- [ ] Product rules hold: no network, no account needed.

## Risks

- **Double work with #176.** Its workout UI isn't on `main`. If the overhaul starts before the #176 slices land, one of them gets rebuilt. That's why open question 5 is needed.
- **Conflicts with tracking plan 1** in `SetReviewOverlay.kt` and `WorkoutScreen.kt`. Mitigation: plan 1 merges first, and W4/W5 place its label block.
- **Showing tilt that's wrong.** Today's level metric measures curl roll, not one-end-high. Recommendation: keep tilt/twist hidden until tracking item 9 (open question 1).
- **"Hidden" could look broken.** Mitigated by the one-line Tier B explainer and the "not set" chip text.
- **Robolectric + Compose on compileSdk 36:** pin `@Config(sdk = [35])` if needed, same as 176a-0.
- **Flaky or machine-dependent screenshots:** Roborazzi's native graphics can differ slightly between the builder's machine and CI (fonts, JDK). Mitigation: W1 pins the Robolectric SDK and a small compare threshold in the shared test base, and baselines are recorded on Linux like CI. If CI still differs, W1 is where it gets fixed, not each later PR.
- **Pixel-art rules vs legibility:** `GEMINI.md` asks for pixel-art buttons. The plan keeps them for actions and uses legible text for numbers. If a mockup breaks a `GEMINI.md` rule, the rule wins unless Lukas says otherwise.

## Out of scope

- Any counting, analysis, threshold, `ANALYSIS_VERSION` or sensor change (tracking plans).
- The live rep counter and audio/haptic cues.
- `PreWorkoutLoadoutScreen.kt`, the workout summary (`workoutResume/`), history, and the template picker (#177).
- Room or prefs migrations. Settings' `BarSleeveCard` stays as it is (W3 only reads and writes the same key).
- A second mount toggle for phone-top direction (only if the label data says it's needed).
- Gamification after a set (XP pop etc.). If wanted, it's its own plan and must not grade on unmeasured fields.

## Open questions (for Lukas, before approval)

1. **Tilt/twist before the bar-metric fix:** today's number measures the wrong axis. Hide tilt/twist everywhere until tracking item 9 ships, even with the sleeve set? (Recommended: yes. The chip still sets the side for traces.)
2. **Chip tap from "not set":** open a Left/Right popup, or cycle Not set → Left → Right on taps? Clearing back to "not set" by long-press + Settings is fine? (Recommended: popup, no guessing; long-press + Settings to clear.)
3. **Centre-mounted bar lifts (`BAR_CENTER`):** show the chip there too, or only on sleeve mounts? (Recommended: sleeve only, until there's a centre clip.)
4. **Weight control:** keep six plate chips (−5…+5), or one −/+ stepper with a step toggle (1.25 / 2.5 / 5)? (Recommended: stepper; it's fewer, bigger targets.)
5. **Order vs #176:** wait for #176's rest-timer, notes and unilateral slices to land on `main` first, or let the overhaul own those UIs and drop them from #176? (Recommended: wait for W2 to W6. W1 can go now, see 9.)
6. **Screenshot tests:** **Decided by Lukas (15 Oct).** Device-free Compose UI tests (Robolectric + Paparazzi or Roborazzi) are required. They're set up in the first UI PR (W1), and every PR carries its own tests, which must pass before the branch is pushed. (Replaces the 13 Oct wording; same rule.)
7. **Rest "+30 s" button:** **Decided by Lukas (15 Oct): in.** Built in W6 (`extendRest`, a small VM change).
8. **Antigravity model for the build:** **Decided by Lukas (15 Oct): Gemini 3.8 Flash High** in Antigravity for W1 to W6. Claude only writes plans.
9. **Screenshot library and W1 timing:** Roborazzi or Paparazzi, and may W1 be built now, ahead of the #176 split (it touches no hotspot file and unblocks the home screen plan)? (Recommended: Roborazzi, because it shares the Robolectric runtime with the semantics tests; and yes, build W1 now.)
