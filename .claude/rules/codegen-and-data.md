---
paths:
  - "core/api/**"
  - "core/data/**"
  - "core/model/**"
  - ".github/workflows/regenerate-api-models.yml"
---

# Codegen pipeline and data layer

## ⚠️ Never hand-edit generated output
- `core/api/src/commonMain/kotlin/com/ivangarzab/kluvs/api/models/` and `core/api/openapi.json` are 100% generated from `kluvs-api`'s spec. Don't edit them, and don't add hand-written files to that directory: no missing fields, no nullability tweaks, however small or "obviously correct".
  Why: a past session hand-patched `BookDto.kt` and added a hand-written `SessionParticipantDto.kt`, flagging the risk only in a code comment. A regen two PRs later silently deleted both and broke shipped features. A one-off approved in one conversation is **never** a standing license.
- If a generated DTO is wrong or missing a field, fix the **spec in `kluvs-api`** (`openapi.json`, written by hand there) with a spec PR, and track it with a ticket (`/task-create`).
- If a hand-edit truly seems necessary (e.g. to unblock urgent work while a spec fix is in flight), **ask explicitly and get double-confirmed permission in this conversation**. Never infer it from a past session, and never write it up as a reusable pattern.
- **The one accepted exception** is temporary interim types for parallel-track work against a frozen contract. They live entirely **outside** `core/api` (e.g. `core/data/.../remote/api/dto/`), are never committed, and get swapped for the generated types before any PR opens. Reference: `BookEnrichmentDto.kt` (KT-243/KT-248).
- Verifying a candidate spec PR in `kluvs-api`: follow section A of the `sync-api-contract` skill (generate the real DTOs; don't just read the JSON).

## Try an unreleased spec
To generate the DTOs from a kluvs-api branch or commit before it merges, or before its regen PR lands:

1. In `core/api/build.gradle.kts`, set `val apiSpecRef = "<branch|sha>"`. The ref must be pushed to GitHub.
2. Run `:core:api:tryApiSpec` (Gradle tool window: `core:api` → `api`; sync once if the task isn't listed).
3. To reset, set `apiSpecRef` back to `""` and run `tryApiSpec` again. This regenerates from the saved `openapi.json` and removes any models the other spec added.

**Never commit the result:** neither the changed models nor a non-empty `apiSpecRef`. The regen PR brings the real update. `openApiGenerate` and the regen workflow always use the saved spec and ignore `apiSpecRef`.

## Regen PR branch
- Never commit manual work to `chore/regenerate-api-models`. `regenerate-api-models.yml` force-pushes that branch and opens or updates the bot PR, so anything you add is lost on the next spec release.
- When the regen PR needs app-code fixes, **stack** a fix PR on it:
  - Cut the fix branch from `origin/chore/regenerate-api-models` and open it with `--base chore/regenerate-api-models`, so its diff shows only the fixes.
  - Merge order: squash-merge the regen PR, retarget the fix PR to `develop`, then squash-merge it. The spec update and its fixes land as two consecutive commits.
  - Don't cut the fix branch from `develop` and merge the regen branch into it. That hides the regen PR inside the fix PR.
- Follow-up work that doesn't depend on the regen PR goes on your own branch off `develop`.
- The bot PR is opened by the `kluvs-spec-bot` GitHub App, so `pull_request` workflows (unit tests) run on it automatically.
- Keep `ref: develop` on the workflow's checkout step. `repository_dispatch` runs from the default branch (`main`). Without the pin, the generator runs against `main`'s stale Gradle config and regresses the output.

## Mapping
- Generated DTOs mirror the spec 1:1. Every transformation (id type changes, defaults for loosely typed optional fields, dropping unused fields) belongs in `core/data/.../remote/mappers/` (one `{Entity}Mappers.kt` per entity, `fun XDto.toDomain()`). Never put one in `core/api` or `core/model`.
- When the spec is looser than the real response (optional or nullable in the spec, always present in practice), add an explicit fallback in the mapper and leave the DTO alone. File a spec-tightening ticket if it matters.
- Domain models in `core/model` have no serialization annotations and use `kotlinx.datetime` for dates.

## Integration tests
- Member-scoped endpoints (shelf, like, progress, discussion-note, attendance, join, reading log) return 403 to bot callers. Test them with `createUserAuthedSupabaseClient()` from `core/data/src/commonTest/.../remote/api/TestSupabaseClients.kt`, which signs in as a seeded user. Use `createBotSupabaseClient()` only for bot-allowed endpoints.
- A user-authed client built by hand on the unit-test JVM needs what the helper sets: `MemorySessionManager`, `MemoryCodeVerifierCache`, `enableLifecycleCallbacks = false`, `Dispatchers.setMain(Dispatchers.Unconfined)` (never a test dispatcher, which would leak its virtual clock into later tests).
- When integration tests fail after a backend change, suspect stale seed references or unapplied migrations first. Only then suspect the DTOs. The user runs `npx supabase db reset` in `kluvs-backend`; never run it yourself.
