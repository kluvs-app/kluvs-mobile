package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.LikeLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.LikeRemoteDataSource
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
class LikeRepositoryTest {

    private lateinit var remoteDataSource: LikeRemoteDataSource
    private lateinit var localDataSource: LikeLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: LikeRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<LikeRemoteDataSource>()
        localDataSource = mock<LikeLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = LikeRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getLikeStatus(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.setLikeStatus(any(), any()) } returns Unit
    }

    @Test
    fun `isBookLiked cache miss delegates to remote and caches result`() = runTest {
        everySuspend { remoteDataSource.getLikeStatus(42) } returns Result.success(true)

        val result = repository.isBookLiked("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        verifySuspend { remoteDataSource.getLikeStatus(42) }
        verifySuspend { localDataSource.setLikeStatus("42", true) }
    }

    @Test
    fun `isBookLiked cache hit returns cached data without hitting remote`() = runTest {
        // remoteDataSource.getLikeStatus() intentionally left unstubbed: a strict mock
        // throws if it's invoked, so a passing test proves the cache path was used.
        everySuspend { localDataSource.getLikeStatus("42") } returns true
        everySuspend { localDataSource.getLastFetchedAt("42") } returns Long.MAX_VALUE

        val result = repository.isBookLiked("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `isBookLiked returns failure for invalid book ID`() = runTest {
        val result = repository.isBookLiked("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `toggleLike success delegates to remote and writes through cache`() = runTest {
        everySuspend { remoteDataSource.toggleLike(any()) } returns Result.success(true)

        val result = repository.toggleLike("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        verifySuspend { remoteDataSource.toggleLike(LikeToggleRequestDto(bookId = 42)) }
        verifySuspend { localDataSource.setLikeStatus("42", true) }
    }

    @Test
    fun `toggleLike returns failure for invalid book ID`() = runTest {
        val result = repository.toggleLike("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
