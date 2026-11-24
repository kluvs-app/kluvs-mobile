# :feature:settings

Settings screen: edit profile, avatar, password, sign-out, and account deletion.

## Purpose

Shared presentation and domain logic for account settings: editing name and handle, uploading an
avatar, changing the password, signing out, and deleting the account.

## Key Components

- **`SettingsViewModel`**: `state: StateFlow<SettingsState>`; `loadProfile(userId)`,
  `onSaveProfile()`, `uploadAvatar(imageData)`, change-password sheet intents, and
  delete-account dialog intents (`onDeleteAccountConfirmed()`).
- **UseCases** (`domain/`):
  - `GetEditableProfileUseCase`: loads the editable profile (`EditableProfile`)
  - `UpdateUserProfileUseCase`: validates and saves name/handle
  - `UpdateAvatarUseCase`: uploads the avatar and updates the member
  - `ChangePasswordUseCase`, `DeleteAccountUseCase`
- **`SettingsViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/SettingsViewModelWrapper.swift`.
- `settingsFeatureModule`: Koin wiring.

Sign-out reuses `SignOutUseCase` from `:core:auth`.

## Dependencies

- `:core:model`
- `:core:data`
- `:core:auth`
- `:core:presentation`
- `:core:database` (`commonTest` only)

## Usage

```kotlin
@Composable
fun SettingsScreen(userId: String, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(userId) { viewModel.loadProfile(userId) }
    // fields call viewModel.onNameChanged / onHandleChanged; save via onSaveProfile()
}
```
