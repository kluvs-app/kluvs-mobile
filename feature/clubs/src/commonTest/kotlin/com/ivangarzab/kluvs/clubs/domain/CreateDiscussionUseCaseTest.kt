package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
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

    private val discussionRepository = mock<DiscussionRepository>()
    private val useCase = CreateDiscussionUseCase(discussionRepository)

    private val newDiscussionDate = LocalDateTime(2026, 4, 1, 19, 0)
    private val params = CreateDiscussionUseCase.Params(
        sessionId = "session-1",
        title = "Ch 2",
        location = "Discord",
        date = newDiscussionDate
    )
    private val createdDiscussion = Discussion(
        id = "d2",
        sessionId = "session-1",
        title = "Ch 2",
        location = "Discord",
        date = newDiscussionDate
    )

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend {
            discussionRepository.createDiscussion("session-1", "Ch 2", newDiscussionDate, "Discord")
        } returns Result.success(createdDiscussion)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke succeeds when user is ADMIN`() = runTest {
        everySuspend {
            discussionRepository.createDiscussion("session-1", "Ch 2", newDiscussionDate, "Discord")
        } returns Result.success(createdDiscussion)

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
    fun `invoke calls repository with mapped params`() = runTest {
        everySuspend {
            discussionRepository.createDiscussion("session-1", "Ch 2", newDiscussionDate, "Discord")
        } returns Result.success(createdDiscussion)

        useCase(params, Role.OWNER)

        verifySuspend { discussionRepository.createDiscussion("session-1", "Ch 2", newDiscussionDate, "Discord") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend {
            discussionRepository.createDiscussion("session-1", "Ch 2", newDiscussionDate, "Discord")
        } returns Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
