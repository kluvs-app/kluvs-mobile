package com.ivangarzab.kluvs.designsystem.components.pills

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.statusSuccess

/**
 * One-shot action rendered as a tiny outlined chip (design-system "Pill Button",
 * `.kluvs-btn-pill` — see design-system/docs/buttons.md), e.g. "Copy Club ID". Grey
 * outline/text by default; flips to green when [success] is true. Hollow — the caller
 * owns the transient timing (e.g. flip [success] on click, revert it after a delay);
 * this component only renders whichever state it's given.
 */
@Composable
fun TriggerPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    success: Boolean = false,
    successText: String = text,
    enabled: Boolean = true,
) {
    val tint = if (success) statusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, tint, RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (success) successText else text,
            style = KluvsTheme.typography.finePrint,
            color = tint,
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_TriggerPill() = KluvsTheme {
    var success by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TriggerPill(text = "Copy Club ID", onClick = { success = true }, success = success, successText = "Copied!")
        TriggerPill(text = "Copy Club ID", onClick = {}, enabled = false)
    }
}
