package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.ShelfLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.ShelfRemoteDataSource
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ShelfRepositoryTest {

    private lateinit var remoteDataSource: ShelfRemoteDataSource
    private lateinit var localDataSource: ShelfLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: ShelfRepository

    private val testShelfEntry = ShelfEntry(
        shelf = ShelfStatus.READ,
        book = Book(id = "5", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ShelfRemoteDataSource>()
        localDataSource = mock<ShelfLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = ShelfRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (empty/null)
        everySuspend { localDataSource.getShelf() } returns emptyList()
        everySuspend { localDataSource.getShelfEntry(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.insertShelfEntries(any()) } returns Unit
        everySuspend { localDataSource.deleteShelfEntry(any()) } returns Unit
    }

    @Test
    fun `getShelf cache miss fetches from remote and caches result`() = runTest {
        val expected = listOf(testShelfEntry)
        everySuspend { remoteDataSource.getShelf() } returns Result.success(expected)

        val result = repository.getShelf()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        verifySuspend { remoteDataSource.getShelf() }
        verifySuspend { localDataSource.insertShelfEntries(expected) }
    }

    @Test
    fun `getShelf cache hit returns cached data without hitting remote`() = runTest {
        // remoteDataSource.getShelf() intentionally left unstubbed: a strict mock throws
        // if it's invoked, so a passing test proves the cache path was used.
        everySuspend { localDataSource.getShelf() } returns listOf(testShelfEntry)
        everySuspend { localDataSource.getLastFetchedAt("5") } returns Long.MAX_VALUE

        val result = repository.getShelf()

        assertTrue(result.isSuccess)
        assertEquals(listOf(testShelfEntry), result.getOrNull())
    }

    @Test
    fun `getShelfStatus success delegates to remote with parsed int ID`() = runTest {
        everySuspend { remoteDataSource.getShelfStatus(5) } returns Result.success(ShelfStatus.READ)

        val result = repository.getShelfStatus("5")

        assertTrue(result.isSuccess)
        assertEquals(ShelfStatus.READ, result.getOrNull())
        verifySuspend { remoteDataSource.getShelfStatus(5) }
    }

    @Test
    fun `getShelfStatus cache hit returns cached status without hitting remote`() = runTest {
        // remoteDataSource.getShelfStatus() intentionally left unstubbed: a strict mock
        // throws if it's invoked, so a passing test proves the cache path was used.
        everySuspend { localDataSource.getShelfEntry("5") } returns testShelfEntry
        everySuspend { localDataSource.getLastFetchedAt("5") } returns Long.MAX_VALUE

        val result = repository.getShelfStatus("5")

        assertTrue(result.isSuccess)
        assertEquals(ShelfStatus.READ, result.getOrNull())
    }

    @Test
    fun `getShelfStatus returns failure for invalid book ID`() = runTest {
        val result = repository.getShelfStatus("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `assignShelf success delegates to remote and invalidates cache`() = runTest {
        val expected = ShelfStatus.CURRENTLY_READING
        everySuspend { remoteDataSource.assignShelf(any()) } returns Result.success(expected)

        val result = repository.assignShelf("5", ShelfStatus.CURRENTLY_READING)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        verifySuspend {
            remoteDataSource.assignShelf(
                ShelfAssignRequestDto(
                    bookId = 5,
                    shelf = com.ivangarzab.kluvs.api.models.ShelfStatusDto.currently_reading
                )
            )
        }
        verifySuspend { localDataSource.deleteShelfEntry("5") }
    }

    @Test
    fun `assignShelf returns failure for invalid book ID`() = runTest {
        val result = repository.assignShelf("invalid", ShelfStatus.READ)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `removeFromShelf success delegates to remote and evicts cache`() = runTest {
        everySuspend { remoteDataSource.removeShelf(5) } returns Result.success(Unit)

        val result = repository.removeFromShelf("5")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.removeShelf(5) }
        verifySuspend { localDataSource.deleteShelfEntry("5") }
    }

    @Test
    fun `removeFromShelf returns failure for invalid book ID`() = runTest {
        val result = repository.removeFromShelf("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
