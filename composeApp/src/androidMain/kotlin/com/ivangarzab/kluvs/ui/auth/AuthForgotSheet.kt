package com.ivangarzab.kluvs.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.auth.presentation.AuthViewModel
import com.ivangarzab.kluvs.auth.presentation.ForgotPasswordUiState
import com.ivangarzab.kluvs.designsystem.components.fields.InputField
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheet
import com.ivangarzab.kluvs.designsystem.components.modals.BottomSheetFooter
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.extensions.toLocalizedMessage
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Forgot it?" as a bottom sheet rather than a full screen — reset is a quick, single-field
 * detour off Sign in / Sign up, not a destination worth its own masthead/footer chrome. Extends
 * the existing [AuthViewModel] rather than a dedicated sheet view model, per the shared-VM
 * pattern used elsewhere for stateless sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthForgotSheet(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.forgotPasswordState.collectAsState()

    AuthForgotSheetContent(
        state = state,
        onEmailFieldChange = viewModel::onForgotPasswordEmailChanged,
        onSubmit = viewModel::sendPasswordResetEmail,
        onDismiss = viewModel::onDismissSheet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthForgotSheetContent(
    state: ForgotPasswordUiState,
    onEmailFieldChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val errorMessage = state.generalError?.toLocalizedMessage()

    BottomSheet(
        header = stringResource(if (state.isEmailSent) R.string.check_your_inbox else R.string.forgot_it),
        onDismiss = onDismiss,
        footer = if (!state.isEmailSent) {
            {
                BottomSheetFooter(
                    actionLabel = if (state.isLoading) {
                        stringResource(R.string.sending)
                    } else {
                        stringResource(R.string.send_reset_link)
                    },
                    onAction = onSubmit,
                    onCancel = onDismiss,
                )
            }
        } else {
            null
        },
    ) {
        if (!state.isEmailSent) {
            Text(
                text = stringResource(R.string.reset_password_subhead),
                color = KluvsTheme.colors.contentMuted,
                style = KluvsTheme.typography.body.medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.email),
                value = state.emailField,
                onValueChange = onEmailFieldChange,
                error = state.emailError,
                helperText = if (state.emailError == null) {
                    stringResource(R.string.enter_valid_email_address)
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
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
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = KluvsTheme.colors.cardAlt
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.reset_link_sent_to),
                        color = KluvsTheme.colors.contentMuted,
                        style = KluvsTheme.typography.label,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.emailField,
                        color = KluvsTheme.colors.content,
                        fontWeight = FontWeight.Medium,
                        style = KluvsTheme.typography.body.large,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.reset_link_sent_body),
                        color = KluvsTheme.colors.contentMuted,
                        style = KluvsTheme.typography.body.medium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview_AuthForgotSheet() = KluvsTheme {
    AuthForgotSheetContent(
        state = ForgotPasswordUiState(),
        onEmailFieldChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview_AuthForgotSheet_EmailSent() = KluvsTheme {
    AuthForgotSheetContent(
        state = ForgotPasswordUiState(
            emailField = "test@example.com",
            isEmailSent = true,
        ),
        onEmailFieldChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}
