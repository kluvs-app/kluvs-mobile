package com.ivangarzab.kluvs.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// M3 type scale mapped to design-system/tokens.json (typography.scale + typography.tier).
// Serif (EB Garamond) carries display/headline/title roles — wordmark, headings, section headers.
// Sans (IBM Plex Sans) carries body/label roles — the four-tier content hierarchy + UI chrome.
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 96.sp, lineHeight = 104.sp), // scale.display-1
    displayMedium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp), // scale.display-2
    displaySmall = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),

    headlineLarge = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp), // scale.page-heading
    headlineMedium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp), // scale.section-heading

    titleLarge = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp), // top app-bar / dialog titles
    // tier.1-section-header — "Current Book", "Next Discussion". Pair with colorScheme.onSurfaceVariant.
    titleMedium = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = ebGaramond, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp), // scale.card-heading

    // tier.2-primary-content — club names, book titles, member names. Pair with colorScheme.onSurface.
    bodyLarge = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    // tier.3-supporting-details — counts, metadata, handles, dates. Pair with colorScheme.onSurfaceVariant.
    bodyMedium = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    // tier.4-fine-print — version numbers, disclaimers. Pair with colorScheme.inverseOnSurface.
    bodySmall = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),

    labelLarge = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp), // component.button.primary
    labelMedium = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp), // component.button.outlined/text
    // eyebrow / role-eyebrow / modal-label — uppercase + letterSpacing applied via textTransform at call sites.
    labelSmall = TextStyle(fontFamily = ibmPlexSans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.14.em),
)
