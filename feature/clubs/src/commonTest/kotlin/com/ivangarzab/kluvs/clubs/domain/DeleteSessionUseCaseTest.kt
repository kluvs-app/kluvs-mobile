package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeleteSessionUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = DeleteSessionUseCase(sessionRepository)

    private val params = DeleteSessionUseCase.Params(sessionId = "session-1")

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { sessionRepository.deleteSession(sessionId = "session-1") } returns Result.success("deleted")

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
        everySuspend { sessionRepository.deleteSession(sessionId = "session-1") } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
