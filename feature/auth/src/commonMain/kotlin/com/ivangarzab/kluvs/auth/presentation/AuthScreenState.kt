package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.model.User

/** State of the Sign in and Sign up screens. */
data class AuthScreenState(
    val authState: AuthState = AuthState.Unauthenticated,
    val emailField: String = "",
    val passwordField: String = "",
    val confirmPasswordField: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)

/** Where the user is in signing in. Failures aren't a state: they're danger snackbars. */
sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    /** Email sign up succeeded but requires email confirmation before a session is created */
    data class EmailConfirmationPending(val email: String) : AuthState()
}

enum class AuthMode { LOGIN, SIGNUP }
