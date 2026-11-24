# :feature:join

Join a club from an invite token or link.

## Purpose

Shared presentation and domain logic for the join flow: preview the club behind an invite token,
then join it. If the user isn't signed in, the state asks the UI to route to sign-in first.

## Key Components

- **`JoinViewModel`**: `state: StateFlow<JoinState>`; `onTokenChanged()`, `previewInvite()`,
  `onJoinClicked()`, and `onConsume*()` intents for one-shot flags (`needsSignIn`,
  `joinedClubId`, `joinError`).
- **UseCases** (`domain/`):
  - `PreviewInviteUseCase`: token → `ClubPreview`
  - `JoinClubUseCase`: token → joined club ID
- **`JoinViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/JoinViewModelWrapper.swift`.
- `joinFeatureModule`: Koin wiring.

`JoinViewModel` reads auth state from `AuthRepository` directly (an exception to the
ViewModel → UseCase rule).

Invite-link parsing (`InviteLink`) and resuming a join after sign-in (`PendingJoinCoordinator`)
live in `:shared`, not here.

## Dependencies

- `:core:model`
- `:core:data`
- `:core:auth`
- `:core:presentation`

## Usage

```kotlin
@Composable
fun JoinScreen(viewModel: JoinViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    // TextField(state.tokenInput, viewModel::onTokenChanged); preview via viewModel.previewInvite()
    state.preview?.let { PrimaryButton(text = "Join", onClick = viewModel::onJoinClicked) }
}
```
