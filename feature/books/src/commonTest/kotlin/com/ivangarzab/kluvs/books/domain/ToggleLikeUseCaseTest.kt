package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.kluvs.data.repositories.LikeRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToggleLikeUseCaseTest {

    private lateinit var likeRepository: LikeRepository
    private lateinit var useCase: ToggleLikeUseCase

    @BeforeTest
    fun setup() {
        likeRepository = mock<LikeRepository>()
        useCase = ToggleLikeUseCase(likeRepository)
    }

    @Test
    fun `invoke returns new liked state from repository`() = runTest {
        everySuspend { likeRepository.toggleLike(any()) } returns Result.success(true)

        val result = useCase("42")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { likeRepository.toggleLike(any()) } returns Result.failure(exception)

        val result = useCase("42")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
