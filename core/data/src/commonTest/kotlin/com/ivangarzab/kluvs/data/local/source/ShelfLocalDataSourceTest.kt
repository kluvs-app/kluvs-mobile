package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.entities.BookEntity
import com.ivangarzab.kluvs.database.entities.ShelfEntity
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShelfLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: ShelfLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = ShelfLocalDataSourceImpl(fixture.database)
    }

    private val book = Book(id = "book-1", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
    private val bookEntity = BookEntity("book-1", "The Hobbit", "J.R.R. Tolkien", null, null, null, null, null, null, 0)
    private val shelfEntity = ShelfEntity("book-1", "READ", "MANUAL", null, 0)

    @Test
    fun `getShelfEntry returns entry when book exists`() = runTest {
        setup()
        everySuspend { fixture.shelfDao.getShelfEntry("book-1") } returns shelfEntity
        everySuspend { fixture.bookDao.getBook("book-1") } returns bookEntity

        val result = dataSource.getShelfEntry("book-1")

        assertEquals(shelfEntity.toDomain(bookEntity.toDomain()), result)
    }

    @Test
    fun `getShelfEntry returns null when book does not exist`() = runTest {
        setup()
        everySuspend { fixture.shelfDao.getShelfEntry("book-1") } returns shelfEntity
        everySuspend { fixture.bookDao.getBook("book-1") } returns null

        assertNull(dataSource.getShelfEntry("book-1"))
    }

    @Test
    fun `getShelf skips entries with missing books`() = runTest {
        setup()
        everySuspend { fixture.shelfDao.getShelf() } returns listOf(shelfEntity)
        everySuspend { fixture.bookDao.getBook("book-1") } returns null

        val result = dataSource.getShelf()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `insertShelfEntry inserts book and shelf entry`() = runTest {
        setup()
        val entry = ShelfEntry(shelf = ShelfStatus.READ, source = ShelfSource.MANUAL, book = book)

        everySuspend { fixture.bookDao.insertBook(book.toEntity()) } returns Unit
        everySuspend { fixture.shelfDao.insertShelfEntry(entry.toEntity()) } returns Unit

        dataSource.insertShelfEntry(entry)
    }

    @Test
    fun `deleteShelfEntry deletes existing entry`() = runTest {
        setup()
        everySuspend { fixture.shelfDao.getShelfEntry("book-1") } returns shelfEntity
        everySuspend { fixture.shelfDao.deleteShelfEntry(shelfEntity) } returns Unit

        dataSource.deleteShelfEntry("book-1")
    }

    @Test
    fun `deleteAll clears all shelf entries`() = runTest {
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
}
