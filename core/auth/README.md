# :core:auth

Authentication data layer: sign-in/out, session restore, and auth state.

## Purpose

Wraps Supabase Auth behind `AuthRepository` and exposes the current user and auth state to the
rest of the app. Repository methods return `Result<T>`. Supabase owns session persistence and
token rotation.

## Key Components

- **`AuthRepository`**: `currentUser: StateFlow<User?>`, `isAuthenticated: StateFlow<Boolean>`,
  `initialize()` (session restore), email sign-in/up, Discord/Google OAuth plus
  `handleOAuthCallback()`, native Apple sign-in, password reset/change, `signOut()`.
- **`SignOutUseCase`**: signs out, then clears every table of the local `KluvsDatabase` cache.
- **`AuthService`**: thin wrapper over the Supabase Auth client, used by the repository.
- **`SecureStorage`** (expect/actual): Android `EncryptedSharedPreferences`, iOS Keychain.
- **`AuthError`**: locale-agnostic auth error codes, mapped by `AuthExceptionMapper`.
- `coreAuthModule`: Koin wiring.

## Dependencies

- `:core:model` (api)
- `:core:network`
- `:core:database`

## Usage

Consume it from UseCases, not from ViewModels directly:

```kotlin
// feature/settings/.../domain/ChangePasswordUseCase.kt (simplified)
class ChangePasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(newPassword: String): Result<Unit> =
        authRepository.changePassword(newPassword)
}
```
