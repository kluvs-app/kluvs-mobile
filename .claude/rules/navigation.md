---
paths:
  - "shared/src/commonMain/kotlin/com/ivangarzab/kluvs/app/**"
  - "composeApp/src/androidMain/kotlin/com/ivangarzab/kluvs/ui/**"
  - "iosApp/iosApp/**"
  - "feature/*/src/**/presentation/**"
---

# Navigation

## Two layers
- **App-level navigation is shared.** `AppCoordinator` (`shared/.../app/AppCoordinator.kt`) observes auth state and emits `NavigationState` (`Initializing`, `Unauthenticated`, `Authenticated`, `UpdateRequired`). The platforms react: `MainActivity` on Android, `AppCoordinatorWrapper` on iOS. On major transitions they clear the backstack.
- **Feature navigation is per platform.** Android uses a `NavHost` with `NavDestinations`; iOS uses SwiftUI `NavigationStack`. Screens take explicit `onNavigateTo…` callbacks.

## Rules
- ViewModels never navigate. They change state. Sign-out, for example, updates `AuthRepository`, and `AppCoordinator` reacts.
- Leaving the app (a browser link, the store listing on the force-update screen) is not navigation: it's a `PlatformEffect` (see `architecture.md`). `AppCoordinator.onUpdateApp()` opens the store listing for both platforms.
- Only app-wide states (auth, force-update, and later onboarding) belong in `NavigationState`. Never add feature destinations such as club details to it.
- Duplicating feature navigation across the two platforms is accepted. It's 1–2 lines per screen, and it keeps native behavior (swipe-back, deep links, saved state).

## Invite links (`feature/join`)
- The full-screen `JoinScreen`/`JoinView` (`NavDestinations.JOIN`) is reachable **only** from an invite deep link: an App Link (Android) or Universal Link (iOS) on `app.kluvs.com` / `app.kluvs.xyz`. Typing a code by hand goes through `JoinBottomSheet` inside the authenticated Clubs tab.
- Invite URLs are built and parsed **only** by `InviteLink` in `:shared`. Never hardcode the web origin at a call site. `https://kluvs.com/join/{token}` is **not** a valid invite URL: `kluvs.com` is the marketing domain and has no such route.
