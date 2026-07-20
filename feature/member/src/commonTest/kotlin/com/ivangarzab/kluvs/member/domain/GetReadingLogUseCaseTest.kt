package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ClubPreview
import com.ivangarzab.kluvs.model.ReadingLog
import com.ivangarzab.kluvs.model.ReadingLogEntry
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetReadingLogUseCaseTest {

    private val sessionRepository = mock<SessionRepository>()
    private val useCase = GetReadingLogUseCase(sessionRepository)

    @Test
    fun `invoke returns the reading log grouped into active and finished`() = runTest {
        val log = ReadingLog(
            active = listOf(
                ReadingLogEntry(
                    sessionId = "s1",
                    book = BookSummary(id = "b1", title = "Dune", author = "Frank Herbert"),
                    club = ClubPreview(id = "c1", name = "Sci-Fi Club")
                )
            ),
            finished = listOf(
                ReadingLogEntry(
                    sessionId = "s2",
                    book = BookSummary(id = "b2", title = "1984", author = "George Orwell"),
                    club = ClubPreview(id = "c2", name = "Classics Club")
                )
            )
        )
        everySuspend { sessionRepository.getReadingLog() } returns Result.success(log)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(log, result.getOrNull())
        verifySuspend { sessionRepository.getReadingLog() }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { sessionRepository.getReadingLog() } returns Result.failure(RuntimeException("Network error"))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
