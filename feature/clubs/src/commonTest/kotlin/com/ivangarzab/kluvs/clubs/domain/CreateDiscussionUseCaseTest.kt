package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateDiscussionUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = CreateDiscussionUseCase(sessionRepository)

    private val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
    private val existingDiscussion = Discussion(
        id = "d1",
        sessionId = "session-1",
        title = "Ch 1",
        date = LocalDateTime(2026, 3, 1, 19, 0)
    )
    private val stubSession = Session(
        id = "session-1",
        clubId = "club-1",
        book = book,
        dueDate = null,
        discussions = listOf(existingDiscussion)
    )
    private val newDiscussionDate = LocalDateTime(2026, 4, 1, 19, 0)
    private val params = CreateDiscussionUseCase.Params(
        sessionId = "session-1",
        title = "Ch 2",
        location = "Discord",
        date = newDiscussionDate
    )
    // Expected discussions list after appending the new discussion
    private val expectedNewDiscussion = Discussion(
        id = "",
        sessionId = "session-1",
        title = "Ch 2",
        location = "Discord",
        date = newDiscussionDate
    )
    private val expectedDiscussions = listOf(existingDiscussion, expectedNewDiscussion)

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns Result.success(stubSession)
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", discussions = expectedDiscussions) } returns Result.success(stubSession)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke succeeds when user is ADMIN`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns Result.success(stubSession)
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", discussions = expectedDiscussions) } returns
            Result.success(stubSession)

        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fetches session before updating`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns Result.success(stubSession)
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", discussions = expectedDiscussions) } returns
            Result.success(stubSession)

        useCase(params, Role.OWNER)

        verifySuspend { sessionRepository.getSession("session-1") }
    }

    @Test
    fun `invoke propagates fetch failure without calling update`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
