package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressDeleteResponseDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import com.ivangarzab.kluvs.data.remote.api.ProgressService
import com.ivangarzab.kluvs.model.ProgressStatus
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressRemoteDataSourceTest {

    private lateinit var progressService: ProgressService
    private lateinit var dataSource: ProgressRemoteDataSource

    private val progressDto = ReadingProgressDto(
        id = "progress-1",
        memberId = 1,
        bookId = 5,
        progressType = ReadingProgressDto.ProgressType.page,
        status = ReadingProgressDto.Status.in_progress,
        startedAt = "2026-07-01T10:30:00",
        updatedAt = "2026-07-02T11:00:00",
        currentPage = 50
    )

    @BeforeTest
    fun setup() {
        progressService = mock<ProgressService>()
        dataSource = ProgressRemoteDataSourceImpl(progressService)
    }

    @Test
    fun `getProgress maps entries to domain`() = runTest {
        everySuspend { progressService.getAll(5, null, null) } returns listOf(progressDto)

        val result = dataSource.getProgress(bookId = 5)

        assertTrue(result.isSuccess)
        assertEquals("progress-1", result.getOrNull()?.first()?.id)
        assertEquals(ProgressStatus.IN_PROGRESS, result.getOrNull()?.first()?.status)
    }

    @Test
    fun `createProgress maps created entry to domain`() = runTest {
        val request = ProgressCreateRequestDto(
            bookId = 5,
            progressType = ProgressCreateRequestDto.ProgressType.page,
            currentPage = 50
        )
        everySuspend { progressService.create(request) } returns progressDto

        val result = dataSource.createProgress(request)

        assertEquals("5", result.getOrNull()?.bookId)
    }

    @Test
    fun `updateProgress returns failure when service throws`() = runTest {
        val request = ProgressUpdateRequestDto(
            id = "progress-1",
            progressType = ProgressUpdateRequestDto.ProgressType.page
        )
        everySuspend { progressService.update(request) } throws Exception("Network error")

        val result = dataSource.updateProgress(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteProgress succeeds on successful response`() = runTest {
        everySuspend { progressService.delete("progress-1") } returns
            ProgressDeleteResponseDto(success = true)

        val result = dataSource.deleteProgress("progress-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteProgress fails when backend reports failure`() = runTest {
        everySuspend { progressService.delete("progress-1") } returns
            ProgressDeleteResponseDto(success = false)

        val result = dataSource.deleteProgress("progress-1")

        assertTrue(result.isFailure)
    }
}
