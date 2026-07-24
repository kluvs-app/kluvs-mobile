package com.ivangarzab.kluvs.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.buttons.OutlinedButton
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.statusDangerSubtle

/**
 * Secondary destructive action tucked inside an edit sheet's body (e.g. "Delete club" inside
 * Edit Club) — design-system "Danger zone box" (see design-system/docs/modal.md). Sits at the
 * bottom of [BottomSheet]'s body content, never as its own footer button. The box carries the
 * danger signal (subtle red tint); the button inside stays quiet (muted [OutlinedButton]) —
 * tapping it is expected to open a [ConfirmationDialog], not act immediately.
 */
@Composable
fun DangerZoneBox(
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(statusDangerSubtle, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "DANGER ZONE",
            style = KluvsTheme.typography.eyebrow,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedButton(text = actionLabel, onClick = onActionClick)
    }
}

@PreviewLightDark
@Composable
private fun Preview_DangerZoneBox() = KluvsTheme {
    DangerZoneBox(actionLabel = "Delete club", onActionClick = {})
}
