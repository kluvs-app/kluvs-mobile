package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.BookEnrichmentLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.BookEnrichmentRemoteDataSource
import com.ivangarzab.kluvs.model.Author
import com.ivangarzab.kluvs.model.BookEnrichment
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class BookEnrichmentRepositoryTest {

    private lateinit var remote: BookEnrichmentRemoteDataSource
    private lateinit var local: BookEnrichmentLocalDataSource
    private lateinit var repository: BookEnrichmentRepository

    private val key = "goog-hobbit|J.R.R. Tolkien"
    private val cached = BookEnrichment(null, Author(name = "Cached", description = "old"))
    private val fetched = BookEnrichment(null, Author(name = "Fetched", description = "new"))

    private val now get() = Clock.System.now().toEpochMilliseconds()
    private val fresh get() = now - 1_000
    private val stale get() = now - CacheTTL.BOOK_ENRICHMENT.inWholeMilliseconds - 1_000

    @BeforeTest
    fun setup() {
        remote = mock<BookEnrichmentRemoteDataSource>()
        local = mock<BookEnrichmentLocalDataSource>()
        repository = BookEnrichmentRepositoryImpl(remote, local, CachePolicy())
        everySuspend { local.insertEnrichment(any(), any()) } returns Unit
    }

    private suspend fun get(forceRefresh: Boolean = false) =
        repository.getEnrichment("goog-hobbit", "J.R.R. Tolkien", forceRefresh)

    @Test
    fun `fresh cache is served without a network request`() = runTest {
        everySuspend { local.getEnrichment(key) } returns cached
        everySuspend { local.getLastFetchedAt(key) } returns fresh

        val result = get()

        assertEquals(cached, result.getOrNull())
        verifySuspend(VerifyMode.not) { remote.getEnrichment(any(), any()) }
    }

    @Test
    fun `stale cache refetches and upserts`() = runTest {
        everySuspend { local.getEnrichment(key) } returns cached
        everySuspend { local.getLastFetchedAt(key) } returns stale
        everySuspend { remote.getEnrichment("goog-hobbit", "J.R.R. Tolkien") } returns Result.success(fetched)

        val result = get()

        assertEquals(fetched, result.getOrNull())
        verifySuspend { local.insertEnrichment(key, fetched) }
    }

    @Test
    fun `missing cache fetches remotely and upserts`() = runTest {
        everySuspend { local.getEnrichment(key) } returns null
        everySuspend { local.getLastFetchedAt(key) } returns null
        everySuspend { remote.getEnrichment("goog-hobbit", "J.R.R. Tolkien") } returns Result.success(fetched)

        assertEquals(fetched, get().getOrNull())
        verifySuspend { local.insertEnrichment(key, fetched) }
    }

    @Test
    fun `remote failure with stale cached row serves the stale row`() = runTest {
        everySuspend { local.getEnrichment(key) } returns cached
        everySuspend { local.getLastFetchedAt(key) } returns stale
        everySuspend { remote.getEnrichment(any(), any()) } returns Result.failure(RuntimeException("offline"))

        val result = get()

        assertTrue(result.isSuccess)
        assertEquals(cached, result.getOrNull())
    }

    @Test
    fun `remote failure with no cache keeps the failure`() = runTest {
        everySuspend { local.getEnrichment(key) } returns null
        everySuspend { local.getLastFetchedAt(key) } returns null
        everySuspend { remote.getEnrichment(any(), any()) } returns Result.failure(RuntimeException("offline"))

        val result = get()

        assertTrue(result.isFailure)
        assertEquals("offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `forceRefresh skips a fresh cache and fetches remotely`() = runTest {
        everySuspend { local.getEnrichment(key) } returns cached
        everySuspend { local.getLastFetchedAt(key) } returns fresh
        everySuspend { remote.getEnrichment("goog-hobbit", "J.R.R. Tolkien") } returns Result.success(fetched)

        assertEquals(fetched, get(forceRefresh = true).getOrNull())
    }

    @Test
    fun `cache write failure still returns the fetched data`() = runTest {
        everySuspend { local.getEnrichment(key) } returns null
        everySuspend { local.getLastFetchedAt(key) } returns null
        everySuspend { remote.getEnrichment(any(), any()) } returns Result.success(fetched)
        everySuspend { local.insertEnrichment(any(), any()) } calls { throw RuntimeException("disk full") }

        assertEquals(fetched, get().getOrNull())
    }
}
