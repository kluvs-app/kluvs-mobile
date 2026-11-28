package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper

/** iOS bridge to [AuthViewModel]. */
@Suppress("unused")
class AuthViewModelHelper : BaseViewModelHelper<AuthScreenState>() {

    override val viewModel: AuthViewModel by viewModel()

    fun onEmailFieldChanged(value: String) = viewModel.onEmailFieldChanged(value)

    fun onPasswordFieldChanged(value: String) = viewModel.onPasswordFieldChanged(value)

    fun onConfirmPasswordFieldChanged(value: String) = viewModel.onConfirmPasswordFieldChanged(value)

    fun validateAndSignIn() = viewModel.validateAndSignIn()

    fun validateAndSignUp() = viewModel.validateAndSignUp()

    fun signInWithDiscord() = viewModel.signInWithDiscord()

    fun signInWithGoogle() = viewModel.signInWithGoogle()

    fun handleOAuthCallback(callbackUrl: String) = viewModel.handleOAuthCallback(callbackUrl)

    fun signInWithApple(idToken: String) = viewModel.signInWithApple(idToken)

    fun dismissEmailConfirmationSheet() = viewModel.dismissEmailConfirmationSheet()
}
