package com.ivangarzab.kluvs.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ivangarzab.kluvs.auth.presentation.AuthMode
import com.ivangarzab.kluvs.auth.presentation.AuthState
import com.ivangarzab.kluvs.auth.presentation.AuthViewModel
import com.ivangarzab.kluvs.auth.presentation.LoginNavigation
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.ui.OAuthCallbackHandler
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.ui.extensions.toLocalizedMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Collect OAuth callbacks from MainActivity
    LaunchedEffect(Unit) {
        OAuthCallbackHandler.callbacks.collect { callbackUrl ->
            viewModel.handleOAuthCallback(callbackUrl)
        }
    }

    when (state) {
        is AuthState.Loading -> LoadingScreen()
        is AuthState.Authenticated -> { /* No-op */ }
        is AuthState.Unauthenticated,
        is AuthState.EmailConfirmationPending,
        is AuthState.Error -> {
            AuthFormContent(
                modifier = modifier,
                mode = AuthMode.LOGIN,
                state = uiState,
                errorMessage = (state as? AuthState.Error)?.error?.toLocalizedMessage(),
                onEmailFieldChange = viewModel::onEmailFieldChanged,
                onPasswordFieldChange = viewModel::onPasswordFieldChanged,
                onConfirmPasswordFieldChange = viewModel::onConfirmPasswordFieldChanged,
                onSubmit = viewModel::validateAndSignIn,
                onOAuthSignIn = { provider ->
                    when (provider) {
                        AuthProvider.DISCORD -> viewModel.signInWithDiscord()
                        AuthProvider.GOOGLE -> viewModel.signInWithGoogle()
                        else -> { /* Apple handled natively on iOS only */ }
                    }
                },
                onNavigate = {
                    when (it) {
                        LoginNavigation.SignUp -> onNavigateToSignUp()
                        LoginNavigation.ForgetPassword -> onNavigateToForgotPassword()
                        LoginNavigation.SignIn -> { /* Already here */ }
                    }
                }
            )
        }
    }
}