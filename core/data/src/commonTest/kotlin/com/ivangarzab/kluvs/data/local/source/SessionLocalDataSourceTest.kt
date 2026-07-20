package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.BookEntity
import com.ivangarzab.kluvs.database.entities.SessionEntity
import com.ivangarzab.kluvs.database.entities.SessionMemberEntity
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Session
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: SessionLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = SessionLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getSession returns session when book exists`() = runTest {
        setup()
        val sessionId = "session-1"
        val bookId = "book-1"
        val bookEntity = BookEntity(bookId, "The Hobbit", "Tolkien", null, 1937, null, null, null, null, 0)
        val sessionEntity = SessionEntity(sessionId, "club-1", bookId, "2026-03-15", 0)

        everySuspend { fixture.sessionDao.getSession(sessionId) } returns sessionEntity
        everySuspend { fixture.bookDao.getBook(bookId) } returns bookEntity

        val result = dataSource.getSession(sessionId)

        assertEquals(sessionEntity.toDomain(bookEntity.toDomain()), result)
    }

    @Test
    fun `getSession includes cached session members`() = runTest {
        setup()
        val sessionId = "session-1"
        val bookId = "book-1"
        val bookEntity = BookEntity(bookId, "The Hobbit", "Tolkien", null, 1937, null, null, null, null, 0)
        val sessionEntity = SessionEntity(sessionId, "club-1", bookId, "2026-03-15", 0)

        everySuspend { fixture.sessionDao.getSession(sessionId) } returns sessionEntity
        everySuspend { fixture.bookDao.getBook(bookId) } returns bookEntity
        everySuspend { fixture.sessionDao.getSessionMembers(sessionId) } returns listOf(
            SessionMemberEntity(sessionId, "7", "Ana", true),
            SessionMemberEntity(sessionId, "8", null, false)
        )

        val result = dataSource.getSession(sessionId)

        assertEquals(2, result?.members?.size)
        assertEquals("7", result?.members?.get(0)?.memberId)
        assertEquals(true, result?.members?.get(0)?.isReading)
        assertEquals(false, result?.members?.get(1)?.isReading)
    }

    @Test
    fun `getSession returns null when session does not exist`() = runTest {
        setup()
        everySuspend { fixture.sessionDao.getSession("not-found") } returns null

        assertNull(dataSource.getSession("not-found"))
    }

    @Test
    fun `getSession returns null when book id is null`() = runTest {
        setup()
        val sessionEntity = SessionEntity("session-1", "club-1", null, "2026-03-15", 0)
        everySuspend { fixture.sessionDao.getSession("session-1") } returns sessionEntity

        assertNull(dataSource.getSession("session-1"))
    }

    @Test
    fun `getSession returns null when book does not exist`() = runTest {
        setup()
        val sessionEntity = SessionEntity("session-1", "club-1", "book-1", "2026-03-15", 0)
        everySuspend { fixture.sessionDao.getSession("session-1") } returns sessionEntity
        everySuspend { fixture.bookDao.getBook("book-1") } returns null

        assertNull(dataSource.getSession("session-1"))
    }

    @Test
    fun `getSessionsForClub returns sessions for club`() = runTest {
        setup()
        val clubId = "club-1"
        val bookEntity = BookEntity("book-1", "Dune", "Herbert", null, 1965, null, null, null, null, 0)
        val sessions = listOf(
            SessionEntity("session-1", clubId, "book-1", "2026-04-01", 0)
        )

        everySuspend { fixture.sessionDao.getSessionsForClub(clubId) } returns sessions
        everySuspend { fixture.bookDao.getBook("book-1") } returns bookEntity

        val result = dataSource.getSessionsForClub(clubId)

        assertEquals(1, result.size)
        assertEquals(sessions[0].toDomain(bookEntity.toDomain()), result[0])
    }

    @Test
    fun `insertSession inserts session and book`() = runTest {
        setup()
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val session = Session("session-1", "club-1", book, null, emptyList())

        everySuspend { fixture.bookDao.insertBook(book.toEntity()) } returns Unit
        everySuspend { fixture.sessionDao.insertSession(session.toEntity()) } returns Unit

        dataSource.insertSession(session)
    }

    @Test
    fun `deleteSession deletes existing session`() = runTest {
        setup()
        val entity = SessionEntity("session-1", "club-1", "book-1", "2026-03-15", 0)
        everySuspend { fixture.sessionDao.getSession("session-1") } returns entity
        everySuspend { fixture.sessionDao.deleteSession(entity) } returns Unit

        dataSource.deleteSession("session-1")
    }

    @Test
    fun `deleteAll clears all sessions`() = runTest {
        setup()

        dataSource.deleteAll()
    }

    private fun Book.toEntity() = BookEntity(
        id = id,
        title = title,
        author = author,
        edition = edition,
        year = year,
        isbn = isbn,
        pageCount = null,
        imageUrl = null,
        externalGoogleId = null,
        lastFetchedAt = 0
    )

    private fun Session.toEntity() = SessionEntity(
        id = id,
        clubId = clubId,
        bookId = book.id,
        dueDate = dueDate?.toString(),
        lastFetchedAt = 0
    )
}
