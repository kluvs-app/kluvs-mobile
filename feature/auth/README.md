# :feature:auth

Sign-in, sign-up, password reset, and OAuth UI logic.

## Purpose

Shared presentation layer for the login flows: form state and validation, email sign-in/up,
Discord/Google OAuth, native Apple sign-in, and forgot-password.

## Key Components

- **`AuthViewModel`**: exposes `state: StateFlow<AuthState>`, `uiState: StateFlow<AuthUiState>`
  (form fields and field errors), and `forgotPasswordState: StateFlow<ForgotPasswordUiState>`.
- **`AuthState`**: `Unauthenticated`, `Loading`, `Authenticated(user)`, `Error(AuthError)`,
  `OAuthPending(url)`, `EmailConfirmationPending(email)`.
- **`LoginNavigation`**: `SignIn`, `SignUp`, `ForgetPassword`.
- **`AuthViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/AuthViewModelWrapper.swift`.
- `authFeatureModule`: Koin wiring.

This module has no `domain/` UseCases yet: `AuthViewModel` calls `AuthRepository` directly, an
exception to the ViewModel → UseCase rule. New auth logic should go into UseCases.

## Dependencies

- `:core:model`
- `:core:presentation`
- `:core:auth`

## Usage

```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    // render form from uiState; react to state (OAuthPending → open URL, Authenticated → done)
    PrimaryButton(text = "Sign in", onClick = viewModel::validateAndSignIn)
}
```
