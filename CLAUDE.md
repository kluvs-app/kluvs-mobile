# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow Preferences

Git operations are fine for commits, branching, staging, etc. — but use caution on dangerous operations (force push, hard reset, etc.) and only when there's a clear reason. Default to safe, reversible actions.

For Gradle commands: execute them as needed, but minimize token usage by suppressing verbose output. Use flags like `--quiet` or `--console=plain`, or redirect output to `/dev/null` when only error status matters. Be mindful of token cost when running expensive tasks.

## Planning Preferences

**Plan file location:** All implementation plans should be saved to `.claude/plans/` (gitignored).

**New KMP features** follow a strict 4-phase approach. Use the `new-kmp-feature` skill — it owns the file map, coding conventions, and phase gate rules.

When including build or test steps in plans or agent execution, use `--quiet` flag to suppress verbose output and keep token usage reasonable.

## Project Overview

**Kluvs** is a Kotlin Multiplatform mobile application for managing book clubs and reading sessions across Discord communities. The app uses Compose Multiplatform for UI and Supabase for backend services.

## Build Commands

### Android

```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Build release APK
./gradlew :composeApp:assembleRelease

# Install and run on connected device
./gradlew :composeApp:installDebug
```

### iOS

- Open `iosApp/iosApp.xcodeproj` in Xcode
- Select target device/simulator
- Press Run (⌘R)

## Testing

### Run Unit Tests Only

```bash
# Run all unit tests across all modules (excludes integration tests)
./gradlew testDebugUnitTest -PexcludeTests="**/*IntegrationTest.class"

# Run tests for a specific module
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:clubs:testDebugUnitTest
```

### Run Integration Tests

Integration tests require a local Supabase instance. They live in the `:shared` module:

```bash
# Run ALL tests including integration tests
./gradlew :shared:testDebugUnitTest
```

**Note**: Integration tests connect to a Supabase instance specified by `TEST_SUPABASE_URL` and `TEST_SUPABASE_KEY` environment variables.

#### Setting Up Local Supabase for Integration Tests

Integration tests rely on a **local Supabase instance** running from the `kluvs-backend` project. This ensures tests run against a consistent, isolated environment with seed data.

**Prerequisites:**
1. The `kluvs-backend` project must be cloned at `/Users/ivangarzab/Git/KLUVS/kluvs-backend`
2. Supabase CLI must be installed (`brew install supabase/tap/supabase`)

**Setup Workflow:**

```bash
# Navigate to the API project
cd /Users/ivangarzab/Git/KLUVS/kluvs-backend

# Start local Supabase (if not already running)
npx supabase start

# Check status to get local credentials
npx supabase status
# Note: API URL is typically http://127.0.0.1:54321
# Note: anon key is provided in the status output
```

**Environment Configuration:**

Add these to your `~/.gradle/gradle.properties`:

```properties
TEST_SUPABASE_URL=http://127.0.0.1:54321
TEST_SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0
```

**⚠️ Important: Applying Backend Changes**

When the backend API (`kluvs-backend`) has new database migrations:

```bash
# Navigate to API project
cd /Users/ivangarzab/Git/KLUVS/kluvs-backend

# Reset local database to apply new migrations and reseed
npx supabase db reset
```

This command:
- Drops and recreates the local database
- Applies ALL migrations in order (including new ones)
- Re-seeds test data from `supabase/seed.sql`
- Restarts Supabase containers

**Common Issues:**

1. **Tests failing with 404/NotFound errors**:
   - Likely cause: Backend migrations haven't been applied to local DB
   - Solution: Run `npx supabase db reset` in the API project

2. **Tests failing with schema errors**:
   - Likely cause: Stale database schema (missing migrations)
   - Solution: Run `npx supabase db reset` in the API project

3. **Connection errors**:
   - Verify local Supabase is running: `npx supabase status`
   - Verify `TEST_SUPABASE_URL` points to `http://127.0.0.1:54321`

**Test Data:**

Integration tests use seed data defined in `/Users/ivangarzab/Git/KLUVS/kluvs-backend/supabase/seed.sql`. See test file headers (e.g., `ClubServiceIntegrationTest.kt:28-50`) for documentation of available test data.

### Run Specific Test

```bash
# Run a single test class
./gradlew :core:data:testDebugUnitTest --tests "com.ivangarzab.kluvs.data.repositories.ClubRepositoryTest"

# Run a single test method
./gradlew :core:data:testDebugUnitTest --tests "com.ivangarzab.kluvs.data.repositories.ClubRepositoryTest.testGetClubById"
```

### Code Coverage

```bash
# Generate HTML coverage report
./gradlew shared:koverHtmlReport

# Generate XML coverage report (for CI)
./gradlew shared:koverXmlReport
```

Reports are generated in `shared/build/reports/kover/html/`

## Architecture

### Module Structure

The project follows a multi-module architecture. See `docs/MODULE_GRAPH.md` for the full dependency graph.

```
kluvs-mobile/
├── composeApp/           # Android UI (Compose Multiplatform)
├── iosApp/               # iOS application entry point
├── shared/               # iOS framework export + DI setup + AppCoordinator
├── core/
│   ├── model/            # Domain models (User, Club, Member, etc.)
│   ├── network/          # Supabase client, BuildKonfig, serializers
│   ├── auth/             # Authentication logic and repository
│   ├── data/             # Repositories and remote data sources
│   └── presentation/     # Shared UI utilities (FormatDateTimeUseCase)
└── feature/
    ├── auth/             # Auth UI (AuthViewModel)
    ├── clubs/            # Club details UI (ClubDetailsViewModel)
    └── member/           # Profile UI (MeViewModel)
```

Each module has its own `README.md` with detailed documentation.

### Data Layer Architecture

The data layer follows a clean architecture pattern (located in `:core:data`):

1. **Services** (`core/data/.../remote/api/`) - Direct Supabase API communication
   - `ServerService`, `ClubService`, `MemberService`, `SessionService`

2. **Remote Data Sources** (`core/data/.../remote/source/`) - Transform DTOs to domain models
   - Use mappers to convert between DTOs and domain models

3. **Repositories** (`core/data/.../repositories/`) - Abstract data access
   - Expose clean domain interfaces
   - Designed to support local caching in the future

### Dependency Injection with Koin

All dependency injection is managed through Koin with modular organization:

- **`platformDataModule`** - Platform-specific dependencies (Android/iOS SecureStorage)
- **`coreNetworkModule`** - Supabase client
- **`coreDataModule`** - Services, data sources, repositories
- **`coreAuthModule`** - Auth service and repository
- **`corePresentationModule`** - Shared presentation utilities
- **`authFeatureModule`** - AuthViewModel
- **`clubsFeatureModule`** - Club-related ViewModels and UseCases
- **`memberFeatureModule`** - Member-related ViewModels and UseCases

Koin is initialized in `shared/src/commonMain/kotlin/com/ivangarzab/kluvs/di/KoinHelper.kt`

### Configuration Management

Supabase credentials are managed via BuildKonfig in `:core:network`:

- Production credentials: `SUPABASE_URL`, `SUPABASE_KEY`
- Test credentials: `TEST_SUPABASE_URL`, `TEST_SUPABASE_KEY`

These must be set in `~/.gradle/gradle.properties` or as environment variables. See `core/network/build.gradle.kts` for the configuration logic.

## Navigation Architecture

The app uses a hybrid navigation approach with two layers:

- **App-Level Navigation (Shared):** `AppCoordinator` handles authentication state and determines which major section of the app to show (login vs main app). This logic is shared across Android and iOS.
- **Feature Navigation (Platform-Specific):** Platform NavHost/NavigationStack handles user-driven flows like browsing clubs, viewing details, editing profile, etc.

See `docs/NAVIGATION.md` for detailed navigation architecture documentation.

## Testing Strategy

### Test Organization

Tests are co-located with their implementations in each module:

- **Unit Tests** - Fast tests with mocked dependencies using Mokkery
  - `:core:model` - Model tests
  - `:core:auth` - Auth repository and mapper tests
  - `:core:data` - Repository, data source, and mapper tests
  - `:core:network` - Serializer tests
  - `:core:presentation` - Utility tests
  - `:feature:auth` - AuthViewModel tests
  - `:feature:clubs` - Club UseCase and ViewModel tests
  - `:feature:member` - Member UseCase and ViewModel tests

- **Integration Tests** - Tests against real Supabase instance
  - Located in `shared/src/commonTest/`
  - Suffixed with `*IntegrationTest.kt`
  - Require local Supabase instance running
  - Excluded from quick test runs via `excludeTests` property

### Logging

**Always use barK** (`Bark.*`) for all log statements — never `println` or `Log.*`. Follow the `bark-logging` skill for level selection, message formatting, and exception handling rules.

### Code Review

Use the `code-review` skill (`/code-review`) to perform a project-aware review of staged changes, a file, or a module. It checks architecture boundaries, Koin registration, Bark usage, KMP hygiene, test coverage, and conventional commit format.

## CI/CD

The project uses GitHub Actions:

- **Unit Tests** (`.github/workflows/unit-tests.yml`) - Fast feedback on PRs, excludes integration tests
- **Full Tests** (`.github/workflows/full-tests.yml`) - Runs on `main` branch with local Supabase instance
  - Checks out both `kluvs-mobile` and `kluvs-backend` repos
  - Starts local Supabase instance
  - Applies migrations and seed data
  - Runs complete test suite
  - Uploads coverage to Codecov

## Gradle Configuration Notes

### Exclude Tests Dynamically

The `shared/build.gradle.kts` includes a custom task configuration that allows excluding tests via property:

```bash
./gradlew testDebugUnitTest -PexcludeTests="**/*IntegrationTest.class"
```

### Convention Plugin

All library modules use a shared convention plugin `kluvs.kmp.library` defined in `buildSrc/`. This ensures consistent Kotlin Multiplatform configuration across modules.

### Kover Exclusions

Code coverage excludes:
- Generated code (`*.BuildConfig`, `*.BuildKonfig`)
- DTOs (`**.dtos`)
- Dependency injection modules (`**.di`)