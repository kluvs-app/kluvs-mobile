package com.ivangarzab.kluvs.auth.presentation

import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.ResetPasswordUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * Drives the Forgot Password screen: one email field that sends a password reset link.
 * A failed send shows a danger snackbar and leaves the form as it was, so the user can retry.
 */
class ForgotPasswordViewModel(
    private val validateCredentials: ValidateCredentialsUseCase,
    private val resetPassword: ResetPasswordUseCase,
) : BaseViewModel<ForgotPasswordState>(ForgotPasswordState()) {

    fun onEmailFieldChanged(value: String) {
        updateState { it.copy(emailField = value, emailError = null) }
    }

    fun onSendResetLink() {
        if (currentState.isLoading) return  // A send is already running

        val emailError = validateCredentials(email = currentState.emailField).email
        if (emailError != null) {
            updateState { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            Bark.d("Password reset email requested")
            updateState { it.copy(isLoading = true) }
            resetPassword(currentState.emailField)
                .onSuccess {
                    Bark.i("Password reset email sent")
                    updateState { it.copy(isLoading = false, isEmailSent = true) }
                }
                .onFailure { error ->
                    Bark.e("Password reset email failed to send. User may need to retry.", error)
                    updateState { it.copy(isLoading = false) }
                    send(error.toAuthErrorSnackbar())
                }
        }
    }
}
