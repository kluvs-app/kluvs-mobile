package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetSessionProgressUseCaseTest {

    private val progressRepository = mock<ProgressRepository>()
    private val useCase = GetSessionProgressUseCase(progressRepository)

    private fun progress(
        type: ProgressType = ProgressType.PAGE,
        status: ProgressStatus = ProgressStatus.IN_PROGRESS,
        currentPage: Int? = 50,
        percentComplete: Float? = null
    ) = ReadingProgress(
        id = "progress-1",
        memberId = "7",
        bookId = "book-1",
        sessionId = "session-1",
        type = type,
        status = status,
        currentPage = currentPage,
        percentComplete = percentComplete
    )

    @Test
    fun `invoke maps page-type progress with percent and label`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(progress(currentPage = 50)))

        val result = useCase("session-1", pageCount = 200)

        assertTrue(result.isSuccess)
        val info = result.getOrNull()!!
        assertEquals("progress-1", info.progressId)
        assertEquals(25, info.percent)
        assertEquals("50 of 200 pages", info.label)
        assertFalse(info.isCompleted)
    }

    @Test
    fun `invoke maps percent-type progress`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(progress(type = ProgressType.PERCENT, currentPage = null, percentComplete = 45f)))

        val result = useCase("session-1", pageCount = null)

        val info = result.getOrNull()!!
        assertEquals(45, info.percent)
        assertEquals("45% complete", info.label)
    }

    @Test
    fun `invoke maps completed progress to Finished label`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(progress(status = ProgressStatus.COMPLETED, currentPage = 200)))

        val result = useCase("session-1", pageCount = 200)

        val info = result.getOrNull()!!
        assertEquals("Finished", info.label)
        assertTrue(info.isCompleted)
        assertEquals(100, info.percent)
    }

    @Test
    fun `invoke caps percent at 100`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(progress(currentPage = 500)))

        val result = useCase("session-1", pageCount = 200)

        assertEquals(100, result.getOrNull()?.percent)
    }

    @Test
    fun `invoke returns null when member has no entry for the session`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(emptyList())

        val result = useCase("session-1", pageCount = 200)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        val result = useCase("session-1", pageCount = 200)

        assertTrue(result.isFailure)
    }
}
