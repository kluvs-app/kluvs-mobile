package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [com.ivangarzab.kluvs.auth.presentation.AuthViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class AuthViewModelHelper : KoinComponent {

    private val viewModel: AuthViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method for authentication state.
     *
     * Returns a [com.ivangarzab.kluvs.presentation.Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (AuthState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    /**
     * iOS-friendly observation method for UI state (form fields).
     *
     * Returns a [com.ivangarzab.kluvs.presentation.Closeable] that can be used to cancel the observation.
     */
    fun observeUiState(callback: (AuthUiState) -> Unit): Closeable {
        val job = viewModel.uiState.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun onEmailFieldChanged(value: String) = viewModel.onEmailFieldChanged(value)

    fun onPasswordFieldChanged(value: String) = viewModel.onPasswordFieldChanged(value)

    fun onConfirmPasswordFieldChanged(value: String) = viewModel.onConfirmPasswordFieldChanged(value)

    /**
     * iOS-friendly observation method for forgot-password UI state.
     *
     * Returns a [com.ivangarzab.kluvs.presentation.Closeable] that can be used to cancel the observation.
     */
    fun observeForgotPasswordState(callback: (ForgotPasswordUiState) -> Unit): Closeable {
        val job = viewModel.forgotPasswordState.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun onForgotPasswordEmailChanged(value: String) = viewModel.onForgotPasswordEmailChanged(value)

    fun sendPasswordResetEmail() = viewModel.sendPasswordResetEmail()

    fun resetForgotPasswordState() = viewModel.resetForgotPasswordState()

    fun validateAndSignIn() = viewModel.validateAndSignIn()

    fun validateAndSignUp() = viewModel.validateAndSignUp()

    fun signOut() = viewModel.signOut()

    fun signInWithDiscord() = viewModel.signInWithDiscord()

    fun signInWithGoogle() = viewModel.signInWithGoogle()

    fun handleOAuthCallback(callbackUrl: String) = viewModel.handleOAuthCallback(callbackUrl)

    fun onOAuthUrlLaunched() = viewModel.onOAuthUrlLaunched()

    fun signInWithApple(idToken: String) = viewModel.signInWithApple(idToken)
}