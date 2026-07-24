package com.ivangarzab.kluvs.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.lightBar
import com.ivangarzab.kluvs.designsystem.theme.warmDarkBar

/**
 * Centered confirm/cancel dialog — design-system "Modal", centered-dialog form (see
 * design-system/docs/modal.md). Reserved for hard, confirm-only confirmations (Delete Club,
 * Sign Out, Remove Member) — anything with fields to fill in belongs in [BottomSheet] instead.
 *
 * @param isDestructive true for irreversible actions (delete/remove) — tints the title and
 * confirm label danger-red instead of copper. Sign-out-style confirmations (reversible, just
 * disruptive) should pass false.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    isDestructive: Boolean = false,
) {
    val accentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val containerColor = if (isSystemInDarkTheme()) warmDarkBar else lightBar
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = containerColor,
        title = {
            Text(
                text = title.uppercase(),
                style = KluvsTheme.typography.eyebrow,
                color = accentColor,
            )
        },
        text = {
            Text(
                text = message,
                style = KluvsTheme.typography.body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = accentColor),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            ) {
                Text(dismissLabel)
            }
        },
    )
}

/**
 * Static previews can't render [AlertDialog]'s actual window content (a long-standing Compose
 * Preview limitation shared by any Dialog/ModalBottomSheet-based composable) — this reconstructs
 * the same visual shape as a plain [Column] instead. Use Android Studio's Interactive Mode, or a
 * real device/emulator, to preview [ConfirmationDialog] itself.
 */
@Composable
private fun ConfirmationDialogPreviewShape(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    isDestructive: Boolean,
) {
    val accentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val containerColor = if (isSystemInDarkTheme()) warmDarkBar else lightBar
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(16.dp)),
    ) {
        // Header — eyebrow label, border-bottom.
        Text(
            text = title.uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = accentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 20.dp),
        )
        HorizontalDivider(color = dividerColor)

        // Body.
        Text(
            text = message,
            style = KluvsTheme.typography.body.medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )

        // Footer — Cancel leading (ghost text), action trailing, border-top. Matches the real
        // component's use of plain tinted TextButtons (Android AlertDialog convention), not a
        // filled button — see ConfirmationDialog's own confirmButton/dismissButton above.
        HorizontalDivider(color = dividerColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text(dismissLabel)
            }
            TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = accentColor)) {
                Text(confirmLabel)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ConfirmationDialog_Destructive() = KluvsTheme {
    ConfirmationDialogPreviewShape(
        title = "Delete Club",
        message = "Are you sure you want to delete this club? This action cannot be undone.",
        confirmLabel = "Delete",
        dismissLabel = "Cancel",
        isDestructive = true,
    )
}

@PreviewLightDark
@Composable
private fun Preview_ConfirmationDialog_Default() = KluvsTheme {
    ConfirmationDialogPreviewShape(
        title = "Sign Out",
        message = "Are you sure you want to sign out?",
        confirmLabel = "Sign Out",
        dismissLabel = "Cancel",
        isDestructive = false,
    )
}
