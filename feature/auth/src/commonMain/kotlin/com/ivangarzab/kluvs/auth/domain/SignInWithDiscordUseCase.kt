package com.ivangarzab.kluvs.auth.domain


/**
 * Starts the Discord OAuth flow, returning the URL to open in a browser.
 *
 * @param authRepository Repository for auth state and operations
 */
class SignInWithDiscordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<String> = authRepository.signInWithDiscord()
}
