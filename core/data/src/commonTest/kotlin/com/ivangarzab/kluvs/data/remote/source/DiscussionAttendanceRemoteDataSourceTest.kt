package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionAttendanceService
import com.ivangarzab.kluvs.model.AttendanceStatus
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscussionAttendanceRemoteDataSourceTest {

    private lateinit var discussionAttendanceService: DiscussionAttendanceService
    private lateinit var dataSource: DiscussionAttendanceRemoteDataSource

    @BeforeTest
    fun setup() {
        discussionAttendanceService = mock<DiscussionAttendanceService>()
        dataSource = DiscussionAttendanceRemoteDataSourceImpl(discussionAttendanceService)
    }

    @Test
    fun `getRoster maps roster to domain`() = runTest {
        everySuspend { discussionAttendanceService.getRoster("disc-1") } returns
            DiscussionAttendanceRosterResponseDto(
                responses = emptyList(),
                myStatus = DiscussionAttendanceRosterResponseDto.MyStatus.maybe,
                totalMembers = 4
            )

        val result = dataSource.getRoster("disc-1")

        assertTrue(result.isSuccess)
        assertEquals(AttendanceStatus.MAYBE, result.getOrNull()?.myStatus)
        assertEquals(4, result.getOrNull()?.totalMembers)
    }

    @Test
    fun `upsertAttendance returns stored status`() = runTest {
        val request = DiscussionAttendanceUpsertRequestDto(
            discussionId = "disc-1",
            status = DiscussionAttendanceUpsertRequestDto.Status.yes
        )
        everySuspend { discussionAttendanceService.upsert(request) } returns
            DiscussionAttendanceDto(
                id = "att-1",
                discussionId = "disc-1",
                memberId = 1,
                status = DiscussionAttendanceDto.Status.yes,
                updatedAt = "2026-07-01T10:30:00"
            )

        val result = dataSource.upsertAttendance(request)

        assertEquals(AttendanceStatus.YES, result.getOrNull())
    }

    @Test
    fun `clearAttendance delegates to service`() = runTest {
        everySuspend { discussionAttendanceService.clear("disc-1") } returns Unit

        val result = dataSource.clearAttendance("disc-1")

        assertTrue(result.isSuccess)
        verifySuspend { discussionAttendanceService.clear("disc-1") }
    }

    @Test
    fun `getRoster returns failure when service throws`() = runTest {
        everySuspend { discussionAttendanceService.getRoster("disc-1") } throws
            Exception("Network error")

        val result = dataSource.getRoster("disc-1")

        assertTrue(result.isFailure)
    }
}
