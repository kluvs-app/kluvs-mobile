package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveProgressUseCaseTest {

    private val progressRepository = mock<ProgressRepository>()
    private val useCase = SaveProgressUseCase(progressRepository)

    private fun progress(
        id: String = "progress-1",
        status: ProgressStatus = ProgressStatus.IN_PROGRESS,
        currentPage: Int? = 50
    ) = ReadingProgress(
        id = id,
        memberId = "7",
        bookId = "book-1",
        sessionId = "session-1",
        type = ProgressType.PAGE,
        status = status,
        currentPage = currentPage
    )

    private fun params(
        progressId: String? = null,
        markFinished: Boolean = false
    ) = SaveProgressUseCase.Params(
        progressId = progressId,
        bookId = "book-1",
        sessionId = "session-1",
        pageCount = 200,
        type = ProgressType.PAGE,
        currentPage = 50,
        percentComplete = null,
        markFinished = markFinished
    )

    @Test
    fun `invoke with existing progressId updates the entry`() = runTest {
        everySuspend {
            progressRepository.updateProgress(any(), any(), any(), any(), any())
        } returns Result.success(progress())

        val result = useCase(params(progressId = "progress-1"))

        assertTrue(result.isSuccess)
        assertEquals("progress-1", result.getOrNull()?.progressId)
        assertEquals("50 of 200 pages", result.getOrNull()?.label)
        verifySuspend {
            progressRepository.updateProgress(
                progressId = "progress-1",
                type = ProgressType.PAGE,
                currentPage = 50,
                percentComplete = null,
                status = ProgressStatus.IN_PROGRESS
            )
        }
        verifySuspend(mode = VerifyMode.exactly(0)) {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke without progressId creates a new entry tied to the session`() = runTest {
        everySuspend {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        } returns Result.success(progress())

        val result = useCase(params(progressId = null))

        assertTrue(result.isSuccess)
        verifySuspend {
            progressRepository.createProgress(
                bookId = "book-1",
                type = ProgressType.PAGE,
                currentPage = 50,
                percentComplete = null,
                sessionId = "session-1"
            )
        }
        verifySuspend(mode = VerifyMode.exactly(0)) {
            progressRepository.updateProgress(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke marking finished on create follows up with a status update`() = runTest {
        everySuspend {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        } returns Result.success(progress())
        everySuspend {
            progressRepository.updateProgress(any(), any(), any(), any(), any())
        } returns Result.success(progress(status = ProgressStatus.COMPLETED))

        val result = useCase(params(progressId = null, markFinished = true))

        assertTrue(result.isSuccess)
        assertEquals("Finished", result.getOrNull()?.label)
        assertTrue(result.getOrNull()?.isCompleted == true)
        verifySuspend {
            progressRepository.updateProgress(
                progressId = "progress-1",
                type = ProgressType.PAGE,
                currentPage = 50,
                percentComplete = null,
                status = ProgressStatus.COMPLETED
            )
        }
    }

    @Test
    fun `invoke marking finished on update sets COMPLETED status`() = runTest {
        everySuspend {
            progressRepository.updateProgress(any(), any(), any(), any(), any())
        } returns Result.success(progress(status = ProgressStatus.COMPLETED))

        val result = useCase(params(progressId = "progress-1", markFinished = true))

        assertTrue(result.isSuccess)
        verifySuspend {
            progressRepository.updateProgress(
                progressId = "progress-1",
                type = ProgressType.PAGE,
                currentPage = 50,
                percentComplete = null,
                status = ProgressStatus.COMPLETED
            )
        }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(params(progressId = null))

        assertTrue(result.isFailure)
    }
}
