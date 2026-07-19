package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSource
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Session
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matching
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SessionRepositoryTest {

    private lateinit var remoteDataSource: SessionRemoteDataSource
    private lateinit var localDataSource: SessionLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: SessionRepository

    private val testBook = Book(
        id = "123",
        title = "1984",
        author = "George Orwell",
        isbn = "978-0451524935"
    )

    private val testDueDate = LocalDateTime.parse("2024-12-31T23:59:59")

    private val testDiscussions = listOf(
        Discussion(
            id = "discussion-1",
            sessionId = "session-123",
            title = "Chapter 1-5",
            date = LocalDateTime.parse("2024-11-15T18:00:00"),
            location = "Main Library"
        ),
        Discussion(
            id = "discussion-2",
            sessionId = "session-123",
            title = "Chapter 6-10",
            date = LocalDateTime.parse("2024-11-22T18:00:00"),
            location = "Main Library"
        )
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<SessionRemoteDataSource>()
        localDataSource = mock<SessionLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = SessionRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getSession(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.insertSession(any()) } returns Unit
        everySuspend { localDataSource.deleteSession(any()) } returns Unit
    }

    // ========================================
    // GET SESSION
    // ========================================

    @Test
    fun `getSession success returns Session with nested data`() = runTest {
        val sessionId = "session-123"
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = testBook,
            dueDate = testDueDate,
            discussions = testDiscussions
        )
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.getSession(sessionId)

        assertTrue(result.isSuccess)
        assertEquals(expectedSession, result.getOrNull())
        assertEquals(testBook, result.getOrNull()?.book)
        assertEquals(2, result.getOrNull()?.discussions?.size)
        verifySuspend { remoteDataSource.getSession(sessionId) }
    }

    @Test
    fun `getSession failure returns Result failure`() = runTest {
        val sessionId = "session-123"
        val exception = Exception("Session not found")
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.failure(exception)

        val result = repository.getSession(sessionId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getSession(sessionId) }
    }

    @Test
    fun `getSession with non-existent session returns failure`() = runTest {
        val sessionId = "non-existent"
        val exception = Exception("Session not found")
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.failure(exception)

        val result = repository.getSession(sessionId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.getSession(sessionId) }
    }

    // ========================================
    // CREATE SESSION
    // ========================================

    @Test
    fun `createSession success creates session with book and due date`() = runTest {
        val clubId = "club-456"
        val expectedSession = Session(
            id = "session-new",
            clubId = clubId,
            book = testBook,
            dueDate = testDueDate
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession(clubId, testBook, testDueDate, null)

        assertTrue(result.isSuccess)
        assertEquals(expectedSession, result.getOrNull())
        assertEquals(testBook, result.getOrNull()?.book)
        assertEquals(testDueDate, result.getOrNull()?.dueDate)
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    @Test
    fun `createSession success creates session without due date`() = runTest {
        val clubId = "club-456"
        val expectedSession = Session(
            id = "session-new",
            clubId = clubId,
            book = testBook,
            dueDate = null
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession(clubId, testBook, null, null)

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()?.dueDate)
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    @Test
    fun `createSession using default discussions parameter`() = runTest {
        val clubId = "club-456"
        val expectedSession = Session(
            id = "session-new",
            clubId = clubId,
            book = testBook,
            dueDate = testDueDate
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession(clubId, testBook, testDueDate)

        assertTrue(result.isSuccess)
        assertEquals(expectedSession, result.getOrNull())
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    @Test
    fun `createSession with discussions creates session with discussions`() = runTest {
        val clubId = "club-456"
        val expectedSession = Session(
            id = "session-new",
            clubId = clubId,
            book = testBook,
            dueDate = testDueDate,
            discussions = testDiscussions
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession(clubId, testBook, testDueDate, testDiscussions)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.discussions?.size)
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    @Test
    fun `createSession with empty discussions creates session without discussions`() = runTest {
        val clubId = "club-456"
        val expectedSession = Session(
            id = "session-new",
            clubId = clubId,
            book = testBook,
            dueDate = testDueDate,
            discussions = emptyList()
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession(clubId, testBook, testDueDate, emptyList())

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.discussions?.size)
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    @Test
    fun `createSession sends book_id instead of inline book`() = runTest {
        val expectedSession = Session(
            id = "session-new",
            clubId = "club-456",
            book = testBook,
            dueDate = testDueDate
        )
        everySuspend { remoteDataSource.createSession(any()) } returns Result.success(expectedSession)

        val result = repository.createSession("club-456", testBook, testDueDate, null)

        assertTrue(result.isSuccess)
        verifySuspend {
            remoteDataSource.createSession(
                matching<SessionCreateRequestDto> { dto ->
                    dto.bookId == 123 && dto.book == null
                }
            )
        }
    }

    @Test
    fun `createSession failure returns Result failure`() = runTest {
        val exception = Exception("Failed to create session")
        everySuspend { remoteDataSource.createSession(any()) } returns Result.failure(exception)

        val result = repository.createSession("club-456", testBook, testDueDate, null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.createSession(any()) }
    }

    // ========================================
    // UPDATE SESSION
    // ========================================
    // Note: PUT /session never returns the updated session, so the repository
    // re-fetches via getSession() on success — tests mock both calls.

    @Test
    fun `updateSession with book updates the book`() = runTest {
        val sessionId = "session-123"
        val newBook = Book(
            id = "book-456",
            title = "Brave New World",
            author = "Aldous Huxley",
            isbn = "978-0060850524"
        )
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = newBook,
            dueDate = testDueDate
        )
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.success(Unit)
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.updateSession(sessionId, book = newBook)

        assertTrue(result.isSuccess)
        assertEquals(newBook, result.getOrNull()?.book)
        verifySuspend { remoteDataSource.updateSession(any()) }
        verifySuspend { remoteDataSource.getSession(sessionId) }
    }

    @Test
    fun `updateSession with null book does not update book`() = runTest {
        val sessionId = "session-123"
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = testBook,
            dueDate = LocalDateTime.parse("2025-01-15T23:59:59")
        )
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.success(Unit)
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.updateSession(sessionId, book = null, dueDate = LocalDateTime.parse("2025-01-15T23:59:59"))

        assertTrue(result.isSuccess)
        assertEquals(testBook, result.getOrNull()?.book)
        assertEquals(LocalDateTime.parse("2025-01-15T23:59:59"), result.getOrNull()?.dueDate)
        verifySuspend { remoteDataSource.updateSession(any()) }
    }

    @Test
    fun `updateSession with due date updates due date`() = runTest {
        val sessionId = "session-123"
        val newDueDate = LocalDateTime.parse("2025-01-31T23:59:59")
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = testBook,
            dueDate = newDueDate
        )
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.success(Unit)
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.updateSession(sessionId, dueDate = newDueDate)

        assertTrue(result.isSuccess)
        assertEquals(newDueDate, result.getOrNull()?.dueDate)
        verifySuspend { remoteDataSource.updateSession(any()) }
    }

    @Test
    fun `updateSession with discussions replaces all discussions`() = runTest {
        val sessionId = "session-123"
        val newDiscussions = listOf(
            Discussion(
                id = "discussion-3",
                sessionId = sessionId,
                title = "Final Discussion",
                date = LocalDateTime.parse("2024-12-01T18:00:00"),
                location = "Coffee Shop"
            )
        )
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = testBook,
            dueDate = testDueDate,
            discussions = newDiscussions
        )
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.success(Unit)
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.updateSession(sessionId, discussions = newDiscussions)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.discussions?.size)
        assertEquals("Final Discussion", result.getOrNull()?.discussions?.first()?.title)
        verifySuspend { remoteDataSource.updateSession(any()) }
    }

    @Test
    fun `updateSession with discussionIdsToDelete deletes specific discussions`() = runTest {
        val sessionId = "session-123"
        val discussionIdsToDelete = listOf("discussion-1")
        val remainingDiscussions = listOf(testDiscussions[1])
        val expectedSession = Session(
            id = sessionId,
            clubId = "club-456",
            book = testBook,
            dueDate = testDueDate,
            discussions = remainingDiscussions
        )
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.success(Unit)
        everySuspend { remoteDataSource.getSession(sessionId) } returns Result.success(expectedSession)

        val result = repository.updateSession(sessionId, discussionIdsToDelete = discussionIdsToDelete)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.discussions?.size)
        verifySuspend { remoteDataSource.updateSession(any()) }
    }

    @Test
    fun `updateSession failure returns Result failure without fetching session`() = runTest {
        val exception = Exception("Failed to update session")
        everySuspend { remoteDataSource.updateSession(any()) } returns Result.failure(exception)

        val result = repository.updateSession("session-123", book = testBook)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.updateSession(any()) }
    }

    // ========================================
    // DELETE SESSION
    // ========================================

    @Test
    fun `deleteSession success returns success message`() = runTest {
        val sessionId = "session-123"
        val successMessage = "Session deleted successfully"
        everySuspend { remoteDataSource.deleteSession(sessionId) } returns Result.success(successMessage)

        val result = repository.deleteSession(sessionId)

        assertTrue(result.isSuccess)
        assertEquals(successMessage, result.getOrNull())
        verifySuspend { remoteDataSource.deleteSession(sessionId) }
    }

    @Test
    fun `deleteSession failure returns Result failure`() = runTest {
        val sessionId = "session-123"
        val exception = Exception("Failed to delete session")
        everySuspend { remoteDataSource.deleteSession(sessionId) } returns Result.failure(exception)

        val result = repository.deleteSession(sessionId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.deleteSession(sessionId) }
    }

    @Test
    fun `deleteSession with non-existent session returns failure`() = runTest {
        val sessionId = "non-existent"
        val exception = Exception("Session not found")
        everySuspend { remoteDataSource.deleteSession(sessionId) } returns Result.failure(exception)

        val result = repository.deleteSession(sessionId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.deleteSession(sessionId) }
    }

    // ========================================
    // FINISH SESSION
    // ========================================

    @Test
    fun `finishSession success returns credited count and evicts cached session`() = runTest {
        val sessionId = "session-123"
        everySuspend { remoteDataSource.finishSession(sessionId) } returns Result.success(2)

        val result = repository.finishSession(sessionId)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        verifySuspend { remoteDataSource.finishSession(sessionId) }
        verifySuspend { localDataSource.deleteSession(sessionId) }
    }

    @Test
    fun `finishSession failure returns Result failure and keeps cache`() = runTest {
        val sessionId = "session-123"
        val exception = Exception("Session already finished")
        everySuspend { remoteDataSource.finishSession(sessionId) } returns Result.failure(exception)

        val result = repository.finishSession(sessionId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
            localDataSource.deleteSession(any())
        }
    }
}
