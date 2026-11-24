import SwiftUI
import UIKit

/// The Kluvs color-role model — mirrors kluvs-mobile's Android `KluvsColors`/`KluvsTheme.colors`
/// and `design-system/docs/colors.md`. Same role names as Android (`card`, `bar`, `divider`,
/// `content`, etc.) so the two platforms never drift into different vocabularies for the same
/// concept.
///
/// Unlike Android's Compose `CompositionLocal`-based `KluvsTheme.colors` (which needs a value
/// threaded through composition), each role here is a self-adapting `Color` built from a
/// `UIColor { trait in ... }` closure — the same mechanism `Color.kluvsBackground`/`kluvsSurface`
/// in `Colors.swift` already used. No environment plumbing needed: a view just reads
/// `KluvsTheme.colors.card` and it resolves to the right value for the current light/dark
/// appearance automatically, the same way Apple's own `.label`/`.systemBackground` do.
///
/// Raw token names on iOS (`Color.brandOrange`, `Color.warmDarkCard`, etc., in `Colors.swift`)
/// intentionally keep their existing names — this layer sits on top of them, it doesn't rename
/// them. Do not read `Color.warmDark*`/`Color.light*` tokens directly from new view code; always
/// go through `KluvsTheme.colors`.
public enum KluvsTheme {
    public enum colors {
        public static var background: Color { adaptive(dark: .warmDarkBase, light: .lightPage) }
        public static var bar: Color { adaptive(dark: .warmDarkBar, light: .lightBar) }
        public static var card: Color { adaptive(dark: .warmDarkCard, light: .lightCard) }
        public static var cardAlt: Color { adaptive(dark: .warmDarkCard2, light: .lightDeep) }
        public static var divider: Color { adaptive(dark: .warmDarkCard2, light: .lightDivider) }
        public static var content: Color { adaptive(dark: .contentDarkPrimary, light: .foregroundLightPrimary) }
        public static var contentMuted: Color { adaptive(dark: .foregroundWarmTertiary, light: .foregroundLightTertiary) }
        public static var labelVariant: Color { adaptive(dark: .foregroundWarmPrimary, light: .foregroundLightLabelVariant) }
        public static var placeholder: Color { adaptive(dark: .foregroundWarmPlaceholder, light: .foregroundLightPlaceholder) }
        public static var disabled: Color { adaptive(dark: .foregroundWarmDisabled, light: .foregroundLightDisabled) }

        // Brand/status roles don't switch between dark/light — same values on both surfaces.
        public static let accent = Color.brandOrange
        public static let onAccent = Color.brandOnPrimary
        public static let secondary = Color.brandGreen
        public static let tertiary = Color.brandBlue
        public static let danger = Color.statusDanger
        public static let dangerSubtle = Color.statusDangerSubtle
        public static let success = Color.statusSuccess
        public static let successSubtle = Color.statusSuccessSubtle
        public static let offline = Color.statusWine
        public static let onOffline = Color.statusOnWine

        private static func adaptive(dark: Color, light: Color) -> Color {
            Color(UIColor { $0.userInterfaceStyle == .dark ? UIColor(dark) : UIColor(light) })
        }
    }
}
