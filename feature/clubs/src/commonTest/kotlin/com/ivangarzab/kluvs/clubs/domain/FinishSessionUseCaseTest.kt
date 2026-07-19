package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FinishSessionUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = FinishSessionUseCase(sessionRepository)

    private val params = FinishSessionUseCase.Params(sessionId = "session-1")

    @Test
    fun `invoke succeeds when user is OWNER and returns credited count`() = runTest {
        everySuspend { sessionRepository.finishSession(sessionId = "session-1") } returns Result.success(3)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
    }

    @Test
    fun `invoke succeeds when user is ADMIN`() = runTest {
        everySuspend { sessionRepository.finishSession(sessionId = "session-1") } returns Result.success(0)

        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { sessionRepository.finishSession(sessionId = "session-1") } returns
            Result.failure(RuntimeException("Session already finished"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
