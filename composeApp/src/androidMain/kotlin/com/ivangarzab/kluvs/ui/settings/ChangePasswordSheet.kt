package com.ivangarzab.kluvs.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.designsystem.components.fields.PasswordField
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheetFooter
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.settings.presentation.SettingsState
import com.ivangarzab.kluvs.ui.auth.ErrorBanner
import com.ivangarzab.kluvs.ui.extensions.toLocalizedMessage

/**
 * "Change Password" as a bottom sheet extending [com.ivangarzab.kluvs.settings.presentation.SettingsViewModel]
 * rather than a dedicated sheet view model — same shared-VM pattern used for auth's forgot-
 * password sheet (see [com.ivangarzab.kluvs.ui.auth.AuthForgotSheet]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    state: SettingsState,
    onNewPasswordFieldChange: (String) -> Unit,
    onConfirmPasswordFieldChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val errorMessage = state.changePasswordGeneralError?.toLocalizedMessage()

    BottomSheet(
        header = stringResource(R.string.change_password),
        onDismiss = onDismiss,
        footer = {
            BottomSheetFooter(
                actionLabel = stringResource(R.string.update_password),
                onAction = onSubmit,
                onCancel = onDismiss,
                actionEnabled = !state.isChangingPassword,
            )
        },
    ) {
        PasswordField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.new_password),
            value = state.newPasswordField,
            onValueChange = onNewPasswordFieldChange,
            error = state.newPasswordError,
            helperText = if (state.newPasswordError == null) {
                stringResource(R.string.min_eight_characters)
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.confirm_new_password),
            value = state.confirmPasswordField,
            onValueChange = onConfirmPasswordFieldChange,
            error = state.confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { onSubmit() },
            ),
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBanner(message = errorMessage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview_ChangePasswordSheet() = KluvsTheme {
    ChangePasswordSheet(
        state = SettingsState(),
        onNewPasswordFieldChange = {},
        onConfirmPasswordFieldChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview_ChangePasswordSheet_Error() = KluvsTheme {
    ChangePasswordSheet(
        state = SettingsState(
            newPasswordField = "abc",
            confirmPasswordField = "abc",
            changePasswordGeneralError = AuthError.WeakPassword,
        ),
        onNewPasswordFieldChange = {},
        onConfirmPasswordFieldChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}
