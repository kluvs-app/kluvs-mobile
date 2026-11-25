package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.api.models.ShelfAssignResponseDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfSingleResponseDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.data.remote.api.ShelfService
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShelfRemoteDataSourceTest {

    private lateinit var shelfService: ShelfService
    private lateinit var dataSource: ShelfRemoteDataSource

    @BeforeTest
    fun setup() {
        shelfService = mock<ShelfService>()
        dataSource = ShelfRemoteDataSourceImpl(shelfService)
    }

    @Test
    fun `getShelf maps entries to domain`() = runTest {
        // Given: Service returns one complete entry
        everySuspend { shelfService.getAll() } returns ShelfListResponseDto(
            success = true,
            shelves = listOf(
                ShelfEntryDto(
                    updatedAt = "2026-01-01T00:00:00Z",
                    finishedAt = "2026-01-01T00:00:00Z",
                    source = ShelfEntryDto.Source.manual,
                    shelf = ShelfStatusDto.read,
                    book = BookDto(id = 5, title = "The Hobbit", author = "J.R.R. Tolkien")
                )
            )
        )

        // When: Fetching the shelf
        val result = dataSource.getShelf()

        // Then: The entry is mapped to domain
        assertTrue(result.isSuccess)
        assertEquals(ShelfStatus.READ, result.getOrNull()?.first()?.shelf)
    }

    @Test
    fun `getShelf returns failure when service throws`() = runTest {
        everySuspend { shelfService.getAll() } throws Exception("Network error")

        val result = dataSource.getShelf()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getShelfStatus returns null for unshelved book`() = runTest {
        everySuspend { shelfService.getForBook(5) } returns ShelfSingleResponseDto(
            success = true,
            shelf = null
        )

        val result = dataSource.getShelfStatus(5)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `assignShelf returns the assigned shelf`() = runTest {
        val request = ShelfAssignRequestDto(bookId = 5, shelf = ShelfStatusDto.currently_reading)
        everySuspend { shelfService.assign(request) } returns ShelfAssignResponseDto(
            success = true,
            shelf = ShelfStatusDto.currently_reading
        )

        val result = dataSource.assignShelf(request)

        assertEquals(ShelfStatus.CURRENTLY_READING, result.getOrNull())
    }

    @Test
    fun `removeShelf succeeds on successful response`() = runTest {
        everySuspend { shelfService.remove(5) } returns ShelfSingleResponseDto(success = true, shelf = null)

        val result = dataSource.removeShelf(5)

        assertTrue(result.isSuccess)
    }
}
