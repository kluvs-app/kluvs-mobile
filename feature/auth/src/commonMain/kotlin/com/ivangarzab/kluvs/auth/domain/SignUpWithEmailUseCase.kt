package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User

/**
 * Creates an account with email and password. Fails with [AuthError.EmailConfirmationRequired] when the account must be confirmed by email before a session exists.
 *
 * @param authRepository Repository for auth state and operations
 */
class SignUpWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> = authRepository.signUpWithEmail(email, password)
}
