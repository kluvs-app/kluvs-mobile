import SwiftUI
import UIKit

// Synced from design-system/tokens.json — do not hardcode color values elsewhere.
extension Color {
    // MARK: - Brand
    /// Copper — the only chromatic accent. One per view, on the primary CTA and active state only.
    public static let brandOrange = Color(hex: 0xD16D30)
    public static let brandOrangeHover = Color(hex: 0xB85A22)
    public static let brandOrangeLight = Color(hex: 0xE8944D)
    /// Teal-green — success states, 'joined' indicators.
    public static let brandGreen = Color(hex: 0x48A480)
    public static let brandGreenHover = Color(hex: 0x3A8A6A)
    /// Blue-teal — admin role color, info accents.
    public static let brandBlue = Color(hex: 0x006781)
    public static let brandBlueHover = Color(hex: 0x005568)
    /// Text/icon color on copper surfaces. Contrast vs primary: 3.53:1 — documented exception.
    public static let brandOnPrimary = Color(hex: 0xFFFFFF)

    // MARK: - Warm-dark surface scale (product, dark-by-default)
    public static let warmDarkNav = Color(hex: 0x0F0D0A)
    public static let warmDarkBase = Color(hex: 0x140F0D)
    public static let warmDarkBar = Color(hex: 0x1A140F)
    public static let warmDarkCard = Color(hex: 0x241C17)
    public static let warmDarkCard2 = Color(hex: 0x332B24)
    /// Tinted warm fill for the highlighted Next Discussion card.
    public static let warmDarkAccentFill = Color(hex: 0x382112)

    // MARK: - Foreground on warm-dark surfaces
    /// Cream — label/variant/accent text on dark (wordmark, avatar initials, role label, input labels). NOT primary body text.
    public static let foregroundWarmPrimary = Color(hex: 0xF2EDE5)
    public static let foregroundWarmTertiary = Color(hex: 0x8C8073)
    public static let foregroundWarmPlaceholder = Color(hex: 0x6B5F52)
    public static let foregroundWarmDisabled = Color(hex: 0x4D4033)
    /// Primary body text on dark surfaces.
    public static let contentDarkPrimary = Color(hex: 0xFFFFFF)

    // MARK: - Light surfaces (auth / marketing)
    public static let lightPage = Color(hex: 0xF2EDE5)
    public static let lightBar = Color(hex: 0xF6F0E7)
    public static let lightCard = Color(hex: 0xFAF6EF)
    public static let lightDeep = Color(hex: 0xE8DECC)
    public static let lightDivider = Color(hex: 0xE5DCCB)

    // MARK: - Foreground on light surfaces
    public static let foregroundLightPrimary = Color(hex: 0x1A1A1A)
    public static let foregroundLightSecondary = Color(hex: 0x666666)
    /// Dark Chocolate — label/variant/accent text on light. Inverse of foregroundWarmPrimary.
    public static let foregroundLightLabelVariant = Color(hex: 0x140F0D)
    public static let foregroundLightTertiary = Color(hex: 0x7A6C5E)
    public static let foregroundLightPlaceholder = Color(hex: 0x9A8C7E)
    public static let foregroundLightDisabled = Color(hex: 0xC2B6A8)

    // MARK: - Role colors
    /// Mustard — Owner role badge. ~7:1 on dark, ~3:1 on light. Usable as graphical badge on both surfaces.
    public static let roleOwner = Color(hex: 0xC9900A)
    /// Teal — Admin role badge/dot. Graphical indicator only — never body text on dark (2.95:1, known exception).
    public static let roleAdmin = Color(hex: 0x006781)
    /// Lighter teal — Admin role *label text* on dark surfaces (roleAdmin fails AA as text on dark).
    public static let roleAdminOnDark = Color(hex: 0x7BA8B8)
    /// Member role label text — cream on dark.
    public static let roleMemberLabel = Color(hex: 0xF2EDE5)

    // MARK: - Avatar hue palette — deterministic per-user background for initials avatars
    public static let avatarHuesDark: [Color] = [
        Color(hex: 0x331F17), Color(hex: 0x38271A), Color(hex: 0x2B231D), Color(hex: 0x42251A),
        Color(hex: 0x261A14), Color(hex: 0x3D2E24), Color(hex: 0x4A2E1F), Color(hex: 0x2E2218),
        Color(hex: 0x3A1E16), Color(hex: 0x453325), Color(hex: 0x302018), Color(hex: 0x3D2115),
    ]
    public static let avatarHuesLight: [Color] = [
        Color(hex: 0xF9EDE6), Color(hex: 0xF4EBD9), Color(hex: 0xEBE6E0), Color(hex: 0xF8E3D8),
        Color(hex: 0xF1EBE6), Color(hex: 0xF5E8DF), Color(hex: 0xF9E6D5), Color(hex: 0xEBE8D8),
        Color(hex: 0xF6E2DD), Color(hex: 0xEFE4D6), Color(hex: 0xEFE1D2), Color(hex: 0xF2E1DC),
    ]
    /// Overflow-chip background for `AvatarStack` — matches web's hardcoded OVERFLOW_BG.
    public static let avatarStackOverflowBg = Color(hex: 0x4D4033)

    // MARK: - Adaptive screen surfaces (mirrors Android's MaterialTheme.colorScheme.background/surface)
    /// Screen background — warmDarkBase on dark, lightPage (cream) on light. Use this instead of
    /// the stock `Color(uiColor: .systemBackground)`, which ignores the Kluvs warm palette entirely.
    public static var kluvsBackground: Color {
        Color(UIColor { $0.userInterfaceStyle == .dark ? UIColor(Color.warmDarkBase) : UIColor(Color.lightPage) })
    }
    /// Card/elevated surface — warmDarkCard on dark, lightCard on light.
    public static var kluvsSurface: Color {
        Color(UIColor { $0.userInterfaceStyle == .dark ? UIColor(Color.warmDarkCard) : UIColor(Color.lightCard) })
    }

    // MARK: - Status
    /// AA on dark (5.06:1). Known exception on light (3.76:1) — always paired with an "Error:" prefix.
    public static let statusDanger = Color(hex: 0xEF4444)
    public static let statusDangerHover = Color(hex: 0xDC2626)
    public static let statusSuccess = Color(hex: 0x48A480)
    /// Deep wine — the offline banner fill. Same on dark and light; a state, never used for danger.
    public static let statusWine = Color(hex: 0x4F000C)
    /// Cream text on `statusWine` (13.5:1). Fixed on both themes — does not invert like the warm foreground does.
    public static let statusOnWine = Color(hex: 0xF2EDE5)
    /// Pressed "yes" segment background in AttendanceControl.
    public static let statusSuccessSubtle = Color(hex: 0x48A480, alpha: 0.15)
    /// Pressed "no" segment background in AttendanceControl.
    public static let statusDangerSubtle = Color(hex: 0xEF4444, alpha: 0.08)

    // MARK: - OAuth provider brand colors
    public static let discordBlue = Color(hex: 0x5865F2)
    public static let discordText = Color(hex: 0xFFFFFF)
    public static let googleGray = Color(hex: 0xF2F2F2)
    public static let googleTextGray = Color(hex: 0x1F1F1F)
    public static let googleStroke = Color(hex: 0xD1D1D1)
    public static let appleBlack = Color(hex: 0x0F0F0F)
    public static let appleText = Color(hex: 0xFFFFFF)
}

// Helper extensions
extension UIColor {
    public convenience init(hex: UInt, alpha: CGFloat = 1.0) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255.0,
            green: CGFloat((hex >> 8) & 0xFF) / 255.0,
            blue: CGFloat(hex & 0xFF) / 255.0,
            alpha: alpha
        )
    }
}

extension Color {
    public init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
