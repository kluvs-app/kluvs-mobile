package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * Generic destructive confirmation dialog for delete/remove operations.
 *
 * Presents a cancel-friendly dialog with a red confirm button to ensure
 * the user intentionally performs an irreversible action.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@PreviewLightDark
@Composable
fun Preview_ConfirmationDialog() = KluvsTheme {
    ConfirmationDialog(
        title = "Delete Club",
        message = "Are you sure you want to delete this club? This action cannot be undone.",
        confirmLabel = "Delete",
        onConfirm = {},
        onDismiss = {}
    )
}
