package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.ReadingLog

/**
 * UseCase for fetching the signed-in member's reading log — every session
 * they're part of, grouped into active vs. finished. Mirrors web's
 * ReadingLogModal (`GET session?reading_log=true`).
 *
 * @param sessionRepository Repository for session data
 */
class GetReadingLogUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Result<ReadingLog> {
        Bark.d("Fetching reading log")
        return sessionRepository.getReadingLog()
            .onSuccess { Bark.i("Loaded reading log (Active: ${it.active.size}, Finished: ${it.finished.size})") }
            .onFailure { error -> Bark.e("Failed to fetch reading log. User will see an empty log.", error) }
    }
}
