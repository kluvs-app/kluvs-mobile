package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User

/**
 * Restores the saved auth session on app start. A failed restore keeps the saved session (see `AuthRepositoryImpl.initialize()`).
 *
 * @param authRepository Repository for auth state and operations
 */
class InitializeAuthUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> = authRepository.initialize()
}
