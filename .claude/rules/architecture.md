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
- **UseCases** (`feature/*/domain/`) hold all business logic: validation, role and permission rules, combining repositories. ViewModels call UseCases, never repositories, even when the UseCase is a plain pass-through (see `feature/auth/.../auth/domain/`). Form validation counts as business logic, so it belongs in a UseCase (`ValidateCredentialsUseCase`), not the ViewModel.
  - A UseCase needed by more than one feature lives once in core, in a `domain` package next to the repository it wraps: `core/data/.../data/domain/` (e.g. `SearchBooksUseCase`, `RegisterBookUseCase`) or `core/auth/.../auth/domain/` (e.g. `SignOutUseCase`). Register it in that core module's Koin module, not in each feature. Never copy a UseCase between features. A feature that needs different logic wraps the shared one in its own UseCase.
- **ViewModels** (`feature/*/presentation/`) orchestrate UseCases and expose screen state. No business logic.
- UseCases return domain models from `:core:model`, never presentation or UI models. Some existing UseCases still import `presentation` types (e.g. `GetClubDetailsUseCase`, `GetCurrentUserProfileUseCase`). That's a known violation with a fix ticketed. Don't add more.

## Errors
- Data layer: failures are classified into the closed `AppError` set via `Throwable.toAppError()` (`core:data`).
- Domain: throw `UserFacingException` only for deliberate rule violations with a user-ready message.
- UI: the only way a failure becomes text is `Throwable.toDisplayMessage()` (`core:presentation`). Log the raw throwable with Bark separately. Don't call `toAppError().toUserMessage()` directly from a ViewModel.

## ViewModel state
- One `StateFlow<{Screen}State>` per screen. User intents are `on<Event>()` functions.
- **Side effects** are one-shot things a ViewModel wants to happen. There are three categories, named after who handles them. To pick one: could it run with no screen on top? It's a platform effect. Does it need this screen and disappear once it happens? It's a screen effect. Does it stay on screen? It's state.

  | | Platform effect | Screen effect (planned) | State |
  |---|---|---|---|
  | What | Something only the phone can do | A one-shot event the visible screen reacts to | Anything that stays visible |
  | Examples | Open a link or the store listing, copy, share | Show a toast or snackbar, close a sheet after saving | Sheets, dialogs, banners, loading |
  | Handled by | One Kotlin handler per platform | One shared listener per platform, in the screen scaffold | The screen, from `StateFlow` or local UI state |

- **Platform effects** are cases of the sealed `PlatformEffect` class in `core/presentation/.../presentation/effect/`. A ViewModel takes a `PlatformEffectHandler`, owns an `EffectChannel<PlatformEffect>` wired to it (`EffectChannel<PlatformEffect>().apply { handleWith(viewModelScope, platformEffectHandler::handle) }`), and calls `platformEffects.send(...)`. Each platform runs every case in its own Kotlin handler (`AndroidPlatformEffectHandler`, `IosPlatformEffectHandler`), so screens on either platform never open URLs, the clipboard or the share sheet themselves.
  - Add a new kind as a case on `PlatformEffect`, never per screen, and handle it in both platform handlers.
  - Name a case after what the phone does, never why the screen wants it: `ShareText(text)`, not `ShareClubInvite`; `OpenUrl(url)`, not `OpenPrivacyPolicy`. The screen-specific part (which text, which link, when) stays in the ViewModel. The list should grow with the phone's capabilities, not with the number of screens.
- **Screen effects** (`ScreenEffect`) aren't built yet; a planned base ViewModel adds them. They'll follow the same rules: one shared sealed class with cases named by action (`ShowMessage`, not `ShowClubSavedToast`). Until then, success notices and transient errors are state flags cleared by an `onDismiss<X>()` intent (e.g. `onDismissSaveSuccess`).
- A sheet's visibility is state, because it has to survive rotation and be dismissed. Only the moment around it (close the sheet because the save succeeded) is an effect.
- When a refresh fails, keep the content already on screen and report the failure via a snackbar, not an error page (see `ClubDetailsViewModel`, `MeViewModel`).

## iOS bridge
- Each shared ViewModel gets a `{Name}ViewModelHelper` in `feature/{name}/src/iosMain/.../presentation/`. It's a `KoinComponent` exposing `observeState(callback): Closeable` plus 1:1 action passthroughs.
- SwiftUI consumes it through `iosApp/iosApp/Helpers/{Name}ViewModelWrapper.swift`, which mirrors state into `@Published` properties and closes its `Closeable`s in `deinit`.
- A new ViewModel needs a Helper, a Wrapper, and a `{Name}ViewModelHelperTest` in `iosTest`.

## DI
- Register every injectable in its owning Koin module. The `koin-watcher` skill has the ownership table and scope rules. A new feature module must also be added to `initKoin` in `shared/.../di/KoinHelper.kt`.
