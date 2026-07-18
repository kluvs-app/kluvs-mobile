package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.source.ProgressRemoteDataSource
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
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

class ProgressRepositoryTest {

    private lateinit var remoteDataSource: ProgressRemoteDataSource
    private lateinit var repository: ProgressRepository

    private val testProgress = ReadingProgress(
        id = "progress-1",
        memberId = "member-1",
        bookId = "42",
        type = ProgressType.PAGE,
        status = ProgressStatus.IN_PROGRESS,
        currentPage = 10
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ProgressRemoteDataSource>()
        repository = ProgressRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getProgress success delegates to remote with optional parameters`() = runTest {
        val expected = listOf(testProgress)
        everySuspend { remoteDataSource.getProgress(any(), any(), any()) } returns Result.success(expected)

        val result = repository.getProgress(bookId = "42", sessionId = "session-1", status = ProgressStatus.IN_PROGRESS)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        verifySuspend { remoteDataSource.getProgress(42, "session-1", "in_progress") }
    }

    @Test
    fun `getProgress returns failure for invalid book ID`() = runTest {
        val result = repository.getProgress(bookId = "invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `createProgress success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.createProgress(any()) } returns Result.success(testProgress)

        val result = repository.createProgress(
            bookId = "42",
            type = ProgressType.PERCENT,
            percentComplete = 25f,
            sessionId = "session-1"
        )

        assertTrue(result.isSuccess)
        assertEquals(testProgress, result.getOrNull())
        verifySuspend {
            remoteDataSource.createProgress(
                ProgressCreateRequestDto(
                    bookId = 42,
                    progressType = ProgressCreateRequestDto.ProgressType.percent,
                    percentComplete = 25f,
                    sessionId = "session-1"
                )
            )
        }
    }

    @Test
    fun `createProgress returns failure for invalid book ID`() = runTest {
        val result = repository.createProgress("invalid", ProgressType.PAGE)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `updateProgress success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.updateProgress(any()) } returns Result.success(testProgress)

        val result = repository.updateProgress(
            progressId = "progress-1",
            type = ProgressType.PAGE,
            currentPage = 50,
            status = ProgressStatus.COMPLETED
        )

        assertTrue(result.isSuccess)
        assertEquals(testProgress, result.getOrNull())
        verifySuspend {
            remoteDataSource.updateProgress(
                ProgressUpdateRequestDto(
                    id = "progress-1",
                    progressType = ProgressUpdateRequestDto.ProgressType.page,
                    currentPage = 50,
                    status = ProgressUpdateRequestDto.Status.completed
                )
            )
        }
    }

    @Test
    fun `deleteProgress success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.deleteProgress("progress-1") } returns Result.success(Unit)

        val result = repository.deleteProgress("progress-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.deleteProgress("progress-1") }
    }
}
