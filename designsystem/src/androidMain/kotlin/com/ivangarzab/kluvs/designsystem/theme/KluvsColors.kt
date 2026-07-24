package com.ivangarzab.kluvs.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The Kluvs color-role model — design-system/tokens.json's surface/foreground/status scale,
 * addressed by what each role *represents* (card, bar, divider, content) rather than borrowed
 * M3 vocabulary (surface, surfaceVariant, onSurfaceVariant). Supersedes reading
 * [androidx.compose.material3.MaterialTheme.colorScheme] directly from Kluvs components, which
 * has no slot for the `bar` surface tier at all — every component that needed it (Avatar,
 * BookCoverPlaceholder, ConfirmationDialog, BottomSheet) had to hand-roll its own
 * `isSystemInDarkTheme()` branch instead of reading a shared role.
 *
 * [MaterialTheme]'s own `ColorScheme` is untouched and still provided in [KluvsTheme] — it
 * remains the compat shim stock M3 widgets (Button, TextField, Scaffold) read directly. New
 * Kluvs component code should read [KluvsTheme.colors], never `MaterialTheme.colorScheme`.
 */
data class KluvsColors(
    val background: Color,
    val bar: Color,
    val card: Color,
    val cardAlt: Color,
    val divider: Color,
    val content: Color,
    val contentMuted: Color,
    val labelVariant: Color,
    val placeholder: Color,
    val disabled: Color,
    val accent: Color,
    val onAccent: Color,
    val secondary: Color,
    val tertiary: Color,
    val danger: Color,
    val dangerSubtle: Color,
    val success: Color,
    val successSubtle: Color,
)

val kluvsColorsDark = KluvsColors(
    background = warmDarkBase,
    bar = warmDarkBar,
    card = warmDarkCard,
    cardAlt = warmDarkCard2,
    divider = warmDarkCard2,
    content = contentDarkPrimary,
    contentMuted = foregroundWarmTertiary,
    labelVariant = foregroundWarmPrimary,
    placeholder = foregroundWarmPlaceholder,
    disabled = foregroundWarmDisabled,
    accent = brandPrimary,
    onAccent = brandOnPrimary,
    secondary = brandSecondary,
    tertiary = brandTertiary,
    danger = statusDanger,
    dangerSubtle = statusDangerSubtle,
    success = statusSuccess,
    successSubtle = statusSuccessSubtle,
)

val kluvsColorsLight = KluvsColors(
    background = lightPage,
    bar = lightBar,
    card = lightCard,
    cardAlt = lightDeep,
    divider = lightDivider,
    content = foregroundLightPrimary,
    contentMuted = foregroundLightTertiary,
    labelVariant = foregroundLightLabelVariant,
    placeholder = foregroundLightPlaceholder,
    disabled = foregroundLightDisabled,
    accent = brandPrimary,
    onAccent = brandOnPrimary,
    secondary = brandSecondary,
    tertiary = brandTertiary,
    danger = statusDanger,
    dangerSubtle = statusDangerSubtle,
    success = statusSuccess,
    successSubtle = statusSuccessSubtle,
)

val LocalKluvsColors = compositionLocalOf { kluvsColorsDark }
