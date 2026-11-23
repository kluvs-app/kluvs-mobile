package com.ivangarzab.kluvs.auth.domain

/**
 * Per-field validation errors for the auth forms. A null field passed validation.
 */
data class CredentialErrors(
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
) {
    val hasErrors: Boolean get() = email != null || password != null || confirmPassword != null
}

/**
 * Validates auth form input before any network call: sign in (email + password),
 * sign up (email + password + confirmation) and password reset (email only).
 *
 * Pass only the fields the form has; a null [password] or [confirmPassword] skips that check.
 */
class ValidateCredentialsUseCase {

    operator fun invoke(
        email: String,
        password: String? = null,
        confirmPassword: String? = null,
    ): CredentialErrors = CredentialErrors(
        email = when {
            email.isBlank() -> "Email is required"
            !EMAIL_REGEX.matches(email) -> "Please enter a valid email"
            else -> null
        },
        password = password?.let {
            when {
                it.isBlank() -> "Password is required"
                it.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
                else -> null
            }
        },
        confirmPassword = confirmPassword?.let {
            if (it != password) "Passwords must match" else null
        },
    )

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        const val MIN_PASSWORD_LENGTH = 6
    }
}
