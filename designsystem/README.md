# :designsystem

Android Compose design system: the Kluvs theme and component library.

## Purpose

Android-only leaf module (no iOS target, no `:core`/`:feature` dependencies) used by
`:composeApp`. iOS has its own SwiftUI equivalent in the `iosApp/DesignSystem` Swift package.
Tokens follow the `design-system` repo; don't hardcode colors or spacing in screens.

## Key Components

- **`KluvsTheme`** (`theme/`): the composable wraps app content; the `KluvsTheme` object exposes
  `KluvsTheme.colors` (`KluvsColors`) and `KluvsTheme.typography` (`KluvsTypography`). It still
  feeds a `MaterialTheme` shim for stock M3 widgets, but new code reads `KluvsTheme.*`.
- **Fonts**: `ebGaramond`, `ibmPlexSans`.
- **Components** (`components/`), grouped by kind: `buttons/` (`PrimaryButton`, `SecondaryButton`,
  ...), `fields/` (`InputField`, `SearchField`, `PasswordField`), `modals/` (`BottomSheet`,
  `ConfirmationDialog`), `appbars/`, `avatars/`, `pills/`, `controls/`, `menus/`, `pickers/`,
  `progress/`, `loading/`, `icons/`, plus screen-level pieces like `EmptyState`, `ErrorScreen`
  and `OfflineBanner`.

## Dependencies

- No `project(...)` dependencies.
- Compose (runtime, foundation, material3, ui), Coil

## Usage

```kotlin
KluvsTheme {
    PrimaryButton(text = "Join", onClick = onJoin)
    Text("Clubs", style = KluvsTheme.typography.title.medium, color = KluvsTheme.colors.content)
}
```
