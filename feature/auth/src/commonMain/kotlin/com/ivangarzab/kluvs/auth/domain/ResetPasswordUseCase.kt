package com.ivangarzab.kluvs.auth.domain


/**
 * Sends a password reset email.
 *
 * @param authRepository Repository for auth state and operations
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> = authRepository.resetPasswordForEmail(email)
}
