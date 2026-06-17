package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for updating an existing reading session's book or due date.
 *
 * Requires [Role.OWNER] authorization.
 */
class UpdateSessionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<UpdateSessionUseCase.Params, Session>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(
        val sessionId: String,
        val book: Book?,
        val dueDate: LocalDateTime?
    )

    override suspend fun execute(params: Params): Result<Session> {
        Bark.d("Updating session (Session ID: ${params.sessionId})")
        return sessionRepository.updateSession(
            sessionId = params.sessionId,
            book = params.book,
            dueDate = params.dueDate
        )
            .onSuccess { Bark.i("Session updated (Session ID: ${params.sessionId})") }
            .onFailure { Bark.e("Failed to update session (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
