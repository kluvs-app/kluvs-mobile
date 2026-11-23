package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User

/**
 * Signs in with the identity token from native Sign in with Apple (iOS).
 *
 * @param authRepository Repository for auth state and operations
 */
class SignInWithAppleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> = authRepository.signInWithAppleNative(idToken)
}
