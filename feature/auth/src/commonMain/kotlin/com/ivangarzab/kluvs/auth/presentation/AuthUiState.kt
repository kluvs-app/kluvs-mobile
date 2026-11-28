package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError

data class AuthUiState(
    val emailField: String = "",
    val passwordField: String = "",
    val confirmPasswordField: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    /** The bottom sheet on screen, if any. Opened with `onOpenSheet`, closed by `onDismissSheet`. */
    val activeSheet: AuthSheet? = null,
)

/** Bottom sheets on the Login and Sign Up screens. */
sealed class AuthSheet {
    data object ForgotPassword : AuthSheet()
}

enum class AuthMode { LOGIN, SIGNUP }

data class ForgotPasswordUiState(
    val emailField: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val generalError: AuthError? = null,
)
