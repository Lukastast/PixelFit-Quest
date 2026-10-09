# AGENTS.md: PixelFit Quest

This file is for every AI coding tool on this repo (Antigravity/Gemini, Grok Build, Grok bots, Cursor, Claude, Codex). Read it before you plan or change anything. UI and pixel-art rules are in [`GEMINI.md`](GEMINI.md) and apply to every tool, not only Gemini.

## Project overview

PixelFit Quest is a gamified, offline-first strength-training app for Android. You log workouts, the phone's IMU analyses reps, and you earn XP, coins, streaks, achievements, cosmetics and dwellings for a pixel-art character.

- Single Gradle module: `:app`, package `com.pixelfitquest`
- Kotlin, Jetpack Compose (Material 3), Hilt DI, Room, Navigation Compose, WorkManager, Health Connect
- Firebase Auth (optional Google sign-in) and Firestore (Pro-only cloud features, stubbed off today)
- minSdk 29, target/compileSdk 36, AGP 9.4, Kotlin 2.3, JDK 17 toolchain
- Hobby project. Owner: Lukas (`Lukastast`).

## Where and how to run things

Gradle lives in `Android/`, not the repo root. Always `cd Android` first.

Requirements: JDK 17 (`JAVA_HOME`), Android SDK with `platforms;android-36` and `build-tools;36.0.0` (`ANDROID_HOME`, or `sdk.dir` in `Android/local.properties`, which is gitignored). The Gradle daemon may provision a JDK 21 for itself (see `Android/gradle/gradle-daemon-jvm.properties`); the app still compiles as 17.

| What | Command (from `Android/`) |
|---|---|
| Unit tests (what CI runs) | `./gradlew :app:testDebugUnitTest` |
| One test class / package | `./gradlew :app:testDebugUnitTest --tests "*SetAnalyzerTest"` |
| Android lint (what CI runs) | `./gradlew :app:lintDebug` |
| Compile check only | `./gradlew :app:compileDebugKotlin` |
| Debug APK | `./gradlew :app:assembleDebug` |
| Full local gate before a PR | `./gradlew :app:testDebugUnitTest :app:lintDebug` |

- Lint uses a baseline (`Android/app/lint-baseline.xml`). Existing issues are recorded there; new issues fail the build. Do not regenerate or grow the baseline to make your change pass. Fix the issue. Only Lukas decides to re-baseline.
- There are no instrumented (`androidTest`) tests yet. All tests are JVM unit tests under `Android/app/src/test/`.
- Do not run release builds, signing, or Play uploads. See `SIGNING.md`. Never commit keystores, passwords or `local.properties`.

## Module and package map

All source is in `Android/app/src/main/java/com/pixelfitquest/`. Tests mirror the same packages in `Android/app/src/test/java/com/pixelfitquest/`.

| Package | What lives there |
|---|---|
| `MainActivity.kt`, `PixelFitQuest.kt` | Activity entry point and `@HiltAndroidApp` Application |
| `ui/navigation/` | `AppScaffold` (bottom nav, textured bars), `AppState`, routes in `PixelFitRoutes.kt`, `SessionRouter` |
| `ui/theme/` | Colors, typography, `Spacing.kt` (`spacing.scale(...)`, `PixelFitWidthClass` for responsive layout) |
| `ui/OrientationLock.kt` | Orientation helpers |
| `components/atoms/` | Small reusable pieces: `PixelArtButton`, sprite players, character animations |
| `components/molecules/` | Reusable cards: settings, account, workout, Health Connect, god mode |
| `feature/<name>/` | One folder per feature. Screen and ViewModel at the folder root, plus optional `model/` (pure Kotlin domain logic), `data/` (Room entities, DAOs, repository), `ui/`, `di/` (Hilt module) |
| `feature/workout/` | Live workout flow. `analysis/` is the pure-Kotlin rep/set analysis (`SetAnalyzer`, `ExerciseProfiles`, `Tempo`, `BarImbalance`), `sensor/` is IMU capture (`SensorSession`), `catalog/` is the exercise catalog, `orientation/` handles workout orientation |
| `feature/workoutBuilder/`, `workoutResume/`, `workouts/` | Templates and template picker, set review/resume, workout history |
| `feature/home/`, `customization/`, `levels/`, `progression/`, `streak/`, `achievements/`, `missions/` | Game layer: dwelling scene, character cosmetics, XP/levels, skill tree and rewards, weekly streaks, achievements, daily missions |
| `feature/health/`, `healthbonuses/`, `bodyMetrics/` + top-level `health/` | Health Connect integration, permissions, and the Health/Quest Center rewards |
| `feature/settings/`, `intro/`, `splash/`, `progress/` | Settings, onboarding, splash, lift progress charts (`progress/data` = lift history) |
| `local/` | Source of truth. `db/PixelFitDatabase.kt` (Room DB, migrations), `db/dao/`, `db/entity/`, `LocalPixelFitStore` (main local store), `export/` (free JSON/CSV export), `CloudSyncPolicy` and `CloudBackup` (Pro gate, stubbed off) |
| `firebase/` | Auth (`service/`), Hilt `di/AppModule.kt` (provides the Room DB and most singletons), `model/`. Note: `firebase/repository/*Repository` are mostly thin wrappers over `LocalPixelFitStore`, despite the package name |
| `viewmodel/` | App-wide ViewModels (`PixelFitViewModel`, `GlobalSettingsViewModel`) |
| `helpers/` | Snackbar manager, error types, text helpers |
| `debug/` | God mode prefs (debug tooling only) |

Other top-level folders: `docs/` (GitHub Pages: `index.html`, `privacy.html`; plus `plans/` and `prompts/` for the AI loop), `res/` (source SVGs), `scripts/` (Python pixel-art generators for achievement badges).

## Coding conventions

- Kotlin official code style. Follow the style of the file you are in.
- Compose screens take state and callbacks; logic lives in a `@HiltViewModel`. Reusable cards accept `modifier: Modifier = Modifier`.
- Put game rules and math in pure Kotlin (`model/`, `analysis/`) with no Android imports, so JVM unit tests can cover them. New logic needs unit tests there.
- Inject dependencies with Hilt (`@Inject constructor`, modules in `di/`). No manual singletons.
- Persist through Room via `LocalPixelFitStore` or a feature repository. Never call Firestore directly from UI or ViewModels.
- Use pixel-art assets from `res/drawable` for buttons and plaques, never flat colored boxes (see `GEMINI.md`).
- Do not add new libraries without saying why in the PR. Versions go in `Android/gradle/libs.versions.toml`.
- No dead code, commented-out blocks, or stray debug logs in a PR.

## Product rules (durable)

1. **Offline by default.** Room on the phone is the source of truth. Never block starting a workout on login or network.
2. **Cloud is Pro.** Cloud sync, automatic Firebase backup, multi-device, and leaderboards are PixelFit Pro (paid) features. Do not wire free always-on Firestore sync. Gate cloud calls through `CloudSyncPolicy` (currently `StubCloudSyncPolicy`, which keeps them off).
3. **Sign-in is optional.** Google Sign-In may exist as an opt-in account link. Pro unlocks sync, not the login button itself. Never force an auth screen.
4. **Export is free.** Local JSON/CSV export must stay free and work without an account.
5. **Email/password auth is not the v1 path.** Email/password and forgot-password are optional, later.
6. When you touch settings, login, or Firebase DI, check your change against rules 1 to 5 and say so in the PR under "Product alignment".

## Database rules (Room)

The DB (`local/db/PixelFitDatabase.kt`, built in `firebase/di/AppModule.kt`) has real user data on devices.

- **Every schema change needs a `Migration` and a test.** Bump `version`, add `MIGRATION_N_N+1` next to the others, register it in `AppModule.addMigrations(...)`, and add a unit test that covers the migration SQL.
- **No destructive fallback for new versions.** Never add or rely on `fallbackToDestructiveMigration()` to get past a schema change. The existing call is legacy and is to be removed in its own PR once schema export is on. Do not add new destructive paths.
- Prefer additive changes (`ALTER TABLE ... ADD COLUMN ... NOT NULL DEFAULT ...`). Renames and drops need a plan that Lukas approved.
- `exportSchema` is currently `false`. Turning it on (with `room.schemaLocation` and `MigrationTestHelper` tests) is a planned separate PR. Do not mix it into feature work.

## How work flows (the loop)

Plan, approve, build, review, merge. Details and templates are in [`docs/plans/README.md`](docs/plans/README.md).

1. A plan is written to `docs/plans/<issue>-<slug>.md` from `docs/plans/TEMPLATE.md` (by Antigravity or the PixelFit bot).
2. Lukas approves the plan.
3. The builder (usually Grok Build) implements exactly that plan in its own git worktree and branch.
4. Self-review (`/review`), local gate green, then a PR.
5. PR Scout reviews and CI runs. Lukas merges.

If you were not given a plan file and the task is bigger than a small fix, stop and write a plan first.

## Pull request rules

- **One issue per PR.** Link it (`Closes #123`) and the plan file.
- **Small.** Aim for under ~500 changed lines (excluding generated files, baselines and binary assets). If it grows past that, split it into stacked PRs or stop and ask.
- **Conventional commits:** `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `ci:`, `build:`. Optional scope, e.g. `feat(workout): ...`. Imperative mood, under ~72 chars.
- **Branch names:** `feat/<slug>`, `fix/<slug>`, `chore/<slug>`, based on the latest `origin/main` unless the plan says otherwise.
- **Never push to `main`.** Always a branch and a PR.
- **Rebase on `origin/main` before opening or updating a PR** and fix conflicts yourself. Do not merge `main` into your branch in a way that drags unrelated changes in.
- PR body: what and why, how you tested it, screenshots for UI changes, anything you did not do, product alignment if relevant.

## Parallel agents

- Only run agents in parallel when their plans touch **separate files**. If two plans touch the same file (a common hotspot: `PixelFitDatabase.kt`, `AppModule.kt`, `AppScaffold.kt`, `PixelFitRoutes.kt`, `LocalPixelFitStore.kt`), run them one after the other.
- One worktree and one branch per agent. Never share a working directory.
- Each agent rebases on the latest `origin/main` right before its PR. Merge order is decided by Lukas or the PixelFit bot.
- Only one agent may bump the Room DB version at a time.
- Clean up your worktree after the PR merges (`git worktree remove <path>`).

## Definition of done

A task is done only when all of this is true:

- [ ] `./gradlew :app:testDebugUnitTest :app:lintDebug` passes locally (from `Android/`), and CI is green.
- [ ] New or changed logic has unit tests. A bug fix has a test that failed before the fix.
- [ ] No new lint issues and no baseline growth.
- [ ] Room changes follow the database rules above.
- [ ] UI changes follow `GEMINI.md` and the PR has screenshots (portrait and landscape where the layout differs).
- [ ] The PR follows the PR rules and has a short summary: what changed, why, how it was tested, what was left out.
- [ ] The plan's acceptance criteria are met, and nothing outside the plan's scope was changed.

## Safety rules (plain English)

- Never push to `main`. Never force-push a branch someone else is working on.
- Never merge a PR without green CI and a review. Only Lukas merges.
- Never touch anything related to Trifork (Lukas's job). This repo and its agents are for hobby projects only.
- Never change branch protection, repo settings, secrets, or GitHub Actions secrets.
- Never commit secrets, keystores, `google-services` changes from another project, or `local.properties`.
- Never upload to the Play Store or run release signing.
- Never delete user data paths or add destructive DB fallbacks.
- If something is unclear or outside the plan, stop and ask instead of guessing.
