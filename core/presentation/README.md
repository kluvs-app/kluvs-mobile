# :core:presentation

Presentation utilities shared by every feature module.

## Purpose

Holds the one place errors become user-facing text, shared formatting helpers, common screen
state types, and a few UseCases used by more than one feature (reading progress).

## Key Components

- **`Throwable.toDisplayMessage()`**: the only way a failure becomes UI text.
  `UserFacingException` messages are shown as written; anything else is classified with
  `toAppError()` (`:core:data`) and turned into friendly copy by `AppError.toUserMessage()`.
- **`FormatDateTimeUseCase`** + **`DateTimeFormat`**: `FULL` ("February 12, 2025 at 7:00 PM"),
  `DATE_ONLY`, `TIME_ONLY`, `YEAR_ONLY`. `formatBookYear()` formats publication years.
- **`ScreenState`**: `Loading`, `Error(message)`, `Empty`, `Content`.
- **`GetSessionProgressUseCase`**, **`SaveProgressUseCase`**, **`OwnProgressInfo`**: the
  member's reading progress for a session (used by `:feature:clubs` and `:feature:member`).
- **`Closeable`** (iosMain): returned by every `{Name}ViewModelHelper.observeState()` so Swift can
  cancel observation.
- `corePresentationModule`: Koin wiring.

## Dependencies

- `:core:model` (api)
- `:core:data`
- `androidx.lifecycle.viewmodel` (api)

## Usage

```kotlin
getClubDetails(clubId)
    .onSuccess { details -> _state.update { it.copy(currentClubDetails = details) } }
    .onFailure { error ->
        Bark.e("Failed to load club (ID: $clubId)", error)
        _state.update { it.copy(error = error.toDisplayMessage()) }
    }
```
