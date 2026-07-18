package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
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

class ShelfRepositoryTest {

    private lateinit var remoteDataSource: ShelfRemoteDataSource
    private lateinit var repository: ShelfRepository

    private val testShelfEntry = ShelfEntry(
        shelf = ShelfStatus.READ,
        book = Book(id = "5", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ShelfRemoteDataSource>()
        repository = ShelfRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getShelf success delegates to remote`() = runTest {
        val expected = listOf(testShelfEntry)
        everySuspend { remoteDataSource.getShelf() } returns Result.success(expected)

        val result = repository.getShelf()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        verifySuspend { remoteDataSource.getShelf() }
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
    fun `getShelfStatus returns failure for invalid book ID`() = runTest {
        val result = repository.getShelfStatus("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `assignShelf success delegates to remote`() = runTest {
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
    }

    @Test
    fun `assignShelf returns failure for invalid book ID`() = runTest {
        val result = repository.assignShelf("invalid", ShelfStatus.READ)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `removeFromShelf success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.removeShelf(5) } returns Result.success(Unit)

        val result = repository.removeFromShelf("5")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.removeShelf(5) }
    }

    @Test
    fun `removeFromShelf returns failure for invalid book ID`() = runTest {
        val result = repository.removeFromShelf("invalid")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
