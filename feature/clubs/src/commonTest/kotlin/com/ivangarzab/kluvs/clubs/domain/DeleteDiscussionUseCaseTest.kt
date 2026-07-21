package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeleteDiscussionUseCaseTest {

    private val discussionRepository = mock<DiscussionRepository>()
    private val useCase = DeleteDiscussionUseCase(discussionRepository)

    private val params = DeleteDiscussionUseCase.Params(discussionId = "d1")

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { discussionRepository.deleteDiscussion("d1") } returns Result.success(Unit)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke succeeds when user is ADMIN`() = runTest {
        everySuspend { discussionRepository.deleteDiscussion("d1") } returns Result.success(Unit)

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
        everySuspend { discussionRepository.deleteDiscussion("d1") } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
