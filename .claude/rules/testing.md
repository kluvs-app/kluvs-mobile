---
paths:
  - "**/commonTest/**"
  - "**/androidUnitTest/**"
  - "**/iosTest/**"
  - "**/*Test.kt"
---

# Testing

## Layout
- Tests sit next to the code in each module's `commonTest` (plus `iosTest` for `{Name}ViewModelHelperTest`). Mock with **Mokkery**.
- Integration tests are `*IntegrationTest.kt` in `core/data`. They hit a local Supabase from `kluvs-backend` (setup: `core/data/README.md`) and are excluded from quick runs with `-PexcludeTests="**/*IntegrationTest.class"` (root `build.gradle.kts`).
- Integration seed data comes from `kluvs-backend/supabase/seed.sql`. The available test data is documented in each test file's header (e.g. `ClubServiceIntegrationTest.kt`).

## ⚠️ Test names: no commas
- Never use commas in backticked test names (e.g. `` `deleteClub removes the club, its links` ``). Kotlin/Native also rejects `.` `;` `:` `/` `<` `>` `[` `]` `\`.
  Why: the JVM accepts them, so Android runs pass, and only the iOS compile in CI fails.
- When you change `commonTest`, also compile for iOS before pushing (only when builds are wanted; see `build-and-run.md`): `./gradlew :<module>:compileTestKotlinIosSimulatorArm64 --quiet`.

## Conventions
- A ViewModel with an iOS Helper also gets a `{Name}ViewModelHelperTest` in `iosTest`.
- Use `createUserAuthedSupabaseClient()` for member-scoped endpoints in integration tests (see `codegen-and-data.md`).
