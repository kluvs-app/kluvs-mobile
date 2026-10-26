package com.ivangarzab.kluvs.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.modals.ConfirmationDialog

/**
 * Confirm/cancel dialog for the "Delete Account" row in [AccountSection] — same
 * [ConfirmationDialog] primitive and destructive styling used by [com.ivangarzab.kluvs.ui.me.LogoutConfirmationDialog].
 *
 * No `@Preview` here, matching `LogoutConfirmationDialog` — `ConfirmationDialog` is built on a
 * real `Dialog` window, which doesn't render in Compose's static preview renderer.
 */
@Composable
fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.delete_account),
        message = stringResource(R.string.delete_account_confirmation_message),
        confirmLabel = stringResource(R.string.delete_account),
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
