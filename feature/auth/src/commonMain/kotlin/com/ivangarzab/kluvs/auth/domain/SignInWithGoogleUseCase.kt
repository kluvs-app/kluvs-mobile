package com.ivangarzab.kluvs.auth.domain


/**
 * Starts the Google OAuth flow, returning the URL to open in a browser.
 *
 * @param authRepository Repository for auth state and operations
 */
class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<String> = authRepository.signInWithGoogle()
}
