# :feature:auth

Sign-in, sign-up, password reset, and OAuth UI logic.

## Purpose

Shared presentation layer for the login flows: form state and validation, email sign-in/up,
Discord/Google OAuth, native Apple sign-in, and forgot-password.

## Key Components

- **`AuthViewModel`**: Sign in and Sign up, on `BaseViewModel<AuthScreenState>` (`authState`
  plus form fields and field errors). OAuth URLs open through `PlatformEffect.OpenUrl`.
- **`ForgotPasswordViewModel`**: the Forgot Password screen, on `BaseViewModel<ForgotPasswordState>`.
  Failures are danger snackbars keyed by `NoticeKey.AUTH_*` (`AuthErrorNotice.kt`).
- **`AuthState`**: `Unauthenticated`, `Loading`, `Authenticated(user)`,
  `EmailConfirmationPending(email)`. Failures aren't a state: they're danger snackbars.
- **`LoginNavigation`**: `SignIn`, `SignUp`, `ForgetPassword`.
- **`AuthViewModelHelper`** / **`ForgotPasswordViewModelHelper`** (iosMain): iOS bridges on
  `BaseViewModelHelper`, consumed by the matching `iosApp/iosApp/Helpers/*ViewModelWrapper.swift`.
- `authFeatureModule`: Koin wiring.

UseCases live in `domain/` and wrap `AuthRepository` (`core:auth`).

## Dependencies

- `:core:model`
- `:core:presentation`
- `:core:auth`

## Usage

```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)  // failure snackbars
    // render the form from state; state.authState drives Loading / Authenticated
    PrimaryButton(text = "Sign in", onClick = viewModel::validateAndSignIn)
}
```
