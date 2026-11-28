package com.ivangarzab.kluvs.auth.presentation

/** State of the Forgot Password screen. */
data class ForgotPasswordState(
    val emailField: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    /** The reset link went out: the screen swaps the form for the "check your inbox" card. */
    val isEmailSent: Boolean = false,
)
