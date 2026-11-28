package com.ivangarzab.kluvs.ui.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ivangarzab.kluvs.auth.presentation.AuthMode
import com.ivangarzab.kluvs.auth.presentation.AuthState
import com.ivangarzab.kluvs.auth.presentation.AuthViewModel
import com.ivangarzab.kluvs.auth.presentation.LoginNavigation
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.ui.OAuthCallbackHandler
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToLogIn: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    // Listens outside the Loading switch: a failure arrives right as the form comes back, and an
    // effect sent with no listener is dropped. The form's SnackbarHost shows it once composed.
    val snackbarHostState = remember { SnackbarHostState() }
    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    (state.authState as? AuthState.EmailConfirmationPending)?.let { pending ->
        AuthSignUpConfirmationSheet(
            email = pending.email,
            onDismiss = viewModel::dismissEmailConfirmationSheet,
        )
    }

    // Collect OAuth callbacks from MainActivity
    LaunchedEffect(Unit) {
        OAuthCallbackHandler.callbacks.collect { callbackUrl ->
            viewModel.handleOAuthCallback(callbackUrl)
        }
    }

    when (state.authState) {
        is AuthState.Loading -> LoadingScreen()
        is AuthState.Authenticated -> { /* No-op */ }
        is AuthState.Unauthenticated,
        is AuthState.EmailConfirmationPending -> {
            AuthFormContent(
                modifier = modifier,
                mode = AuthMode.SIGNUP,
                state = state,
                snackbarHostState = snackbarHostState,
                onEmailFieldChange = viewModel::onEmailFieldChanged,
                onPasswordFieldChange = viewModel::onPasswordFieldChanged,
                onConfirmPasswordFieldChange = viewModel::onConfirmPasswordFieldChanged,
                onSubmit = viewModel::validateAndSignUp,
                onOAuthSignIn = { provider ->
                    when (provider) {
                        AuthProvider.DISCORD -> viewModel.signInWithDiscord()
                        AuthProvider.GOOGLE -> viewModel.signInWithGoogle()
                        else -> { /* Apple handled natively on iOS only */ }
                    }
                },
                onNavigate = {
                    when (it) {
                        LoginNavigation.SignIn -> onNavigateToLogIn()
                        // Sign up shows no "Forgot password?" link
                        else -> {}
                    }
                }
            )
        }
    }
}
