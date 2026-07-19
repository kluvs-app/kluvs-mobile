package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError

data class AuthUiState(
    val emailField: String = "",
    val passwordField: String = "",
    val confirmPasswordField: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)

enum class AuthMode { LOGIN, SIGNUP }

data class ForgotPasswordUiState(
    val emailField: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val generalError: AuthError? = null,
)
