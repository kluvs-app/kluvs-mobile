import SwiftUI

/// Every icon in active use across the iOS app, custom-drawable or SF Symbol-backed — mirrors
/// Android's `IconType` (`designsystem/.../components/icons/IconType.kt`) case-for-case where a
/// real Android call site exists. `apple`/`crown`/`info`/`shield` are real iOS-only cases with no
/// Android equivalent yet (Apple Sign-In, member role badges) — a known, already-tracked
/// platform parity gap (Android has `ic_crown`/`ic_info`/`ic_shield`/etc. as unused drawables),
/// not something to drop here to force a false 1:1 match.
///
/// Kept 1:1 with real call sites, same discipline as Android's own doc comment: add a member only
/// when a screen actually needs it, so this enum doubles as a live audit of icon usage.
public enum IconType {
    // Custom drawable-backed (app's own Assets.xcassets — unchanged by this package move).
    case club, clubs, user, book, location, settings, help, checkmark
    case signOut, email, password, edit, back, info, crown, shield
    case discord, google, apple
    case visibility, visibilityOff

    // SF Symbol-backed — mirrors Android's Material-icon half of IconType.
    case arrowBack, add, search, moreVert, chevronRight, chevronDown, check, close
    case favorite, favoriteOutline
    case editNote, reading
    case error

    /// The asset-catalog name for custom-drawable cases, `nil` for SF Symbol-backed ones.
    var assetName: String? {
        switch self {
        case .club: "ic_club"
        case .clubs: "ic_clubs"
        case .user: "ic_user"
        case .book: "ic_book"
        case .location: "ic_location"
        case .settings: "ic_settings"
        case .help: "ic_help"
        case .checkmark: "ic_checkmark"
        case .signOut: "ic_logout"
        case .email: "ic_email"
        case .password: "ic_password"
        case .edit: "ic_edit"
        case .back: "ic_back"
        case .info: "ic_info"
        case .crown: "ic_crown"
        case .shield: "ic_shield"
        case .discord: "ic_discord"
        case .google: "ic_google"
        case .visibility: "ic_visibility"
        case .visibilityOff: "ic_visibility_off"
        default: nil
        }
    }

    /// The SF Symbol name for symbol-backed cases, `nil` for custom-drawable ones.
    var symbolName: String? {
        switch self {
        case .apple: "apple.logo"
        case .arrowBack: "arrow.left"
        case .add: "plus"
        case .search: "magnifyingglass"
        // Apple's own overflow-menu idiom is horizontal ellipsis, unlike Android's vertical
        // "more_vert" — a deliberate platform-convention difference, not a mismatch to fix.
        case .moreVert: "ellipsis"
        case .chevronRight: "chevron.right"
        case .chevronDown: "chevron.down"
        case .check: "checkmark"
        case .close: "xmark"
        case .favorite: "heart.fill"
        case .favoriteOutline: "heart"
        // Placeholder SF Symbols — swap during the planned iconography pass, not meant as final.
        case .editNote: "pencil.and.list.clipboard"
        case .reading: "book.fill"
        // Matches Android's IconType.Error -> Icons.Filled.Warning mapping.
        case .error: "exclamationmark.triangle.fill"
        default: nil
        }
    }

    /// The raw, unmodified image — no `.resizable()`, no forced rendering mode. Most call sites
    /// should use the `Icon` view instead (resizable + tinted); this exists `public` for the rare
    /// case that needs to control rendering mode itself, e.g. a brand-colored OAuth glyph (Discord/
    /// Google) that must NOT be template-tinted the way every other icon is — same reason
    /// Android's `SocialButton` calls `painterResource(...)` directly instead of going through the
    /// shared `Icon` composable.
    ///
    /// Custom-drawable cases resolve from `Bundle.module` (`Resources/Icons.xcassets`, bundled
    /// into this package, not the app) — unlike fonts and colors elsewhere in this package, image
    /// assets do NOT resolve process-wide by default; `Image(name:)` without an explicit bundle
    /// only searches the *main* bundle, so a package-bundled asset is invisible to it unless told
    /// where to look.
    public var image: Image {
        if let symbolName {
            Image(systemName: symbolName)
        } else if let assetName {
            Image(assetName, bundle: .module)
        } else {
            // Unreachable: every case sets exactly one of assetName/symbolName above.
            Image(systemName: "questionmark")
        }
    }
}
