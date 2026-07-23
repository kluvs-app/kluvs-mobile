package com.ivangarzab.kluvs.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** A multi-rung family (Display / Headline / Title) — small/medium/large sizes of the same shape. */
data class KluvsTypeScale(
    val small: TextStyle,
    val medium: TextStyle,
    val large: TextStyle,
)

/** Body only has two rungs in design-system/docs/typography.md — no "small" observed in the audit. */
data class KluvsBodyScale(
    val large: TextStyle,
    val medium: TextStyle,
)

/**
 * The Kluvs typography model — design-system/docs/typography.md, tokens.json `typography.family`.
 * Supersedes borrowing [androidx.compose.material3.Typography]'s roles directly, which has no
 * concept of a serif/sans register switch or an italic modifier and forced every Garamond heading
 * into a single bold-non-italic shape regardless of what the design actually called for.
 *
 * Sizes below are the shared, platform-agnostic scale — the same numbers design-system/tokens.json
 * defines for web and (eventually) iOS. Do not invent a different value here for a rung that's
 * already defined there; if a size doesn't fit, that's a signal to use an adjacent rung or raise a
 * new rung with design-system, not to special-case Android.
 *
 * Modifiers are intentionally NOT part of this data class:
 * - `feature` (roman -> italic) — call [feature] on the relevant TextStyle. Valid on Headline,
 *   Title, and Caption only.
 * - `highlight` (step up one rung) — no API needed. Call sites pick the next rung directly
 *   (e.g. `title.medium` instead of `title.small`) for the singular emphasized instance in an
 *   otherwise-repeating list. Creates no new value.
 *
 * Line-heights and letter-spacings (aside from Eyebrow's, which is a real design-system value) are
 * first-pass Android-side estimates — design-system/tokens.json deliberately leaves those as an
 * implementation-layer decision, not yet a cross-platform-agreed value. Revisit once formalized.
 */
data class KluvsTypography(
    val display: KluvsTypeScale,
    val headline: KluvsTypeScale,
    val title: KluvsTypeScale,
    val body: KluvsBodyScale,
    val caption: TextStyle,
    val eyebrow: TextStyle,
    val label: TextStyle,
    val finePrint: TextStyle,
    val mono: TextStyle,
)

/**
 * The `feature` modifier — roman -> italic register flip signaling "the featured/current thing"
 * (a book title, the active reading session, an empty-state headline). Size and weight untouched.
 */
fun TextStyle.feature(): TextStyle = copy(fontStyle = FontStyle.Italic)

val kluvsTypography = KluvsTypography(
    display = KluvsTypeScale(
        small = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 52.sp),
        medium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 64.sp, lineHeight = 68.sp),
        large = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 96.sp, lineHeight = 100.sp),
    ),
    headline = KluvsTypeScale(
        small = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 30.sp, lineHeight = 36.sp),
        medium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 40.sp),
        large = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 40.sp, lineHeight = 46.sp),
    ),
    title = KluvsTypeScale(
        small = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
        medium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 24.sp),
        large = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp),
    ),
    body = KluvsBodyScale(
        large = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        medium = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    ),
    caption = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    eyebrow = TextStyle(
        fontFamily = ibmPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.14.em, // design-system/tokens.json typography.family.eyebrow.tracking — canonical, not an estimate.
        // Note: Compose TextStyle has no text-transform equivalent — callers must uppercase the
        // string themselves (see RoleEyebrow.kt for the established pattern).
    ),
    label = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
    finePrint = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    mono = TextStyle(
        // design-system/tokens.json font-family.mono — system-stack placeholder, no dedicated typeface chosen yet.
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)

/**
 * Static because typography doesn't vary per-recomposition the way ambient state (e.g. color)
 * might — matches AOSP's own `LocalTypography`/`LocalColorScheme`, both `staticCompositionLocalOf`.
 * Read via [KluvsTheme.type], not this directly.
 */
val LocalKluvsTypography = staticCompositionLocalOf { kluvsTypography }
