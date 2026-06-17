package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for creating a new reading session for a club.
 *
 * Requires [Role.OWNER] authorization.
 */
class CreateSessionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<CreateSessionUseCase.Params, Session>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(
        val clubId: String,
        val book: Book,
        val dueDate: LocalDateTime?
    )

    override suspend fun execute(params: Params): Result<Session> {
        Bark.d("Creating session for club (Club ID: ${params.clubId}, Book: ${params.book.title})")
        return sessionRepository.createSession(
            clubId = params.clubId,
            book = params.book,
            dueDate = params.dueDate
        )
            .onSuccess { Bark.i("Session created (Club ID: ${params.clubId}, Session ID: ${it.id})") }
            .onFailure { Bark.e("Failed to create session (Club ID: ${params.clubId}). Retry.", it) }
    }
}
