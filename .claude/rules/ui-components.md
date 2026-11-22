---
paths:
  - "designsystem/**"
  - "iosApp/**"
  - "composeApp/**"
  - "feature/*/src/**/presentation/**"
---

# UI components, design system, and sheets

## Before building or changing a component
- Read the matching `../design-system/docs/*.md` spec (and `colors_and_type.css` / `preview/`) **first**, before auditing mobile or web code. The DS spec is the reference. Existing hand-rolled code is not: when you replace an implementation, match the spec, not the buggy code you're replacing.
- Confirm scope, names, and API with the user before writing a new component. Build only what was agreed: no extra variants, no speculative components.

## Grouping and naming
- Group components by what they represent to the user, not by how they're implemented. A value selector (`Dropdown`) and a command list (`ActionMenu`) stay separate even though both are a trigger plus a popover. An editable field (`InputField`) and a tap-to-open chooser (`PickerField`) stay separate even though they look alike. Quick test: describe each one in a short phrase. If the phrases differ, they're separate components.
- Don't add a shared parent or generic abstraction across a couple of similar components. Wait until a third real case exists.
- Each component family gets its own subpackage, matching the existing ones under `designsystem/.../components/` (`buttons`, `controls`, `fields`, `menus`, `modals`, `pills`, ...) and `iosApp/DesignSystem/Sources/DesignSystem/Components/`. Don't drop files loose in `components/`.
- New component names carry no `Kluvs` prefix (`Icon`, `PrimaryButton`, `BottomSheet`).

## `:designsystem` (Android) / `DesignSystem` Swift package (iOS)
- `:designsystem` is an Android-only Compose module at the top level, not under `core/`, and it doesn't use the `kluvs.kmp.library` convention plugin (iOS UI is SwiftUI, not Compose Multiplatform). It's a leaf module with zero `core:*`/`feature:*` dependencies.
- Components are hollow: plain props and DS-local enums, no domain types. Translate between domain and component types with small private `toX()`/`toDomain()` extensions in the calling file in `composeApp` (see `ActiveSessionTab.kt`), never inside `:designsystem`.
- Read theme values through `KluvsTheme.typography` / `KluvsTheme.colors`, not `MaterialTheme.typography` / `colorScheme`. Don't pick a pixel value for a type rung that `typography.md` doesn't define. Use the closest existing family.
- When moving a component, find its callers with a usage grep (`ComponentName(`), not an import grep. Same-package callers have no import line and break silently.

## Previews
- Make previews interactive: hold state with local `remember { mutableStateOf(...) }` and wire the real callbacks, never `{}`. Include enabled and disabled states where they apply.
- Window- or popup-based composables (`AlertDialog`, `ModalBottomSheet`, `DropdownMenu`) render blank in static previews. Rebuild the component's real anatomy in the preview instead (header, body, footer, dividers, container color).

## Sheets and modals
- Web modals become bottom sheets on mobile: forms use a sheet, confirm-only prompts use a dialog (`../design-system/docs/modal.md`).
- Sheets (Android `BottomSheet` / iOS `.sheet`) are stateless views driven by callbacks into the screen's existing ViewModel. Don't create a dedicated ViewModel for a sheet, even if a ticket sketch suggests one. For async load-on-open, use the per-item lazy-map pattern (`ClubDetailsState.discussionRosters` + `ClubDetailsViewModel.onLoadAttendanceRoster`). A new ViewModel is justified only for a full navigation destination (e.g. `BookDetailsViewModel`).
