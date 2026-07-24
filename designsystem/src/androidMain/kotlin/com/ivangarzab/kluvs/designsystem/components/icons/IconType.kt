package com.ivangarzab.kluvs.designsystem.components.icons

/**
 * Every icon in active use across the app, custom-drawable or Material-backed.
 * Kept 1:1 with real call sites (no speculative entries) so this enum doubles as
 * a live audit of icon usage — add a member only when a screen actually needs it.
 */
enum class IconType {
    Back,
    Book,
    Checkmark,
    Club,
    Clubs,
    Discord,
    Edit,
    Email,
    Google,
    Help,
    Location,
    Password,
    Settings,
    SignOut,
    User,

    ArrowBack,
    Add,
    Search,
    MoreVert,
    ChevronRight,
    ChevronDown,
    Check,
    Favorite,
    FavoriteOutline,
}
