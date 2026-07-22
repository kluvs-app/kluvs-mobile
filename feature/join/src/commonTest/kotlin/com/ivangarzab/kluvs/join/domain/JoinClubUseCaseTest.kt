package com.ivangarzab.kluvs.join.domain

import com.ivangarzab.kluvs.data.repositories.JoinRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JoinClubUseCaseTest {

    private lateinit var joinRepository: JoinRepository
    private lateinit var useCase: JoinClubUseCase

    @BeforeTest
    fun setup() {
        joinRepository = mock<JoinRepository>()
        useCase = JoinClubUseCase(joinRepository)
    }

    @Test
    fun `invoke returns joined club id on success`() = runTest {
        everySuspend { joinRepository.joinClub("token-1") } returns Result.success("club-1")

        val result = useCase("token-1")

        assertTrue(result.isSuccess)
        assertEquals("club-1", result.getOrNull())
        verifySuspend { joinRepository.joinClub("token-1") }
    }

    @Test
    fun `invoke returns failure for invalid token`() = runTest {
        val exception = Exception("Invalid or expired token")
        everySuspend { joinRepository.joinClub("bad-token") } returns Result.failure(exception)

        val result = useCase("bad-token")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
