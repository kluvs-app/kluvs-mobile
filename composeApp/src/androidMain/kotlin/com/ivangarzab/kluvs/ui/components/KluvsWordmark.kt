package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.LocalKluvsLabelColor
import com.ivangarzab.kluvs.designsystem.theme.ebGaramond

/**
 * The "KLUVS" wordmark — typographic, EB Garamond Bold, wide tracking.
 * Matches design-system/assets/kluvs-wordmark-{light,dark}.svg (48px / letter-spacing 8.6).
 * Color is theme-aware: cream on dark, dark chocolate on light ([LocalKluvsLabelColor]).
 */
@Composable
fun KluvsWordmark(modifier: Modifier = Modifier) {
    Text(
        text = "KLUVS",
        modifier = modifier,
        color = LocalKluvsLabelColor.current,
        fontFamily = ebGaramond,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.18.em,
    )
}

@PreviewLightDark
@Composable
fun Preview_KluvsWordmark() = KluvsTheme {
    KluvsWordmark(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp)
    )
}
