package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignShelfUseCaseTest {

    private lateinit var shelfRepository: ShelfRepository
    private lateinit var useCase: AssignShelfUseCase

    @BeforeTest
    fun setup() {
        shelfRepository = mock<ShelfRepository>()
        useCase = AssignShelfUseCase(shelfRepository)
    }

    @Test
    fun `invoke returns assigned shelf from repository`() = runTest {
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.success(ShelfStatus.READ)

        val result = useCase("42", ShelfStatus.READ)

        assertTrue(result.isSuccess)
        assertEquals(ShelfStatus.READ, result.getOrNull())
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.failure(exception)

        val result = useCase("42", ShelfStatus.READ)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
