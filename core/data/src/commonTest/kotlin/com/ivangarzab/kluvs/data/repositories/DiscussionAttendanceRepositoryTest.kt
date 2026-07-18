package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.data.local.source.DiscussionAttendanceLocalDataSource
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
    private lateinit var localDataSource: DiscussionAttendanceLocalDataSource
    private lateinit var repository: DiscussionAttendanceRepository

    private val testRoster = AttendanceRoster(
        responses = emptyList(),
        myStatus = AttendanceStatus.YES,
        totalMembers = 1
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<DiscussionAttendanceRemoteDataSource>()
        localDataSource = mock<DiscussionAttendanceLocalDataSource>()
        repository = DiscussionAttendanceRepositoryImpl(remoteDataSource, localDataSource)

        everySuspend { localDataSource.setMyStatus(any(), any()) } returns Unit
        everySuspend { localDataSource.clearMyStatus(any()) } returns Unit
    }

    @Test
    fun `getRoster success delegates to remote and caches my status`() = runTest {
        everySuspend { remoteDataSource.getRoster("discussion-1") } returns Result.success(testRoster)

        val result = repository.getRoster("discussion-1")

        assertTrue(result.isSuccess)
        assertEquals(testRoster, result.getOrNull())
        verifySuspend { remoteDataSource.getRoster("discussion-1") }
        verifySuspend { localDataSource.setMyStatus("discussion-1", AttendanceStatus.YES) }
    }

    @Test
    fun `getRoster success with no myStatus clears cache`() = runTest {
        val rosterWithoutMyStatus = testRoster.copy(myStatus = null)
        everySuspend { remoteDataSource.getRoster("discussion-1") } returns Result.success(rosterWithoutMyStatus)

        val result = repository.getRoster("discussion-1")

        assertTrue(result.isSuccess)
        verifySuspend { localDataSource.clearMyStatus("discussion-1") }
    }

    @Test
    fun `setAttendance success delegates to remote with mapped request and caches result`() = runTest {
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
        verifySuspend { localDataSource.setMyStatus("discussion-1", AttendanceStatus.YES) }
    }

    @Test
    fun `clearAttendance success delegates to remote and clears cache`() = runTest {
        everySuspend { remoteDataSource.clearAttendance("discussion-1") } returns Result.success(Unit)

        val result = repository.clearAttendance("discussion-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.clearAttendance("discussion-1") }
        verifySuspend { localDataSource.clearMyStatus("discussion-1") }
    }
}
