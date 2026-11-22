# :feature:clubs

Clubs screen: club details, active reading session, discussions, members, and admin actions.

## Purpose

Shared presentation and domain logic for the Clubs tab: switching between the user's clubs,
viewing the active session and discussion timeline, managing members and roles, and the
owner/admin operations (create/edit/delete club, sessions, discussions, invite links).

## Key Components

- **`ClubDetailsViewModel`**: single `state: StateFlow<ClubDetailsState>` for the whole screen;
  loads club details, active session, members and own progress, and exposes `on<Event>()`
  intents for every action.
- **UseCases** (`domain/`), e.g.:
  - `GetClubDetailsUseCase`, `GetActiveSessionUseCase`, `GetMemberClubsUseCase`
  - `GetClubMembersUseCase`: members sorted by role (owners, then admins, then members)
  - `CreateClubUseCase`, `CreateSessionUseCase`, `FinishSessionUseCase`, `UpdateJoinPolicyUseCase`
  - `SetAttendanceUseCase`, `CreateDiscussionNoteUseCase`, `UpdateMemberRoleUseCase`
- **`BaseAdminUseCase`**: role-gated base class; fails with `UnauthorizedException` when the
  caller's `Role` isn't allowed.
- **UI models** (`ClubModels.kt`): `ClubListItem`, `ClubDetails`, `ActiveSessionDetails`,
  `MemberListItemInfo`, `DiscussionTimelineItemInfo`.
- **`ClubDetailsViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/ClubDetailsViewModelWrapper.swift`.
- `clubsFeatureModule`: Koin wiring.

## Dependencies

- `:core:model`
- `:core:data`
- `:core:auth`
- `:core:presentation`

## Usage

```kotlin
@Composable
fun ClubsScreen(userId: String, viewModel: ClubDetailsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(userId) { viewModel.loadUserClubs(userId) }
    // render state.currentClubDetails, state.activeSession, state.members
}
```
