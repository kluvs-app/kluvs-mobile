package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoveFromShelfUseCaseTest {

    private lateinit var shelfRepository: ShelfRepository
    private lateinit var useCase: RemoveFromShelfUseCase

    @BeforeTest
    fun setup() {
        shelfRepository = mock<ShelfRepository>()
        useCase = RemoveFromShelfUseCase(shelfRepository)
    }

    @Test
    fun `invoke returns success from repository`() = runTest {
        everySuspend { shelfRepository.removeFromShelf(any()) } returns Result.success(Unit)

        val result = useCase("42")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { shelfRepository.removeFromShelf(any()) } returns Result.failure(exception)

        val result = useCase("42")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
