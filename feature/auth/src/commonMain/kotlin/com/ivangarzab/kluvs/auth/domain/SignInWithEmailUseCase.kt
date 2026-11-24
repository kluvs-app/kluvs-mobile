package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User

/**
 * Signs in with email and password.
 *
 * @param authRepository Repository for auth state and operations
 */
class SignInWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> = authRepository.signInWithEmail(email, password)
}
