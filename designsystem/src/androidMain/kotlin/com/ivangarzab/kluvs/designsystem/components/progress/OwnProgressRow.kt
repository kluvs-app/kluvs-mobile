package com.ivangarzab.kluvs.designsystem.components.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.OutlinedButton
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.feature

/**
 * A member's progress on a session book: thin bar, status label, and the entry point to the
 * progress edit sheet. Hollow — takes plain values instead of the app's `OwnProgressInfo` domain
 * type; callers destructure their own model before calling this.
 *
 * Shared between the Clubs screen (active session) and the Me screen (shelf rows), and — as of
 * this pass — the Clubs Overview tab too, which previously had its own private near-duplicate of
 * this exact component (same layout, same button, differing only in whether [leftLabel] renders
 * italic). That duplicate is now deleted in favor of this, with [leftLabelEmphasized].
 *
 * @param percent 0-100, or null if progress hasn't started yet — drives both the progress bar and
 * the "Update" vs "Track Progress" button label.
 * @param statusLabel e.g. "42 of 169 pages" — null renders "Not started".
 * @param leftLabel e.g. "Your progress", "Next · Thu, Dec 31", or a formatted "3 of 5 discussions"
 * string — callers own the exact copy.
 * @param leftLabelEmphasized italicizes [leftLabel] via the `feature` modifier (Caption+feature) —
 * the Overview tab's discussion-count line used this; plain shelf-row labels don't.
 */
@Composable
fun OwnProgressRow(
    percent: Int?,
    statusLabel: String?,
    onUpdateProgress: () -> Unit,
    modifier: Modifier = Modifier,
    leftLabel: String = "Your progress",
    leftLabelEmphasized: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressBar(
                percent = percent ?: 0,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                text = if (percent != null) "Update" else "Track Progress",
                onClick = onUpdateProgress
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = leftLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = if (leftLabelEmphasized) KluvsTheme.typography.caption.feature() else KluvsTheme.typography.caption
            )
            Text(
                text = statusLabel ?: "Not started",
                color = MaterialTheme.colorScheme.primary,
                style = KluvsTheme.typography.caption
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_OwnProgressRow() = KluvsTheme {
    OwnProgressRow(
        percent = 25,
        statusLabel = "42 of 169 pages",
        onUpdateProgress = {},
        leftLabel = "Next · Thu, Dec 31"
    )
}

@PreviewLightDark
@Composable
private fun Preview_OwnProgressRow_Emphasized() = KluvsTheme {
    OwnProgressRow(
        percent = 60,
        statusLabel = "42 of 169 pages",
        onUpdateProgress = {},
        leftLabel = "3 of 5 discussions",
        leftLabelEmphasized = true,
    )
}
