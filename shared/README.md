# :shared

App integration module: Koin setup, app-level coordinators, and the iOS framework.

## Purpose

1. **iOS framework**: builds `Shared.framework` (static), exporting the modules the SwiftUI app
   needs.
2. **App coordination**: cross-platform coordinators that sit above individual features
   (auth-driven navigation, invite links, connectivity).

## Key Components

- **`initKoin()`** (`di/KoinHelper.kt`): starts Koin with the app module, the core modules
  (network, data, presentation, auth) and every feature module. A new feature module must be added
  here.
- **`AppCoordinator`**: restores the session and maps auth state to `NavigationState`
  (`Initializing`, `Unauthenticated`, `Authenticated(userId)`).
- **`PendingJoinCoordinator`**: holds an invite token from a link and resumes the join after
  sign-in (`AutoJoinResult`).
- **`ConnectivityCoordinator`**: forwards offline state to the shell for the "Working offline" banner.
- **`InviteLink`**: builds and parses invite URLs.
- **`initializeSentry()`**, **`SentryTrainer`**: crash reporting and the barK → Sentry bridge.
- **`*CoordinatorHelper`** (iosMain): iOS bridges for the coordinators, consumed by
  `iosApp/iosApp/Helpers/*CoordinatorWrapper.swift`.

## iOS framework exports

`:core:model`, `:core:auth`, `:core:data`, `:core:presentation`, and all six features
(`:feature:auth`, `:feature:clubs`, `:feature:member`, `:feature:settings`, `:feature:books`,
`:feature:join`), plus barK.

## Dependencies

- `:core:model`, `:core:auth`, `:core:data`, `:core:presentation` (api)
- `:core:network`
- `:feature:auth`, `:feature:clubs`, `:feature:member`, `:feature:settings`, `:feature:books`,
  `:feature:join` (api)

## Usage

```kotlin
// Android: composeApp/.../App.kt
initKoin { androidContext(this@App) }
```

```swift
// iOS: iosApp/iosApp/iOSApp.swift
KoinHelperKt.doInitKoin()
```
