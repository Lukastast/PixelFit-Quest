# Plan: <short title>

<!--
Copy this file to docs/plans/<issue>-<slug>.md (e.g. docs/plans/184-template-picker-search.md).
One plan = one issue = one PR. Keep the expected diff under ~500 changed lines; split otherwise.
The builder implements exactly this plan. Anything not listed here is out of scope.
-->

- **Issue:** #<number> <link>
- **Status:** draft | approved by Lukas (<date>) | built in PR #<n>
- **Author:** <Antigravity / PixelFit bot / Lukas>
- **Builder:** <Grok Build / Antigravity>
- **Branch:** `feat/<slug>` from latest `origin/main`
- **Expected size:** ~<n> changed lines

## Goal

<1 to 3 sentences: what the user can do after this ships, and why it matters.>

## Context

<What exists today, with file paths. Things the builder must verify in code before changing them. Links to related PRs or plans.>

## Files touched

| File | Change |
|---|---|
| `Android/app/src/main/java/com/pixelfitquest/...` | <new / modify: what> |
| `Android/app/src/test/java/com/pixelfitquest/...` | <new test> |

<If any of these is a hotspot (PixelFitDatabase.kt, AppModule.kt, AppScaffold.kt, PixelFitRoutes.kt, LocalPixelFitStore.kt), say so: this plan must not run in parallel with another plan touching it.>

## Steps

1. <small, checkable step>
2. <...>
3. <...>

## Database changes

<None. / Version N to N+1: exact columns, MIGRATION_N_N+1 SQL, registered in AppModule, migration test. No destructive fallback.>

## Tests

- <Unit test class and the cases it covers. For a bug: the test that fails before the fix.>
- Run from `Android/`: `./gradlew :app:testDebugUnitTest :app:lintDebug`

## Acceptance criteria

- [ ] <observable behaviour 1>
- [ ] <observable behaviour 2>
- [ ] Tests and lint green locally and in CI, no lint-baseline growth
- [ ] UI change: screenshots (portrait + landscape if layout differs) in the PR, follows GEMINI.md
- [ ] Product rules in AGENTS.md still hold (offline-first, cloud is Pro, export free)

## Out of scope

- <things the builder must not do in this PR, even if tempting>

## Open questions

- <anything Lukas must decide before approval. Empty before approval.>
