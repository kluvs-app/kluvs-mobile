package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.data.remote.source.DiscussionAttendanceRemoteDataSource
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscussionAttendanceRepositoryTest {

    private lateinit var remoteDataSource: DiscussionAttendanceRemoteDataSource
    private lateinit var repository: DiscussionAttendanceRepository

    private val testRoster = AttendanceRoster(
        responses = emptyList(),
        myStatus = AttendanceStatus.YES,
        totalMembers = 1
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<DiscussionAttendanceRemoteDataSource>()
        repository = DiscussionAttendanceRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getRoster success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.getRoster("discussion-1") } returns Result.success(testRoster)

        val result = repository.getRoster("discussion-1")

        assertTrue(result.isSuccess)
        assertEquals(testRoster, result.getOrNull())
        verifySuspend { remoteDataSource.getRoster("discussion-1") }
    }

    @Test
    fun `setAttendance success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.upsertAttendance(any()) } returns Result.success(AttendanceStatus.YES)

        val result = repository.setAttendance("discussion-1", AttendanceStatus.YES)

        assertTrue(result.isSuccess)
        assertEquals(AttendanceStatus.YES, result.getOrNull())
        verifySuspend {
            remoteDataSource.upsertAttendance(
                DiscussionAttendanceUpsertRequestDto(
                    discussionId = "discussion-1",
                    status = DiscussionAttendanceUpsertRequestDto.Status.yes
                )
            )
        }
    }

    @Test
    fun `clearAttendance success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.clearAttendance("discussion-1") } returns Result.success(Unit)

        val result = repository.clearAttendance("discussion-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.clearAttendance("discussion-1") }
    }
}
