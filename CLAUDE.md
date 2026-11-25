# CLAUDE.md

**Kluvs** is a Kotlin Multiplatform app for book clubs and reading sessions: Android UI in Compose, iOS UI in native SwiftUI, with business logic shared through `:shared`. The backend is Supabase (`kluvs-backend` Edge Functions). Clubs can optionally link to a Discord server via `kluvs-bot`; web-only clubs work standalone. There are six user-facing areas: **Auth**, **Clubs**, **Books**, **Member/Me**, **Settings**, and **Join** (invite links). Both apps are live in the Google Play Store and the App Store with real users.

## ⚠️ Absolute rules
- **Never hand-edit generated codegen output** (`core/api/.../api/models/`, `core/api/openapi.json`). Fix the spec in `kluvs-api` instead. Details and the one exception: `.claude/rules/codegen-and-data.md`.
- **Before saying a spec PR (in `kluvs-api`) is correct, generate the real DTOs.** Reading the JSON isn't enough. Recipe: the `sync-api-contract` skill, section A.
- **Don't run Gradle or `xcodebuild` unless asked.** The user is usually building in the IDE. See `.claude/rules/build-and-run.md`.
- **Architecture is data → domain → UI**: repositories handle data access, UseCases hold business logic, ViewModels hold state. See `.claude/rules/architecture.md`.
- **Log with barK only** (`Bark.*`), never `println`/`Log.*`. See the `bark-logging` skill.
- Never run `supabase db reset` (or any Supabase CLI command) against `kluvs-backend`; the user does that.
- Implementation plans go in `.claude/plans/` (gitignored).

## Commands
Run Gradle only when builds are wanted, and always with `--quiet --console=plain`.

```bash
./gradlew :composeApp:installDebug                       # build + install Android debug
./gradlew :composeApp:assembleRelease                    # release APK
./gradlew testDebugUnitTest -PexcludeTests="**/*IntegrationTest.class"   # all unit tests
./gradlew :feature:clubs:testDebugUnitTest               # one module
./gradlew :core:data:testDebugUnitTest --tests "com.ivangarzab.kluvs.data.repositories.ClubRepositoryTest"
./gradlew :core:data:testDebugUnitTest --tests "*IntegrationTest"   # needs local Supabase (core/data/README.md)
./gradlew :<module>:compileTestKotlinIosSimulatorArm64   # iOS test compile (catches Native-only name errors)
./gradlew koverHtmlReport                                # coverage → build/reports/kover/
```

iOS: open `iosApp/Kluvs.xcodeproj` in Xcode.

## Module map
Source of truth for the module list: `settings.gradle.kts`. Each module has a `README.md`.

```
composeApp/     Android UI (Compose): NavHost, MainActivity, MainScreen (bottom nav: Me/Clubs/Books)
iosApp/         iOS app (SwiftUI): Views/{Auth,Clubs,Books,Me,Join}, Helpers/ (ViewModel wrappers), DesignSystem/ Swift package
shared/         iOS framework export, Koin setup (di/KoinHelper.kt), AppCoordinator, InviteLink
designsystem/   Android Compose DS: KluvsTheme (typography + colors) and the component library
core/
  model/        domain models (leaf)
  api/          100% generated OpenAPI DTOs (never hand-edit)
  network/      Supabase client, BuildKonfig, connectivity
  database/     Room DB + DAOs (backs cache-first repositories)
  auth/         auth logic and repository
  data/         services, remote data sources, mappers, repositories
  presentation/ shared UI utilities, error → display message
feature/
  auth/ clubs/ books/ member/ settings/ join/   ViewModels + UseCases per area
```

## Where the details live
Rules in `.claude/rules/` load automatically when you touch matching files.

| Topic | Rule file |
|---|---|
| Layers, errors, ViewModel state, iOS bridge, DI | `architecture.md` |
| Codegen, regen PRs, mappers, integration-test auth | `codegen-and-data.md` |
| Building, running, Xcode Cloud | `build-and-run.md` |
| Components, design system, previews, sheets | `ui-components.md` |
| AppCoordinator, feature navigation, invite links | `navigation.md` |
| Auth and session handling | `auth.md` |
| Test layout, Native test-name trap | `testing.md` |
| Convention plugin, Kover, BuildKonfig | `build-logic.md` |
| Live-product constraints, platform ordering in copy | `product.md` |

Repo skills (`.claude/skills/`). Several load automatically based on context:

| Skill | When |
|---|---|
| `new-kmp-feature` | A brand-new top-level `feature/{name}` module only (not changes to an existing screen). 4-phase approach. Invoke explicitly. |
| `code-review` | `/code-review`: project-aware review of staged changes, a file or a module. |
| `bark-logging` | Any log statement or exception handling. |
| `koin-watcher` | Adding injectable classes or wiring Koin modules. |
| `migrate-room-db` | Room schema changes (`MigrationXToY.kt`). |
| `sync-api-contract` | API spec changes, DTO regeneration, verifying a candidate spec PR. |

## CI/CD
- GitHub Actions (`.github/workflows/`) run Android CI, releases, and API-model regeneration. `unit-tests.yml` runs on PRs. `full-tests.yml` runs on pushes to `main`/`develop` against a local Supabase and uploads coverage to Codecov.
- iOS CI and releases run on **Xcode Cloud** (`iosApp/ci_scripts/`).
- The base branch for PRs is `develop`.
