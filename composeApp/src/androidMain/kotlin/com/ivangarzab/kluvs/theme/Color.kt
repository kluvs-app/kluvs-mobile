package com.ivangarzab.kluvs.theme

import androidx.compose.ui.graphics.Color

// Synced from design-system/tokens.json — do not hardcode color values elsewhere.

// --- Brand ---
/** Copper — the only chromatic accent. One per view, on the primary CTA and active state only. */
val brandPrimary = Color(0xFFD16D30)
val brandPrimaryHover = Color(0xFFB85A22)
val brandPrimaryLight = Color(0xFFE8944D)
/** Teal-green — success states, 'joined' indicators. */
val brandSecondary = Color(0xFF48A480)
val brandSecondaryHover = Color(0xFF3A8A6A)
/** Blue-teal — admin role color, info accents. */
val brandTertiary = Color(0xFF006781)
val brandTertiaryHover = Color(0xFF005568)
/** Text/icon color on copper surfaces. Contrast vs primary: 3.53:1 — documented exception. */
val brandOnPrimary = Color(0xFFFFFFFF)

// --- Warm-dark surface scale (product, dark-by-default) ---
val warmDarkNav = Color(0xFF0F0D0A)
val warmDarkBase = Color(0xFF140F0D)
val warmDarkBar = Color(0xFF1A140F)
val warmDarkCard = Color(0xFF241C17)
val warmDarkCard2 = Color(0xFF332B24)
/** Tinted warm fill for the highlighted Next Discussion card. */
val warmDarkAccentFill = Color(0xFF382112)

// --- Foreground on warm-dark surfaces ---
/** Cream — label/variant/accent text on dark (wordmark, avatar initials, role label, input labels). NOT primary body text. */
val foregroundWarmPrimary = Color(0xFFF2EDE5)
val foregroundWarmTertiary = Color(0xFF8C8073)
val foregroundWarmPlaceholder = Color(0xFF6B5F52)
val foregroundWarmDisabled = Color(0xFF4D4033)
/** Primary body text on dark surfaces. */
val contentDarkPrimary = Color(0xFFFFFFFF)

// --- Light surfaces (auth / marketing) ---
val lightPage = Color(0xFFF2EDE5)
val lightBar = Color(0xFFF6F0E7)
val lightCard = Color(0xFFFAF6EF)
val lightDeep = Color(0xFFE8DECC)
val lightDivider = Color(0xFFE5DCCB)

// --- Foreground on light surfaces ---
val foregroundLightPrimary = Color(0xFF1A1A1A)
val foregroundLightSecondary = Color(0xFF666666)
/** Dark Chocolate — label/variant/accent text on light. Inverse of foregroundWarmPrimary. */
val foregroundLightLabelVariant = Color(0xFF140F0D)
val foregroundLightTertiary = Color(0xFF7A6C5E)
val foregroundLightPlaceholder = Color(0xFF9A8C7E)
val foregroundLightDisabled = Color(0xFFC2B6A8)

// --- Role colors ---
/** Mustard — Owner role badge. ~7:1 on dark, ~3:1 on light. Usable as graphical badge on both surfaces. */
val roleOwner = Color(0xFFC9900A)
/** Teal — Admin role badge/dot. Graphical indicator only — never body text on dark (2.95:1, known exception). */
val roleAdmin = Color(0xFF006781)
/** Lighter teal — Admin role *label text* on dark surfaces (roleAdmin fails AA as text on dark). */
val roleAdminOnDark = Color(0xFF7BA8B8)
/** Member role label text — cream on dark. */
val roleMemberLabel = Color(0xFFF2EDE5)

// --- Status ---
/** AA on dark (5.06:1). Known exception on light (3.76:1) — always paired with an "Error:" prefix. */
val statusDanger = Color(0xFFEF4444)
val statusDangerHover = Color(0xFFDC2626)
val statusSuccess = Color(0xFF48A480)

// --- OAuth provider brand colors ---
val providerDiscordBg = Color(0xFF5865F2)
val providerDiscordText = Color(0xFFFFFFFF)
val providerGoogleBg = Color(0xFFF2F2F2)
val providerGoogleText = Color(0xFF1F1F1F)
val providerGoogleStroke = Color(0xFFD1D1D1)
val providerAppleBg = Color(0xFF0F0F0F)
val providerAppleText = Color(0xFFFFFFFF)
