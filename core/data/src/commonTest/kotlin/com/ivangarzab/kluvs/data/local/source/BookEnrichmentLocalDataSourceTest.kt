package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.database.entities.BookEnrichmentEntity
import com.ivangarzab.kluvs.model.BookEnrichment
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookEnrichmentLocalDataSourceTest {

    private fun setup(): Pair<DatabaseMockFixture, BookEnrichmentLocalDataSource> {
        val fixture = DatabaseMockFixture()
        return fixture to BookEnrichmentLocalDataSourceImpl(fixture.database)
    }

    @Test
    fun `getEnrichment returns mapped enrichment when row exists`() = runTest {
        val (fixture, dataSource) = setup()
        everySuspend { fixture.bookEnrichmentDao.getEnrichment("k") } returns
            BookEnrichmentEntity("k", null, null, "[]", 0)

        assertEquals(BookEnrichment(null, null, emptyList()), dataSource.getEnrichment("k"))
    }

    @Test
    fun `getEnrichment returns null when row is missing`() = runTest {
        val (fixture, dataSource) = setup()
        everySuspend { fixture.bookEnrichmentDao.getEnrichment("k") } returns null

        assertNull(dataSource.getEnrichment("k"))
    }

    @Test
    fun `insertEnrichment upserts through the dao`() = runTest {
        val (fixture, dataSource) = setup()
        everySuspend { fixture.bookEnrichmentDao.insertEnrichment(any()) } returns Unit

        dataSource.insertEnrichment("k", BookEnrichment(null, null))

        verifySuspend { fixture.bookEnrichmentDao.insertEnrichment(any()) }
    }

    @Test
    fun `getLastFetchedAt delegates to the dao`() = runTest {
        val (fixture, dataSource) = setup()
        everySuspend { fixture.bookEnrichmentDao.getLastFetchedAt("k") } returns 123L

        assertEquals(123L, dataSource.getLastFetchedAt("k"))
    }
}
