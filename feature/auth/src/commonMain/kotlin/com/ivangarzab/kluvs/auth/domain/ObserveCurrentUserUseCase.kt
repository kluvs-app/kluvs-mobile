package com.ivangarzab.kluvs.auth.domain

import com.ivangarzab.kluvs.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the signed-in [User], or null when signed out.
 *
 * @param authRepository Repository for auth state and operations
 */
class ObserveCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<User?> = authRepository.currentUser
}
