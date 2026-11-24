---
name: code-review
description: >
  Performs a project-aware code review. Invoke with /code-review to review
  staged changes, a specific file, or a module. Checks project conventions
  (Bark logging, Koin DI, architecture layers, KMP boundaries) in addition
  to general code quality.
allowed-tools: Read, Grep, Glob, Bash
---

# Code Review Skill

Performs a structured review grounded in this project's conventions.
It is NOT a generic linter — it checks things the compiler can't catch.

**All checklist sections are diff-scoped.** Only apply a section if the diff
actually contains the relevant code — don't go hunting outside the diff.

---

## Step 1 — Determine Scope

If the user invoked `/code-review` with no arguments, ask:

> "What should I review?
>
> 1. **Staged changes** — what's been `git add`-ed and is ready to commit
> 2. **Unstaged changes** — work in progress that hasn't been staged yet
> 3. **This branch vs `develop`** — every commit on the current branch that hasn't been merged yet
> 4. **The last commit** — exactly what changed in the most recent commit (useful for a post-commit sanity check before pushing)
> 5. **A specific file or path** — read one or more files directly"

If the user provided a path or flag, use that directly. Proceed once scope is clear.

---

## Step 2 — Gather the Diff

Depending on scope, run ONE of:

```bash
git diff --cached            # 1. staged — changes indexed but not yet committed
git diff                     # 2. unstaged — changes in working tree vs index
git diff develop...HEAD      # 3. branch — all commits on this branch vs develop
git diff HEAD~1 HEAD         # 4. last commit — what the most recent commit changed
```

For option 5, use `Read` / `Glob` directly on the named path(s).

Use `Grep` and `Read` to look up surrounding context as needed — but only
flag issues that exist within the diff, not in code the diff didn't touch.

---

## Step 3 — Run the Checklist

Each section lists the condition that triggers it. If that condition isn't true
of this diff, skip the section entirely without mentioning it.

### 3.1 Architecture & Layer Boundaries
**Trigger:** diff touches any class in `remote/api/`, `remote/source/`, repositories, ViewModels, or UseCases.

- [ ] No direct Supabase/network calls from a ViewModel or UseCase — those belong in Services/DataSources
- [ ] No domain model construction inside a Service — use mappers in the DataSource layer
- [ ] No DTO types leaking into `:feature:*` or `:core:presentation` modules
- [ ] No platform-specific APIs (`android.*`, `UIKit`, etc.) used in `commonMain` code

### 3.2 Koin Registration
**Trigger:** diff adds a new class that is constructor-injected somewhere (Service, DataSource, Repository, ViewModel, UseCase).

- [ ] The new class is registered in the correct Koin module (see `koin-watcher` skill for ownership table)
- [ ] Registration uses `singleOf` / `factoryOf` unless manual wiring is needed
- [ ] Repositories are registered by interface, not by implementation
- [ ] If a new feature module was created, it's listed in `KoinHelper.kt`

### 3.3 Bark Logging
**Trigger:** diff adds or modifies any log statement, or adds a new `catch` block.

- [ ] No `println`, `Log.d`, `Log.e`, or any platform logger — only `Bark.*`
- [ ] Log level matches intent (see `bark-logging` skill for level table)
- [ ] Message is capitalized and follows `"[Action]: [Details] (ID: $id)"` format
- [ ] Exception object passed as second argument to `Bark.w` / `Bark.e`
- [ ] No passwords, tokens, or PII logged

### 3.4 KMP Hygiene
**Trigger:** diff touches `commonMain`, or adds/modifies `expect`/`actual` declarations.

- [ ] `expect`/`actual` used correctly for platform-specific implementations
- [ ] No `@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")` without justification
- [ ] Coroutines in `commonMain`: ViewModels launch in `viewModelScope` (main-safe by default); standalone/background scopes use `Dispatchers.Default` or `Dispatchers.IO`, never a hardcoded `Dispatchers.Main`

### 3.5 General Code Quality
**Trigger:** always — apply to everything in the diff.

- [ ] No silent `catch` blocks — exceptions are logged or rethrown
- [ ] No `!!` force-unwrap without a comment explaining why it's safe
- [ ] No magic numbers — use named constants
- [ ] No dead code (commented-out blocks, unused parameters, unreachable branches)
- [ ] Functions do one thing; flag anything over ~40 lines for discussion

### 3.6 Test Coverage
**Trigger:** diff adds new public functions, ViewModel logic, or mappers — and no test file covers them.

- [ ] New public functions have corresponding unit tests
- [ ] New ViewModel logic has ViewModel tests (follow patterns in `:feature:*`)
- [ ] New mappers have mapper tests
- [ ] Tests use Mokkery for mocking — not Mockito or manual stubs
- [ ] No `Thread.sleep` in tests — use `runTest` and `advanceUntilIdle`

### 3.7 Conventional Commits
**Trigger:** scope is "last commit" or "branch vs main" (i.e., commit messages are available).

- [ ] Each commit follows `type(scope): description` format
- [ ] Type is one of: `feat`, `fix`, `perf`, `refactor`, `test`, `chore`, `ci`, `docs`, `style`, `build`
- [ ] No period at the end of the subject line
- [ ] Breaking changes noted with `!` or a `BREAKING CHANGE:` footer

---

## Step 4 — Output the Review

```
## Code Review

**Scope:** <what was reviewed, e.g. "staged changes" or "branch vs main">

### Issues

#### Critical  ← must fix before merging
- `<file>:<line>` — <description>

#### Warnings  ← should fix, but not a blocker
- `<file>:<line>` — <description>

#### Suggestions  ← optional improvements
- `<file>:<line>` — <description>

### Passed Checks
- <list of triggered sections that had no findings>
```

If there are zero issues:
> "No issues found. All triggered checks passed."

Do NOT invent issues to seem thorough. Only report what you actually found in the diff.

---

## Constraints

- Do not run Gradle builds or tests
- Do not make edits unless the user explicitly asks for a fix after the review
- Stay within the diff — do not audit code the diff didn't touch