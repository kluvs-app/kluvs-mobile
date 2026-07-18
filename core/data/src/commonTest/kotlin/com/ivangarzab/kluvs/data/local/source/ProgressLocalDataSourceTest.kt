package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.entities.BookEntity
import com.ivangarzab.kluvs.database.entities.ProgressEntity
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: ProgressLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = ProgressLocalDataSourceImpl(fixture.database)
    }

    private val progressEntity = ProgressEntity(
        id = "progress-1",
        memberId = "member-1",
        bookId = "book-1",
        sessionId = null,
        type = "PAGE",
        status = "IN_PROGRESS",
        currentPage = 10,
        percentComplete = null,
        startedAt = null,
        completedAt = null,
        lastFetchedAt = 0
    )

    private val bookEntity = BookEntity("book-1", "The Hobbit", "J.R.R. Tolkien", null, null, null, null, null, null, 0)

    @Test
    fun `getProgress returns entries with book summary populated`() = runTest {
        setup()
        everySuspend { fixture.progressDao.getProgressEntries("book-1", null, null) } returns listOf(progressEntity)
        everySuspend { fixture.bookDao.getBook("book-1") } returns bookEntity

        val result = dataSource.getProgress(bookId = "book-1")

        val expectedSummary = BookSummary(
            id = bookEntity.id,
            title = bookEntity.title,
            author = bookEntity.author,
            pageCount = bookEntity.pageCount,
            imageUrl = bookEntity.imageUrl
        )
        assertEquals(listOf(progressEntity.toDomain(expectedSummary)), result)
    }

    @Test
    fun `getProgress returns entries without book summary when book missing`() = runTest {
        setup()
        everySuspend { fixture.progressDao.getProgressEntries("book-1", null, null) } returns listOf(progressEntity)
        everySuspend { fixture.bookDao.getBook("book-1") } returns null

        val result = dataSource.getProgress(bookId = "book-1")

        assertEquals(listOf(progressEntity.toDomain(null)), result)
    }

    @Test
    fun `insertProgress inserts progress entity`() = runTest {
        setup()
        val progress = ReadingProgress(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            type = ProgressType.PAGE,
            status = ProgressStatus.IN_PROGRESS,
            currentPage = 10
        )

        everySuspend { fixture.progressDao.insertProgress(progress.toEntity()) } returns Unit

        dataSource.insertProgress(progress)
    }

    @Test
    fun `deleteProgress deletes existing entry`() = runTest {
        setup()
        everySuspend { fixture.progressDao.getProgress("progress-1") } returns progressEntity
        everySuspend { fixture.progressDao.deleteProgress(progressEntity) } returns Unit

        dataSource.deleteProgress("progress-1")
    }

    @Test
    fun `deleteAll clears all progress entries`() = runTest {
        setup()

        dataSource.deleteAll()
    }
}
