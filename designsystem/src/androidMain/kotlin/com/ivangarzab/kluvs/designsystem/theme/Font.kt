package com.ivangarzab.kluvs.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.ivangarzab.kluvs.designsystem.R

// Two-register type system synced from design-system/tokens.json (typography.font-family).
// Serif (EB Garamond): wordmark, display text, page/section headings, book titles (italic).
// Sans (IBM Plex Sans): all UI chrome — body, labels, buttons, tabs, helper text.
// No other font families.

/** Serif register — literary content. Italic reserved for book titles. */
val ebGaramond = FontFamily(
    Font(R.font.eb_garamond_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.eb_garamond_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.eb_garamond_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.eb_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic),
)

/** Sans register — UI chrome, body, labels. */
val ibmPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold, FontStyle.Normal),
)
