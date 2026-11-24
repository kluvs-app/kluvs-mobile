package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User

/**
 * Completes an OAuth sign-in from the redirect deep link.
 *
 * @param authRepository Repository for auth state and operations
 */
class HandleOAuthCallbackUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(callbackUrl: String): Result<User> = authRepository.handleOAuthCallback(callbackUrl)
}
