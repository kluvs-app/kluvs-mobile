---
paths:
  - "core/api/**"
  - "core/data/**"
  - "core/model/**"
  - ".github/workflows/regenerate-api-models.yml"
---

# Codegen pipeline and data layer

These rules add to the codegen rules in `CLAUDE.md` (never hand-edit generated output; generate and inspect DTOs before approving a spec PR) and to the `sync-api-contract` skill.

## Regen PR branch
- Never commit manual work to `chore/regenerate-api-models`. `regenerate-api-models.yml` force-pushes that branch and opens or updates the bot PR, so anything you add there is lost on the next spec release. Do follow-up work on your own branch off `develop`.
- The bot PR is opened with `GITHUB_TOKEN`, so it doesn't trigger `pull_request` workflows. To get unit tests on it, trigger them manually or close and reopen the PR.
- Keep `ref: develop` on the workflow's checkout step. `repository_dispatch` runs from the default branch (`main`). Without the pin, the generator runs against `main`'s stale Gradle config and regresses the generated output.

## Mapping
- Generated DTOs mirror the spec 1:1. Every transformation (id type changes, defaults for loosely-typed optional fields, dropping unused fields) belongs in the `core/data/.../remote/mappers/` layer, never in `core/api` and never in `core/model`.
- When the spec is looser than the real response (field optional or nullable in the spec, always present in practice), add an explicit fallback in the mapper. Don't touch the DTO. File a spec-tightening ticket if it matters.

## Integration tests
- Member-scoped endpoints (shelf, like, progress, discussion-note, attendance, join, reading log) return 403 to bot callers. Test them with `createUserAuthedSupabaseClient()` from `core/data/src/commonTest/.../remote/api/TestSupabaseClients.kt`, which signs in as a seeded user. Use `createBotSupabaseClient()` only for bot-allowed endpoints.
- If you build a new user-authed client by hand on the unit-test JVM, it needs what the helper already sets: `MemorySessionManager`, `MemoryCodeVerifierCache`, `enableLifecycleCallbacks = false`, and `Dispatchers.setMain(UnconfinedTestDispatcher())`.
- When integration failures show up after a backend change, check for stale seed references before you suspect the DTOs.
