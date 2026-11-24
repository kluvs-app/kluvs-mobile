package com.ivangarzab.kluvs.join.domain

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes whether a user is signed in, so the join screen knows to route through
 * auth before joining.
 *
 * @param authRepository Repository for auth state
 */
class ObserveIsAuthenticatedUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<Boolean> = authRepository.isAuthenticated
}
