package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Discussion
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

class UpdateDiscussionUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = UpdateDiscussionUseCase(sessionRepository)

    private val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
    private val existingDate = LocalDateTime(2026, 3, 1, 19, 0)
    private val existingDiscussion = Discussion(
        id = "d1",
        sessionId = "session-1",
        title = "Old Title",
        location = "Library",
        date = existingDate
    )
    private val stubSession = Session(
        id = "session-1",
        clubId = "club-1",
        book = book,
        dueDate = null,
        discussions = listOf(existingDiscussion)
    )
    private val params = UpdateDiscussionUseCase.Params(
        sessionId = "session-1",
        discussionId = "d1",
        title = "New Title",
        location = null,
        date = null
    )
    // After update: title changes, location and date stay the same
    private val expectedUpdatedDiscussion = existingDiscussion.copy(title = "New Title")
    private val expectedDiscussions = listOf(expectedUpdatedDiscussion)

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns Result.success(stubSession)
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", discussions = expectedDiscussions) } returns
            Result.success(stubSession)

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
    fun `invoke applies partial updates preserving unchanged fields`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns Result.success(stubSession)
        everySuspend { sessionRepository.updateSession(sessionId = "session-1", discussions = expectedDiscussions) } returns
            Result.success(stubSession)

        // Only title changes; location and date remain as-is
        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke propagates fetch failure`() = runTest {
        everySuspend { sessionRepository.getSession("session-1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
