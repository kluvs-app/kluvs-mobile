package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.OwnProgressInfo
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * The signed-in member's progress on a session book: thin bar, status label,
 * and the entry point to the progress edit sheet ([ReadingProgressBottomSheet]).
 *
 * Shared between the Clubs screen (active session) and the Me screen (shelf rows).
 *
 * @param leftLabel Optional caption shown left of the status label, e.g. "Next · Thu, Dec 31"
 * on Me screen shelf rows. Defaults to "Your progress" (Clubs screen convention).
 */
@Composable
fun OwnProgressRow(
    ownProgress: OwnProgressInfo?,
    onUpdateProgress: () -> Unit,
    modifier: Modifier = Modifier,
    leftLabel: String = "Your progress",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { (ownProgress?.percent ?: 0) / 100f },
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onUpdateProgress) {
                Text(
                    text = if (ownProgress != null) "Update" else "Track Progress",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = leftLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = ownProgress?.label ?: "Not started",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_OwnProgressRow() = KluvsTheme {
    OwnProgressRow(
        ownProgress = OwnProgressInfo(
            progressId = "p0",
            type = ProgressType.PAGE,
            currentPage = 42,
            percentComplete = null,
            isCompleted = false,
            percent = 25,
            label = "42 of 169 pages"
        ),
        onUpdateProgress = {},
        leftLabel = "Next · Thu, Dec 31"
    )
}
