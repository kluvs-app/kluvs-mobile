package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.LikeStatusResponseDto
import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import com.ivangarzab.kluvs.api.models.LikeToggleResponseDto
import com.ivangarzab.kluvs.data.remote.api.LikeService
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LikeRemoteDataSourceTest {

    private lateinit var likeService: LikeService
    private lateinit var dataSource: LikeRemoteDataSource

    @BeforeTest
    fun setup() {
        likeService = mock<LikeService>()
        dataSource = LikeRemoteDataSourceImpl(likeService)
    }

    @Test
    fun `getLikeStatus returns liked flag`() = runTest {
        everySuspend { likeService.getStatus(5) } returns LikeStatusResponseDto(
            success = true,
            liked = true
        )

        val result = dataSource.getLikeStatus(5)

        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `getLikeStatus fails when liked flag is missing`() = runTest {
        everySuspend { likeService.getStatus(5) } returns LikeStatusResponseDto(success = true)

        val result = dataSource.getLikeStatus(5)

        assertTrue(result.isFailure)
    }

    @Test
    fun `toggleLike returns new liked state`() = runTest {
        val request = LikeToggleRequestDto(bookId = 5)
        everySuspend { likeService.toggle(request) } returns LikeToggleResponseDto(
            success = true,
            liked = false
        )

        val result = dataSource.toggleLike(request)

        assertEquals(false, result.getOrNull())
    }

    @Test
    fun `toggleLike returns failure when service throws`() = runTest {
        val request = LikeToggleRequestDto(bookId = 5)
        everySuspend { likeService.toggle(request) } throws Exception("Network error")

        val result = dataSource.toggleLike(request)

        assertTrue(result.isFailure)
    }
}
