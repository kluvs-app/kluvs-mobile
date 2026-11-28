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
- Every screen ViewModel extends `BaseViewModel<{Screen}State>` (`core/presentation/.../presentation/viewmodel/`), which owns the screen's one `state: StateFlow<S>`. Subclasses read it with `currentState` and change it with `updateState { it.copy(...) }`, never their own `MutableStateFlow`. User intents are `on<Event>()` functions.
  - `AppCoordinator` isn't a screen, so it still owns its flows and its `EffectChannel`.
  - `S` is bound to `Any` so Kotlin/Native exports it to Swift as a typed generic (checked in the KT-363 spike). Keep the base's generics at class level: generic *functions* lose their types in Swift.
- **Side effects** are one-shot things a ViewModel wants to happen. There are three categories, named after who handles them. To pick one: could it run with no screen on top? It's a platform effect. Does it need this screen and disappear once it happens? It's a screen effect. Does it stay on screen? It's state.

  | | Platform effect | Screen effect | State |
  |---|---|---|---|
  | What | Something only the phone can do | A one-shot event the visible screen reacts to | Anything that stays visible |
  | Examples | Open a link or the store listing, copy, share | Show a snackbar | Sheets, dialogs, banners, loading |
  | Handled by | One Kotlin handler per platform | One shared listener per platform, in the screen scaffold | The screen, from `StateFlow` |

- **Platform effects** are cases of the sealed `PlatformEffect` class in `core/presentation/.../presentation/effect/`. A ViewModel calls the base's `protected send(PlatformEffect)`. `BaseViewModel` gets the `PlatformEffectHandler` from Koin (`by inject()`), so no ViewModel takes it as a constructor parameter or wires a channel; a test of a ViewModel that sends one starts Koin with a mock handler in `@BeforeTest` (see `ClubDetailsViewModelTest`). Each platform runs every case in its own Kotlin handler (`AndroidPlatformEffectHandler`, `IosPlatformEffectHandler`), so screens on either platform never open URLs, the clipboard or the share sheet themselves.
  - Add a new kind as a case on `PlatformEffect`, never per screen, and handle it in both platform handlers.
  - Name a case after what the phone does, never why the screen wants it: `ShareText(text)`, not `ShareClubInvite`; `OpenUrl(url)`, not `OpenPrivacyPolicy`. The screen-specific part (which text, which link, when) stays in the ViewModel. The list should grow with the phone's capabilities, not with the number of screens.
- **Screen effects** are cases of one shared sealed `ScreenEffect` class next to `PlatformEffect`, never per-screen types. A ViewModel calls the base's `protected send(ScreenEffect)`. They're delivered only while the screen is listening: one sent with no listener attached (a tab switched away from mid-request) is dropped, never shown later out of context. Platform effects are buffered instead, since they don't need a screen. One shared listener per platform shows them: each screen attaches it once, with no per-screen handling code. Same naming rule as platform effects: name a case after what the screen does (`ShowSnackbar`), not why.
  - Snackbars follow `design-system/docs/states.md` § Snackbar: the tone (success or danger) is the only style choice; duration and dismissal come from the design system, not from parameters. There are no toasts.
  - A snackbar's message is either text from Kotlin (`toDisplayMessage()` errors) or a shared key that each listener maps to its own string resource (success notices, and auth failures: `AuthError` has localized platform strings, so `toAuthErrorSnackbar()` in `feature/auth` maps it to a `NoticeKey.AUTH_*` key).
  - Screens that haven't migrated yet still use state flags cleared by an `onDismiss<X>()` intent. Don't add new ones.
- **Sheets and dialogs are ViewModel state**, never `remember` / `@State` flags, so the same tap opens the same sheet on both platforms and it survives rotation.
  - Each screen's state has `activeSheet: {Screen}Sheet?` and `activeDialog: {Screen}Dialog?`. Both are sealed classes with one case per sheet or dialog; a case carries the ID it's about (`EditDiscussion(discussionId)`). See `ClubDetailsState`.
  - Intents are always `onOpenSheet(sheet)`, `onDismissSheet()`, `onOpenDialog(dialog)` and `onDismissDialog()`, with these exact names (KT-363 moves them into the base ViewModel). Every way of closing calls the dismiss intent: Cancel, the close button, swipe down, back, a tap outside.
  - A dialog can sit on top of a sheet (delete note over the Notes sheet). Draw dialogs after sheets so they land on top.
  - A form sheet closes from the ViewModel when its save succeeds, via a private `closeSheet(ifShowing = …)` that leaves alone a sheet the user swapped to mid-save. A failed save leaves the sheet open. Because the sheet stays open while saving, ignore a repeat save while one is running.
  - Confirming a dialog closes it right away: the action intent (`onDeleteClub()`) clears `activeDialog` first.
  - What the user is typing inside a sheet can stay in local UI state.
- Sheets are never navigation destinations: a sheet is a layer over its screen and uses that screen's ViewModel. Only screens can be deep linked into. If a sheet ever needs a deep link, its own ViewModel or its own data, promote it to a full screen destination instead (see `JoinBottomSheet` vs. `JoinScreen`).
- When a refresh fails, keep the content already on screen and report the failure via a snackbar, not an error page (see `ClubDetailsViewModel`, `MeViewModel`).

## iOS bridge
- Each shared ViewModel gets a `{Name}ViewModelHelper` in `feature/{name}/src/iosMain/.../presentation/`. It extends `BaseViewModelHelper<{Screen}State>` (`core/presentation/src/iosMain/`), which provides `observeState(callback): Closeable`, and adds only 1:1 action passthroughs. Swift sees the concrete state type through the generic base.
- The Helper owns its ViewModel's lifetime: declare it `override val viewModel: XViewModel by viewModel()`, never `by inject()`. That puts the ViewModel in the Helper's `ViewModelStore`, and `close()` clears it (cancels `viewModelScope`, runs `onCleared()`), as popping a `NavBackStackEntry` does on Android.
- SwiftUI consumes it through `iosApp/iosApp/Helpers/{Name}ViewModelWrapper.swift`, which mirrors state into `@Published` properties. Its `deinit` closes its `Closeable`s, then calls `helper.close()`.
- Screen effects: the wrapper exposes `screenEffectSource` (its Helper, as the non-generic `ScreenEffectSource`), and the view attaches `.screenEffects(viewModel.screenEffectSource)` once (`iosApp/iosApp/Components/ScreenEffectsModifier.swift`). That's the iOS counterpart of Android's `ScreenEffectHost`. It listens only while the view is on screen.
- A new ViewModel needs a Helper, a Wrapper, and a `{Name}ViewModelHelperTest` in `iosTest`.

## DI
- Register every injectable in its owning Koin module. The `koin-watcher` skill has the ownership table and scope rules. A new feature module must also be added to `initKoin` in `shared/.../di/KoinHelper.kt`.
