---
paths:
  - "core/**"
  - "feature/**"
  - "shared/**"
  - "iosApp/iosApp/Helpers/**"
---

# Architecture: data → domain → UI

## Dependency direction
- `feature:*` depends only on `core:*`, never on another feature. Features reach data only through `:core:data` repositories, never `:core:database` or `:core:network` directly. Using `:core:database` as a `commonTest` dependency for in-memory DBs in ViewModel tests is fine.
- `:core:model` and `:designsystem` are leaf modules. `:shared` aggregates every feature plus the core modules for the iOS framework and Koin setup.

## Layers
- **Repositories** (`core/data/.../repositories/`) do data access only: they coordinate remote data sources and the Room cache, map DTO → domain, and return `Result<T>`. No business rules.
  - Cache-first: serve Room data that's present, complete and within its `CacheTTL`, unless `forceRefresh = true` (pull-to-refresh). Otherwise fetch remote and write back. "Complete" matters: a `Club` cached incidentally from a Member response (no `members`) is incomplete and triggers a refresh.
  - On remote failure, return the cached value even if it's past its TTL (`recoverWithCache`), and surface the error only when nothing is cached. The fallback is silent to callers. The UI learns about it through `ConnectivityRepository.isOffline` → `ConnectivityCoordinator` ("Working offline" banner).
- **UseCases** (`feature/*/domain/`) hold all business logic: validation, role and permission rules, combining repositories. ViewModels call UseCases, never repositories. Known legacy violations: `AuthViewModel` and `JoinViewModel` call `AuthRepository` directly. Don't copy that pattern.
- **ViewModels** (`feature/*/presentation/`) orchestrate UseCases and expose screen state. No business logic.
- UseCases return domain models from `:core:model`, never presentation or UI models. Some existing UseCases still import `presentation` types (e.g. `GetClubDetailsUseCase`, `GetCurrentUserProfileUseCase`). That's a known violation with a fix ticketed. Don't add more.

## Errors
- Data layer: failures are classified into the closed `AppError` set via `Throwable.toAppError()` (`core:data`).
- Domain: throw `UserFacingException` only for deliberate rule violations with a user-ready message.
- UI: the only way a failure becomes text is `Throwable.toDisplayMessage()` (`core:presentation`). Log the raw throwable with Bark separately. Don't call `toAppError().toUserMessage()` directly from a ViewModel.

## ViewModel state
- One `StateFlow<{Screen}State>` per screen. User intents are `on<Event>()` functions.
- One-shot events (success notices, transient errors) are modeled as state flags cleared by an `onDismiss<X>()` intent (e.g. `onDismissSaveSuccess`), until the planned SideEffects layer lands.
- When a refresh fails, keep the content already on screen and report the failure via a snackbar, not an error page (see `ClubDetailsViewModel`, `MeViewModel`).

## iOS bridge
- Each shared ViewModel gets a `{Name}ViewModelHelper` in `feature/{name}/src/iosMain/.../presentation/`. It's a `KoinComponent` exposing `observeState(callback): Closeable` plus 1:1 action passthroughs.
- SwiftUI consumes it through `iosApp/iosApp/Helpers/{Name}ViewModelWrapper.swift`, which mirrors state into `@Published` properties and closes its `Closeable`s in `deinit`.
- A new ViewModel needs a Helper, a Wrapper, and a `{Name}ViewModelHelperTest` in `iosTest`.

## DI
- Register every injectable in its owning Koin module. The `koin-watcher` skill has the ownership table and scope rules. A new feature module must also be added to `initKoin` in `shared/.../di/KoinHelper.kt`.
