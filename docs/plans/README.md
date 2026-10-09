# The PixelFit AI coding loop

Every non-trivial change goes through the same loop. It keeps plans visible, PRs small, and review real.

```
 plan  ->  approve  ->  build (worktree)  ->  /review  ->  PR  ->  PR Scout + CI  ->  Lukas merges
```

## Who does what

| Step | Who | Output |
|---|---|---|
| 1. Plan | Antigravity (UI-heavy work) or the PixelFit bot (product owner) | `docs/plans/<issue>-<slug>.md` from [`TEMPLATE.md`](TEMPLATE.md) |
| 2. Approve | Lukas | Plan status set to "approved", open questions empty |
| 3. Build | Grok Build CLI (multi-file features) or Antigravity (UI iteration with screenshots) | Commits on `feat/<slug>` in its own worktree |
| 4. Self-review | The builder, with Grok Build's `/review` (or `code-review`) | Fixes before the PR |
| 5. PR | The builder | One PR into `main`, linked to the issue and plan |
| 6. Review + CI | PR Scout + GitHub Actions (unit tests, lint) | Review comments, green checks |
| 7. Merge | Lukas only | Merge, then delete the branch and worktree |

Opus (or another expensive model) is only for hard plans or a second-opinion review, not for bulk building.

## 1. Plan

- Start from an issue. One issue, one plan, one PR.
- Copy `TEMPLATE.md` to `docs/plans/<issue>-<slug>.md` and fill every section. List the exact files to touch.
- Keep the expected diff under ~500 changed lines. If it is bigger, split it into several plans and number them.
- Antigravity: write the plan into this folder, not only into `~/.gemini/antigravity/brain/`.
- Commit the plan on the feature branch (or on its own `docs/plan-<issue>` branch) so the builder, PR Scout and Lukas all read the same file.

## 2. Approve

Lukas reads the plan and says yes, no, or what to change. Nothing gets built from an unapproved plan.

## 3. Build in a worktree

From the repo root on your PC:

```bash
git fetch origin
git worktree add ../pfq-<slug> -b feat/<slug> origin/main
cd ../pfq-<slug>
```

Interactive Grok Build:

```bash
grok --cwd ../pfq-<slug>
# then: /execute-plan docs/plans/<issue>-<slug>.md
```

Or `grok --worktree=<slug> --worktree-ref origin/main` to let Grok Build create the worktree itself (interactive mode only).

Headless Grok Build (the worktree flag does not apply to `-p`, so create the worktree first as above):

```bash
grok --cwd ../pfq-<slug> --max-turns 150 --prompt-file prompt.md --output-format streaming-json > build.log
```

Fill `prompt.md` from [`../prompts/grok-build-feature.md`](../prompts/grok-build-feature.md). Avoid `--always-approve` for open-ended work; prefer `--permission-mode acceptEdits` plus reviewing commands, or keep runs short and phased.

Big plans: split into phases and run one phase per session (or `grok --continue`), committing and pushing after each phase so a turn cap never loses work.

## 4. Self-review

Before the PR, in the same worktree:

- `./gradlew :app:testDebugUnitTest :app:lintDebug` from `Android/`
- Grok Build `/review` on the diff against `origin/main`; fix what it finds
- `git fetch origin && git rebase origin/main`, then rerun the gate

## 5. Open the PR

```bash
git push -u origin feat/<slug>
gh pr create --base main --title "feat: <summary>" --body-file pr.md
```

The PR body: what and why, `Closes #<issue>`, link to the plan, how it was tested, screenshots for UI, what was left out, product alignment if settings/login/Firebase were touched.

## 6. Review and CI

- CI (`.github/workflows/unit-tests.yml`) runs debug unit tests and Android lint on every PR.
- PR Scout reviews the PR. Address its comments with new commits on the same branch.

## 7. Merge

Only Lukas merges, and only when CI is green and the review is addressed. Afterwards:

```bash
git worktree remove ../pfq-<slug>
git branch -d feat/<slug>
```

Set the plan's status to "built in PR #<n>" (or leave it; the PR links it).

## Parallel work

Several builders can run at once only if their plans touch separate files. See "Parallel agents" in [`AGENTS.md`](../../AGENTS.md).
