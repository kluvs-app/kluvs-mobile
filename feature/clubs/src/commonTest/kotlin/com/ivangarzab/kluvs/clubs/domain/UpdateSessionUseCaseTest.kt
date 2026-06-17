package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateSessionUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = UpdateSessionUseCase(sessionRepository)

    private val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
    private val dueDate = LocalDateTime(2026, 6, 1, 0, 0)
    private val params = UpdateSessionUseCase.Params(sessionId = "session-1", book = book, dueDate = dueDate)
    private val stubSession = Session(id = "session-1", clubId = "club-1", book = book, dueDate = dueDate, discussions = emptyList())

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", book = book, dueDate = dueDate) } returns
            Result.success(stubSession)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is ADMIN`() = runTest {
        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", book = book, dueDate = dueDate) } returns
            Result.failure(RuntimeException("Network error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
