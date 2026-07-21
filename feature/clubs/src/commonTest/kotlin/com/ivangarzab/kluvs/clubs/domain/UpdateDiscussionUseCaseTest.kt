package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateDiscussionUseCaseTest {

    private val discussionRepository = mock<DiscussionRepository>()
    private val useCase = UpdateDiscussionUseCase(discussionRepository)

    private val params = UpdateDiscussionUseCase.Params(
        discussionId = "d1",
        title = "New Title",
        location = null,
        date = null
    )
    private val updatedDiscussion = Discussion(
        id = "d1",
        sessionId = "session-1",
        title = "New Title",
        location = "Library",
        date = kotlinx.datetime.LocalDateTime(2026, 3, 1, 19, 0)
    )

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend {
            discussionRepository.updateDiscussion("d1", "New Title", null, null)
        } returns Result.success(updatedDiscussion)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke succeeds when user is ADMIN`() = runTest {
        everySuspend {
            discussionRepository.updateDiscussion("d1", "New Title", null, null)
        } returns Result.success(updatedDiscussion)

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
    fun `invoke propagates repository failure`() = runTest {
        everySuspend {
            discussionRepository.updateDiscussion("d1", "New Title", null, null)
        } returns Result.failure(RuntimeException("Not found"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
