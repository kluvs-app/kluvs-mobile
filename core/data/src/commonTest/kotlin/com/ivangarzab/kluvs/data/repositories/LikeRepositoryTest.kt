package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
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

class LikeRepositoryTest {

    private lateinit var remoteDataSource: LikeRemoteDataSource
    private lateinit var repository: LikeRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<LikeRemoteDataSource>()
        repository = LikeRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `isBookLiked success delegates to remote with parsed int ID`() = runTest {
        everySuspend { remoteDataSource.getLikeStatus(42) } returns Result.success(true)

        val result = repository.isBookLiked("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        verifySuspend { remoteDataSource.getLikeStatus(42) }
    }

    @Test
    fun `isBookLiked returns failure for invalid book ID`() = runTest {
        val result = repository.isBookLiked("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `toggleLike success delegates to remote with parsed int ID`() = runTest {
        everySuspend { remoteDataSource.toggleLike(any()) } returns Result.success(true)

        val result = repository.toggleLike("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        verifySuspend { remoteDataSource.toggleLike(LikeToggleRequestDto(bookId = 42)) }
    }

    @Test
    fun `toggleLike returns failure for invalid book ID`() = runTest {
        val result = repository.toggleLike("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
