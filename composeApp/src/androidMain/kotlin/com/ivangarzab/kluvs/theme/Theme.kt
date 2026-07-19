package com.ivangarzab.kluvs.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

// Dark-by-default product surfaces — warm near-black scale from design-system/tokens.json.
private val DarkColorScheme = darkColorScheme(
    primary = brandPrimary,
    onPrimary = brandOnPrimary,
    primaryContainer = warmDarkAccentFill,
    onPrimaryContainer = brandPrimary,
    secondary = brandSecondary,
    onSecondary = contentDarkPrimary,
    tertiary = brandTertiary,
    onTertiary = contentDarkPrimary,
    background = warmDarkBase,
    onBackground = contentDarkPrimary,
    surface = warmDarkCard,
    onSurface = contentDarkPrimary,
    surfaceVariant = warmDarkCard2,
    onSurfaceVariant = foregroundWarmTertiary,
    inverseSurface = lightPage,
    inverseOnSurface = foregroundWarmTertiary,
    error = statusDanger,
    onError = contentDarkPrimary,
    outline = warmDarkCard2,
    outlineVariant = warmDarkCard2,
)

// Auth / marketing surfaces — cream scale, inverse of the warm-dark stack.
private val LightColorScheme = lightColorScheme(
    primary = brandPrimary,
    onPrimary = brandOnPrimary,
    primaryContainer = lightDeep,
    onPrimaryContainer = brandPrimary,
    secondary = brandSecondary,
    onSecondary = foregroundLightPrimary,
    tertiary = brandTertiary,
    onTertiary = foregroundLightPrimary,
    background = lightPage,
    onBackground = foregroundLightPrimary,
    surface = lightCard,
    onSurface = foregroundLightPrimary,
    surfaceVariant = lightDeep,
    onSurfaceVariant = foregroundLightTertiary,
    inverseSurface = warmDarkBase,
    inverseOnSurface = foregroundLightSecondary,
    error = statusDanger,
    onError = foregroundLightPrimary,
    outline = lightDivider,
    outlineVariant = lightDivider,
)


/**
 * Cream on dark / dark-chocolate on light — the "label/variant/accent" role for wordmark,
 * avatar initials, role labels, and input labels (design-system foreground-warm.primary /
 * foreground-light.label-variant). Distinct from [MaterialTheme.colorScheme.onSurfaceVariant],
 * which carries meta/supporting text instead.
 */
val LocalKluvsLabelColor = compositionLocalOf { foregroundWarmPrimary }

@Composable
fun KluvsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val labelColor = if (darkTheme) foregroundWarmPrimary else foregroundLightLabelVariant

    CompositionLocalProvider(LocalKluvsLabelColor provides labelColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}