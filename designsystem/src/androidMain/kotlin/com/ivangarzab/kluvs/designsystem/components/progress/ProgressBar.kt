package com.ivangarzab.kluvs.designsystem.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Determinate reading-progress bar (design-system "Reading Progress", see
 * design-system/docs/states.md §4) — a pill-shaped track with a pill-shaped copper fill.
 * Hand-drawn rather than wrapping `androidx.compose.material3.LinearProgressIndicator`
 * directly, since recent M3 versions add a visible gap between the filled/unfilled track
 * and a "stop indicator" dot at the end by default — neither exists in the real spec.
 *
 * @param percent 0-100.
 */
@Composable
fun ProgressBar(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = (percent / 100f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_ProgressBar() = KluvsTheme {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProgressBar(percent = 0)
        ProgressBar(percent = 47)
        ProgressBar(percent = 100)
    }
}
