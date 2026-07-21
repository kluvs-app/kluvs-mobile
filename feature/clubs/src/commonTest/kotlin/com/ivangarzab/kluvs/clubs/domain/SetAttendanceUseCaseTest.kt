package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.model.AttendanceStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetAttendanceUseCaseTest {

    private val discussionAttendanceRepository = mock<DiscussionAttendanceRepository>()
    private val useCase = SetAttendanceUseCase(discussionAttendanceRepository)

    @Test
    fun `invoke returns saved status on success`() = runTest {
        everySuspend { discussionAttendanceRepository.setAttendance("d1", AttendanceStatus.MAYBE) } returns
            Result.success(AttendanceStatus.MAYBE)

        val result = useCase(SetAttendanceUseCase.Params("d1", AttendanceStatus.MAYBE))

        assertTrue(result.isSuccess)
        assertEquals(AttendanceStatus.MAYBE, result.getOrNull())
    }

    @Test
    fun `invoke propagates failure`() = runTest {
        everySuspend { discussionAttendanceRepository.setAttendance("d1", AttendanceStatus.NO) } returns
            Result.failure(RuntimeException("Network error"))

        val result = useCase(SetAttendanceUseCase.Params("d1", AttendanceStatus.NO))

        assertTrue(result.isFailure)
    }
}
