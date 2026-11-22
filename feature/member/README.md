# :feature:member

"Me" tab: the signed-in member's profile, stats, shelf, and reading log.

## Purpose

Shared presentation and domain logic for the user's own profile screen: profile header,
statistics, books currently being read across clubs (with progress), the next upcoming
discussion, the reading log, and sign-out.

## Key Components

- **`MeViewModel`**: `state: StateFlow<MeState>`; `loadUserData(userId)`, `refresh()`,
  `onSaveProgress(...)`, reading-log and sign-out dialog intents.
- **UseCases** (`domain/`):
  - `GetCurrentUserProfileUseCase`: profile with formatted join date and avatar URL
  - `GetUserStatisticsUseCase`: clubs count and books read
  - `GetOnYourShelfUseCase`: active-session books across clubs plus the next discussion ("Up next")
  - `GetReadingLogUseCase`: the member's reading history
- **UI models** (`MemberModels.kt`): `UserProfile`, `UserStatistics`, `ShelfItem`, `UpNextItem`.
- **`MeViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/MeViewModelWrapper.swift`.
- `memberFeatureModule`: Koin wiring.

Progress saving and sign-out reuse `SaveProgressUseCase` (`:core:presentation`) and
`SignOutUseCase` (`:core:auth`).

## Dependencies

- `:core:model`
- `:core:presentation`
- `:core:auth`
- `:core:data`
- `:core:database` (`commonTest` only)

## Usage

```kotlin
@Composable
fun MeScreen(userId: String, viewModel: MeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(userId) { viewModel.loadUserData(userId) }
    // render state.profile, state.statistics, state.shelf, state.upNext
}
```
