package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearAttendanceUseCaseTest {

    private val discussionAttendanceRepository = mock<DiscussionAttendanceRepository>()
    private val useCase = ClearAttendanceUseCase(discussionAttendanceRepository)

    @Test
    fun `invoke succeeds`() = runTest {
        everySuspend { discussionAttendanceRepository.clearAttendance("d1") } returns Result.success(Unit)

        val result = useCase("d1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke propagates failure`() = runTest {
        everySuspend { discussionAttendanceRepository.clearAttendance("d1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase("d1")

        assertTrue(result.isFailure)
    }
}
