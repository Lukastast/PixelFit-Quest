# Grok Build feature prompt (headless template)

Copy this into a `prompt.md`, fill the `<...>` parts, and run it from the worktree:

```bash
grok --cwd <worktree> --max-turns 150 --prompt-file prompt.md --output-format streaming-json > build.log
```

- Default `--max-turns` is too low for multi-file features. Use ~150 for a normal plan. For a large plan, run it in phases (one phase per run, or `grok --continue` with a short "continue from phase N" prompt) and push after each phase.
- `--worktree` does nothing with `-p`/`--prompt-file`. Create the worktree with `git worktree add` first.
- Prefer not to use `--always-approve` for open-ended work.

---

You are implementing one approved plan in this git checkout of PixelFit Quest.

Checkout: <absolute path of the worktree>
Branch already created: <feat/slug>
Base: origin/main at <short sha>
Plan: docs/plans/<issue>-<slug>.md  (read it fully first; it is the scope)
Issue: #<number>

Gradle project: Android/ (run ./gradlew from Android/)
JAVA_HOME=<path to JDK 17>
ANDROID_HOME=<path to Android SDK>
PATH must include $JAVA_HOME/bin

Read AGENTS.md and GEMINI.md before you change anything. They are binding.

## Hard rules
- Implement only what the plan says. If the plan is wrong or something is missing, stop and write the problem into the PR body instead of improvising a bigger change.
- Verify current behaviour in the code before you change it. Do not assume a feature already exists.
- Commit after each coherent slice with conventional commits (feat:, fix:, test:, refactor:, docs:, chore:). Push often so a turn cap does not lose work: git push -u origin <feat/slug>
- As soon as the first slice compiles and its tests pass, open a DRAFT pull request into main. Update the PR body as you go. Do not merge.
- Never push to main. Never force-push. Never edit .github/workflows, branch protection, secrets, or signing. No Play uploads.
- Room: any schema change needs a Migration registered in AppModule and a test. Never add destructive fallback.
- Keep the diff under ~500 changed lines. If you are heading past that, stop at a clean slice and say what is left.
- Never touch anything Trifork-related.
- Do not invent facts, prices, medical claims, or SDK behaviour.
- Existing tests may only be changed if the plan says so or they encode a proven bug. Say so in the commit and the PR. Do not silently weaken a test.

## Locked decisions (do not ask, do not wait)
<decisions copied from the plan's approved answers>

## What to ship, in order
If you run out of turns, this order is the priority. Push after each.
1. <phase / slice 1 from the plan>
2. <phase / slice 2>
3. <...>

## Do not do
<copied from the plan's "Out of scope">

## Before you finish
From Android/:
./gradlew :app:testDebugUnitTest :app:lintDebug
Fix failures you caused. Do not add entries to app/lint-baseline.xml.
Then run /review on your diff against origin/main and fix what it finds.
git fetch origin && git rebase origin/main, rerun the gate, push.

In the PR body list: what you changed, how you tested it, screenshots or "no UI change", what you deliberately did not do, any tests you revised and why, and "Product alignment" if you touched settings, login or Firebase.

When the PR is up to date and the plan's acceptance criteria are met, stop. The PR URL is the result that matters.
