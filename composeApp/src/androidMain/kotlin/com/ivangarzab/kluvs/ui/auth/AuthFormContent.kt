package com.ivangarzab.kluvs.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.auth.presentation.AuthMode
import com.ivangarzab.kluvs.auth.presentation.AuthUiState
import com.ivangarzab.kluvs.auth.presentation.LoginNavigation
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.contentDarkPrimary
import com.ivangarzab.kluvs.theme.providerDiscordBg
import com.ivangarzab.kluvs.theme.providerGoogleBg
import com.ivangarzab.kluvs.theme.providerGoogleText
import com.ivangarzab.kluvs.ui.components.InputField
import com.ivangarzab.kluvs.ui.components.SocialButton
import com.ivangarzab.kluvs.ui.components.TextDivider

@Composable
fun AuthFormContent(
    modifier: Modifier = Modifier,
    mode: AuthMode,
    state: AuthUiState,
    errorMessage: String? = null,
    onEmailFieldChange: (String) -> Unit,
    onPasswordFieldChange: (String) -> Unit,
    onConfirmPasswordFieldChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOAuthSignIn: (AuthProvider) -> Unit,
    onNavigate: (LoginNavigation) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    actionColor = MaterialTheme.colorScheme.error
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Welcome to your Kluvs",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(
                    if (mode == AuthMode.LOGIN) {
                        R.string.sign_in_to_your_account
                    } else {
                        R.string.create_a_new_account
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SocialButton(
                text = stringResource(R.string.continue_with_discord),
                icon = painterResource(R.drawable.ic_discord),
                iconSize = 20.dp,
                backgroundColor = providerDiscordBg,
                textColor = contentDarkPrimary,
                onClick = { onOAuthSignIn(AuthProvider.DISCORD) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SocialButton(
                text = stringResource(R.string.continue_with_google),
                icon = painterResource(R.drawable.ic_google),
                iconSize = 20.dp,
                backgroundColor = providerGoogleBg,
                textColor = providerGoogleText,
                onClick = { onOAuthSignIn(AuthProvider.GOOGLE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextDivider(text = stringResource(R.string.or_continue_with_email))

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.email),
                value = state.emailField,
                onValueChange = onEmailFieldChange,
                iconRes = R.drawable.ic_email,
                iconDescription = stringResource(R.string.description_email_text_field),
                supportingText = state.emailError
                    ?: stringResource(R.string.enter_valid_email_address),
                supportingTextColor = if (state.emailError != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color.Gray
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                label = stringResource(R.string.password),
                value = state.passwordField,
                onValueChange = onPasswordFieldChange,
                iconRes = R.drawable.ic_password,
                iconDescription = stringResource(R.string.description_password_text_field),
                supportingText = state.passwordError
                    ?: stringResource(R.string.min_eight_characters),
                supportingTextColor = if (state.emailError != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color.Gray
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mode == AuthMode.LOGIN) {
                        ImeAction.Go
                    } else {
                        ImeAction.Next
                    }
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onSubmit()}
                ),
            )

            if (mode == AuthMode.SIGNUP) {
                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    isPassword = true,
                    label = stringResource(R.string.confirm_password),
                    value = state.confirmPasswordField,
                    onValueChange = onConfirmPasswordFieldChange,
                    iconRes = R.drawable.ic_password,
                    iconDescription = stringResource(R.string.description_confirm_password_text_field),
                    supportingText = state.confirmPasswordError
                        ?: stringResource(R.string.match_password_above),
                    supportingTextColor = if (state.emailError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Gray
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { onSubmit() }
                    ),
                )
            }

            if (mode == AuthMode.LOGIN) {
                TextButton(
                    modifier = Modifier
                        .align(Alignment.End),
                    onClick = { onNavigate(LoginNavigation.ForgetPassword) },
                    content = {
                        Text(
                            text = stringResource(R.string.forgot_password),
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = { onSubmit() }
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) {
                        stringResource(R.string.sign_in)
                    } else {
                        stringResource(R.string.sign_up)
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.background
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) {
                        stringResource(R.string.dont_have_an_account)
                    } else {
                        stringResource(R.string.already_have_an_account)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onNavigate(
                                    if (mode == AuthMode.LOGIN) {
                                        LoginNavigation.SignUp
                                    } else {
                                        LoginNavigation.SignIn
                                    }
                                )
                            }
                        ),
                    text = if (mode == AuthMode.LOGIN) {
                        stringResource(R.string.sign_up)
                    } else {
                        stringResource(R.string.sign_in)
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_LoginScreen() = KluvsTheme {
    AuthFormContent(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        mode = AuthMode.LOGIN,
        state = AuthUiState(),
        onEmailFieldChange = { _ -> },
        onPasswordFieldChange = { _ -> },
        onConfirmPasswordFieldChange = { _ -> },
        onOAuthSignIn = { _ -> },
        onSubmit = { },
        onNavigate = { _ -> },
    )
}

@PreviewLightDark
@Composable
fun Preview_SignupScreen() = KluvsTheme {
    AuthFormContent(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        mode = AuthMode.SIGNUP,
        state = AuthUiState(),
        onEmailFieldChange = { _ -> },
        onPasswordFieldChange = { _ -> },
        onConfirmPasswordFieldChange = { _ -> },
        onOAuthSignIn = { _ -> },
        onSubmit = { },
        onNavigate = { _ -> },
    )
}