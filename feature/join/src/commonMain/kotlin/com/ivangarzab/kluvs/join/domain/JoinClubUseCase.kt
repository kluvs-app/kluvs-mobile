package com.ivangarzab.kluvs.join.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.JoinRepository

/**
 * UseCase for joining the club behind an invite token.
 *
 * @param joinRepository Repository for the invite-link join flow
 */
class JoinClubUseCase(
    private val joinRepository: JoinRepository
) {
    suspend operator fun invoke(token: String): Result<String> {
        Bark.d("Joining club via use case")
        return joinRepository.joinClub(token)
    }
}
