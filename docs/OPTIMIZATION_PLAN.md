# PixelFit-Quest Optimization Plan

**Branch:** `feat/performance-and-workout-notes` (draft PR #176)  
**Scope:** Ranked plan only — do **not** change rep/ROM/`SetAnalyzer`/`Signal`/`FullRomStore` logic without a dedicated tracking-sensitive review. No Play upload.  
**Repo checkout:** `/workspace/PixelFit-Quest` @ `95654fb`

---

## Already done (this branch / recent history)

| Item | Evidence | Notes |
|------|----------|--------|
| Move clips / characters / dwellings / gyms → `drawable-nodpi` | `res/drawable-nodpi` ≈ **27 MB**, 77 PNGs | Pixel art no longer density-scaled for those assets |
| Re-enable Kotlin/KSP incremental | Commit `17dace9` removed `kotlin.incremental=false` + `ksp.incremental=false` | Default incremental is back on |
| Drop live Good/Miss overlay + 3-2-1 countdown | `#170` / `#171` | Less Compose work during Recording |
| Sitting face flash fix | `#168` / `SpriteSheetPlayer` clipRect path | Correctness, not throughput |
| `onRecordingTick` throttle | `WorkoutViewModel.onRecordingTick`: `count % 8 != 0` return | Already reduces UI updates ~8× vs every sample |

### Incomplete / leftover from nodpi move

- **Furniture:** `DwellingVisuals.furnitureRes` always returns `null`; four `dwelling_*_furniture.png` in `drawable/` were **1×1 stubs** (dead). Removed in this pass (see “Trivial cleanups”).
- **UI chrome still in density-scaled `drawable/`:** navbar, slides, quest boards, achievement badges, buttons, etc. On xxxhdpi these decode at **4×** linear size.

---

## Ranking key

| Priority | Meaning |
|----------|---------|
| **P0** | High user-visible jank / memory / battery during core workout or home; do next |
| **P1** | Clear win, medium risk or needs design; after P0 |
| **P2** | Build hygiene / structure / future; lower urgency |

**Model guidance (as requested):**

- **Tracking-sensitive** (IMU rate, interpolate, `SetAnalyzer`/`Signal`, buffer semantics) → **Grok high**
- **UI polish / Compose / assets** → **Antigravity when quota**; else Grok high OK for pure refactors
- **Pure refactor / build flags** → OK on **high** (any)

**Risk to rep tracking:** Low / Medium / High — High means can change accepted-rep counts or ROM scores.

---

## P0 — Do next

### P0-1. Split Recording UI state from monolith `WorkoutState`
- **Where:** `WorkoutViewModel.WorkoutState` + `WorkoutScreen` (`collectAsState` on whole state)
- **Problem:** Every `onRecordingTick` copies full `WorkoutState` (phase, weight, review, rest fields…). Rest timer `tickRest(200)` does the same **5×/s**. Entire tree (gym BG, `CharacterIdleAnimation`, buttons) recomposes.
- **Change:** Separate flows: `recordingHud: StateFlow<Pair<Int,Float>>`, `restRemainingMs: StateFlow<Long>`, keep phase/review on slower flow. Or `derivedStateOf` / child composables that take only needed params + `key(phase)`.
- **Impact:** Large drop in Recording/Rest jank; less GC from Compose.
- **Risk to reps:** **Low** (UI wiring only).
- **Model:** Antigravity (UI) or Grok high (refactor).

### P0-2. Stop full-sheet decode + infinite bob on Recording hot path
- **Where:** `CharacterIdleAnimation` / `IdleAnimation` (`ImageBitmap.imageResource` every composition path); `WorkoutScreen` `rememberInfiniteTransition` bob while Recording
- **Problem:** Idle sheets are **6240×480** ≈ **1.4 MB ARGB** each; workout clips **3840×480** ≈ **7 MB**. Decode via `imageResource` with no subsample. During Recording, infinite bob + 10 fps frame ticker fight IMU→StateFlow updates.
- **Change:** `remember(spriteSheetId) { ImageBitmap.imageResource(...) }`; consider static pose or paused anim while Recording; prefer `SpriteSheetPlayer` path; optional subsample / half-res display sheets later.
- **Impact:** High memory + frame-time win on workout screen.
- **Risk to reps:** **None**.
- **Model:** Antigravity when quota.

### P0-3. Move high-res pixel chrome out of density-scaled `drawable/`
- **Where:** especially `navbar.png` (1200×168), `navbar_vertical.png` (168×1200), `slide*.png` (500×500), `questloginboard*.png`, large `info_background_*`
- **Problem:** In default `drawable/`, Android treats them as mdpi and **upscales** → navbar ≈ **12 MB** decode on xxxhdpi; slides ≈ **15 MB**. Loaded in `AppScaffold` via `ImageBitmap.imageResource`.
- **Change:** Move pixel-art UI to `drawable-nodpi` (same pattern as characters). Keep tiny system-ish icons if density-aware is desired.
- **Impact:** Large APK-runtime memory win on high-dpi phones; sticky across all tabs.
- **Risk to reps:** **None** (visual scale may change slightly — verify navbar hit targets).
- **Model:** Antigravity / pure asset move OK on high.

### P0-4. SensorSession alloc + main-thread listener pressure (careful) — **DONE (alloc-only)**
- **Where:** `SensorSession.onSensorChanged` — was `ArrayList += TimedVec3/TimedQuat` under lock; prefers `SENSOR_DELAY_FASTEST` for **4** listeners (accel, gyro, rot, linear); `onAccelTick` → UI
- **Problem:** At FASTEST, tens of thousands of small objects/min; lock contended; UI already throttled but sensor thread still allocates. Buffers grow unboundedly for long sets (pre-size 4096 then grow).
- **Done:** SoA `TimedVec3Buffer` / `TimedQuatBuffer` — primitive storage during recording, `add` (no `+=`), copy `event.values` to locals once, `clear()` reuses capacity; box to `TimedVec3`/`TimedQuat` only in `snapshotInterpolated`. Rates / sensors / interpolate unchanged.
- **Deferred (tracking review):** capped drop-oldest ring by max duration; lowering rate; dropping gyro/rot/linear; changing `EDGE_NS`.
- **Impact:** Medium–high jank/GC during long sets; battery secondary.
- **Risk to reps:** **Low** (alloc-only; sequence-preserving buffer tests).
- **Model:** **Grok high** (tracking-adjacent).

---

## P1 — After P0

### P1-1. Rest timer UI isolation — **DONE**
- **Where:** `WorkoutScreen` `LaunchedEffect` + `RestTimerHud` / `RestTimerCard`
- **Done:** Dedicated `restRemainingMs` flow + leaf collector (with P0-1); tick **500 ms** (was 200 ms; display is second-granularity).
- **Impact:** Rest-phase jank gone; tiny battery.
- **Risk:** **Low** (don’t change autostart/finish transitions).
- **Model:** Antigravity / high.

### P1-2. Bitmap decode policy (Coil or Options)
- **Where:** Coil is on classpath (`coil-compose`) but sprites use raw `ImageBitmap.imageResource` / `painterResource`
- **Change:** For full-bleed backgrounds (gym 1376×768 ≈ 4 MB ARGB, dwellings ≈ 3.5 MB), decode with `inSampleSize` targeting view size, or Coil `size` + RGB_565 where banding OK. Cache one gym BG per screen.
- **Impact:** Home/workout peak RSS down materially.
- **Risk:** **Low** (visual); keep FilterQuality.None for sprites.
- **Model:** Antigravity.

### P1-3. APK dead / deferred assets — **DONE (safe subset)**
- **`cape_hero_walk.png`:** still in APK / manifest with `eagerLoad: false`; runtime walk remains bob+slide (`PixelCharacterMotion`) — **not decoded**.
- **Coaching clips:** `FormClipVisual` now `remember`-caches `ImageBitmap` per resolved tag (no re-decode on recomposition).
- **Skipped here:** strip walk from APK / WebP / splash policy (optional follow-ups).
- **Impact:** Avoid accidental walk decode + cheaper clip recomposition.
- **Risk:** **None**.
- **Model:** high / Antigravity.

### P1-4. `FLAG_KEEP_SCREEN_ON` scope — **DONE**
- **Where:** `WorkoutOrientationLock`
- **Done:** Keep-screen-on only while `Recording` or `Resting`; cleared on Review/Idle and on leave.
- **Impact:** Battery during long review/notes.
- **Risk:** **None** to reps.
- **Model:** high.

### P1-5. Health Connect read cadence & shape — **DONE**
- **Where:** `HomeScreen` / `HomeViewModel` / `HealthConnectRepository`
- **Done:** Dropped 30 s Home poll; refresh on initialize + `ON_RESUME` with **5 min TTL** cache; resting-HR pagination capped (`pageSize=1000`, max 3 pages / 2500 samples). Health bonus timing may lag by up to TTL.
- **Impact:** Battery + home hitch when returning to app.
- **Risk:** **None** to reps.
- **Model:** Grok high (logic) / Antigravity (UI trigger).

### P1-6. Enable R8 minify + resource shrink (release only) — **DONE**
- **Where:** `app/build.gradle.kts` + `proguard-rules.pro`
- **Done:** `isMinifyEnabled = true`, `isShrinkResources = true` for release; keep rules for Gson/Firestore/Room/Hilt/Health Connect (+ Credential Manager). Device smoke test still recommended before Play.
- **Impact:** APK size / some methoding; not runtime jank.
- **Risk:** **Medium** (reflection/crash) — not rep math, but ship risk.
- **Model:** high; validate on device.

### P1-7. Gradle throughput — **DONE**
- **Where:** `Android/gradle.properties`
- **Done:** `org.gradle.parallel=true`, `org.gradle.caching=true`. Configuration-cache still deferred.
- **Impact:** Dev build time only.
- **Risk:** **None** to app.
- **Model:** high (trivial).

---

## P2 — Later / structural

### P2-1. AGP / dependency audit
- **Current:** AGP **9.4.0**, Kotlin **2.3.20**, KSP **2.3.6**, Compose BOM **2026.03.00**, Room 2.8.4, HC 1.1.0 — already modern.
- **Change:** Periodic BOM bumps; remove unused deps if Coil stays unused for sprites; confirm Tag Manager necessity.
- **Impact:** Build/security hygiene.
- **Risk:** Low–Medium (compat).
- **Model:** high.

### P2-2. Unused resource sweep
- After R8 shrink, run Android Studio unused-resources / `shrinkResources` report; delete true dead drawables (furniture stubs already removed).
- **Model:** high / Antigravity.

### P2-3. Module structure
- Single `:app` (~264 KT files). Optional extract `:core-imu`, `:feature-workout`, `:feature-home` only if build times or ownership demand it — **not** required for memory/jank.
- **Risk:** High churn; **avoid** while tuning reps.
- **Model:** high (architect).

### P2-4. SetAnalyzer / Signal hot path (post-UI)
- Analysis runs **once** at `finishSet` (not per sample) — good. Cost is O(samples) in `Signal.prepare` / `segmentCycles` + interpolate at snapshot.
- Future: reuse buffers; avoid `samples.toList()` copy; profile before changing thresholds/`ANALYSIS_VERSION`.
- **Risk to reps:** **High**.
- **Model:** **Grok high only**, with golden IMU fixtures / unit tests.

### P2-5. Sensor rate policy (explicit product decision)
- Manifest has `HIGH_SAMPLING_RATE_SENSORS`; code tries FASTEST → 200 Hz → GAME.
- Cap at **100–200 Hz** *only* after comparing rep detection on fixture traces.
- **Risk to reps:** **High**.
- **Model:** **Grok high**.

---

## Suggested execution order

1. **P0-1 + P0-2** (Recording jank) — UI, no analyzer touch  
2. **P0-3** (navbar/slides → nodpi) — verify UI scale  
3. ~~**P0-4 alloc-only** SensorSession (no rate change)~~ **done**  
4. ~~**P1-1, P1-4, P1-5** (rest / screen-on / HC)~~ **done**  
5. ~~**P1-3** (lazy clips / walk not eager)~~ **done** (safe subset); **P1-2** skipped (subsample/Coil)  
6. ~~**P1-6, P1-7** (R8 + Gradle)~~ **done** (device smoke before Play)  
7. **P2-4 / P2-5** only with fixture A/B on rep counts  

---

## Trivial cleanups applied (this pass)

1. Deleted unused **1×1** furniture stubs:  
   `drawable/dwelling_{tent,shack,cottage,castle}_furniture.png`  
   (`furnitureRes` already returned `null`; no R references.)
2. Deleted unreferenced **`drawable-nodpi/background_home_screen1.png`** (1800×3200 ≈ 22 MB decode if ever loaded; zero code refs).

## Implemented on this branch (follow-up commits)

| Item | Status |
|------|--------|
| **P0-1** Recording/rest HUD split (`recordingHud` + `restRemainingMs` flows; leaf collectors) | Done |
| **P0-2** `remember` sprite sheet decode; pause idle anim + bob during Recording | Done |
| **P0-3** navbar / slides / quest boards / info_background* → `drawable-nodpi` | Done |
| **P0-4** SensorSession SoA stream buffers (no per-sample TimedVec3/Quat; clear reuses capacity) | Done |
| Trivial asset deletes (furniture stubs, `background_home_screen1`) | Done |
| **P1-1** Rest HUD isolation (P0-1) + 500 ms tick | Done |
| **P1-3** Walk not eager; `FormClipVisual` remember-cache decode | Done (safe subset) |
| **P1-4** `FLAG_KEEP_SCREEN_ON` only Recording/Resting | Done |
| **P1-5** Drop 30 s HC poll; 5 min TTL; cap resting-HR pages | Done |
| **P1-6** Release minify + shrinkResources + keep rules | Done |
| **P1-7** Gradle parallel + caching | Done |
| **P1-2** Subsampled decode / Coil | **Skipped** (risky without Coil wiring) |

**Not touched:** `SetAnalyzer`, `Signal`, SensorSession rates / registered sensors / interpolate edge, Play upload. Capped drop-oldest ring deferred (would change long-set samples).

---

## Quick metrics (branch snapshot)

| Bucket | Size / count |
|--------|----------------|
| `drawable/` | ~728 KB, was 91 files (−4 furniture) |
| `drawable-nodpi/` | ~27 MB, 77→76 files (−1 unused BG) |
| Release minify | **on** (+ shrinkResources) |
| Modules | `:app` only |
| Sensor policy | FASTEST preferred, 4 sensors |
| Keep screen on | Recording + Resting only |
| HC refresh | initialize + resume, **5 min TTL**; no 30 s loop |
