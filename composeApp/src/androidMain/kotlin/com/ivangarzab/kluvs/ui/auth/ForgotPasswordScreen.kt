package com.ivangarzab.kluvs.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.auth.presentation.ForgotPasswordState
import com.ivangarzab.kluvs.auth.presentation.ForgotPasswordViewModel
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import com.ivangarzab.kluvs.designsystem.components.appbars.TopAppBar
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.components.buttons.TextButton
import com.ivangarzab.kluvs.designsystem.components.fields.InputField
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
import org.koin.androidx.compose.koinViewModel

/**
 * Sends a password reset link. Reached from "Forgot password?" on Sign in; a quick detour, so it
 * reuses the auth form's masthead and field but skips the footer. Failures show as snackbars.
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    ForgotPasswordContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailFieldChange = viewModel::onEmailFieldChanged,
        onSubmit = viewModel::onSendResetLink,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordState,
    snackbarHostState: SnackbarHostState,
    onEmailFieldChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            // Back only: the masthead below carries the screen's headline
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                header = "",
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, snackbar = { KluvsSnackbar(it) }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Fade between the form and the sent state, like the tab screens' content swaps
            AnimatedContent(
                targetState = state.isEmailSent,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ForgotPasswordTransition",
            ) { isEmailSent ->
                Column {
                    AuthMasthead(
                        voicePhrase = stringResource(
                            if (isEmailSent) R.string.check_your_inbox else R.string.forgot_it
                        ),
                        subhead = if (isEmailSent) null else stringResource(R.string.reset_password_subhead),
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isEmailSent) {
                        ResetLinkSentCard(email = state.emailField)

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.back_to_sign_in),
                            onClick = onNavigateBack,
                            emphasized = true,
                        )
                    } else {
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

                        Spacer(modifier = Modifier.height(16.dp))

                        PrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(if (state.isLoading) R.string.sending else R.string.send_reset_link),
                            onClick = onSubmit,
                            enabled = !state.isLoading,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetLinkSentCard(email: String) {
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
                text = email,
                color = KluvsTheme.colors.content,
                fontWeight = FontWeight.Medium,
                style = KluvsTheme.typography.body.large,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.reset_link_sent_body),
                color = KluvsTheme.colors.contentMuted,
                style = KluvsTheme.typography.body.medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ForgotPassword() = KluvsTheme {
    var state by remember { mutableStateOf(ForgotPasswordState()) }
    ForgotPasswordContent(
        state = state,
        snackbarHostState = remember { SnackbarHostState() },
        onEmailFieldChange = { state = state.copy(emailField = it, emailError = null) },
        onSubmit = { state = state.copy(isEmailSent = true) },
        onNavigateBack = { state = ForgotPasswordState() },
    )
}

@PreviewLightDark
@Composable
private fun Preview_ForgotPassword_EmailSent() = KluvsTheme {
    var state by remember {
        mutableStateOf(ForgotPasswordState(emailField = "test@example.com", isEmailSent = true))
    }
    ForgotPasswordContent(
        state = state,
        snackbarHostState = remember { SnackbarHostState() },
        onEmailFieldChange = { state = state.copy(emailField = it) },
        onSubmit = {},
        onNavigateBack = { state = ForgotPasswordState() },
    )
}
