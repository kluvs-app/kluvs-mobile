package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetShelfUseCaseTest {

    private lateinit var shelfRepository: ShelfRepository
    private lateinit var useCase: GetShelfUseCase

    private val testEntry = ShelfEntry(
        shelf = ShelfStatus.CURRENTLY_READING,
        book = Book(id = "42", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = "978-0-395-07122-1")
    )

    @BeforeTest
    fun setup() {
        shelfRepository = mock<ShelfRepository>()
        useCase = GetShelfUseCase(shelfRepository)
    }

    @Test
    fun `invoke returns shelf entries from repository`() = runTest {
        everySuspend { shelfRepository.getShelf() } returns Result.success(listOf(testEntry))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("The Hobbit", result.getOrNull()?.first()?.book?.title)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { shelfRepository.getShelf() } returns Result.failure(exception)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
