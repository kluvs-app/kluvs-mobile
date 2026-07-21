package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.model.AttendanceResponse
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAttendanceRosterUseCaseTest {

    private val discussionAttendanceRepository = mock<DiscussionAttendanceRepository>()
    private val useCase = GetAttendanceRosterUseCase(discussionAttendanceRepository)

    private val stubRoster = AttendanceRoster(
        responses = listOf(AttendanceResponse(memberId = "m1", name = "Ivan", status = AttendanceStatus.YES)),
        myStatus = AttendanceStatus.YES,
        totalMembers = 3
    )

    @Test
    fun `invoke returns roster on success`() = runTest {
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(stubRoster)

        val result = useCase("d1")

        assertTrue(result.isSuccess)
        assertEquals(stubRoster, result.getOrNull())
    }

    @Test
    fun `invoke propagates failure`() = runTest {
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase("d1")

        assertTrue(result.isFailure)
    }
}
