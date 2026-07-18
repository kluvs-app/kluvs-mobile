package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for [ShelfService] using local Supabase instance with seed data.
 *
 * The shelf endpoint is member-scoped (user JWT only), so all calls run as the
 * seeded auth user Ivan Garza (member 1).
 *
 * Ivan's seeded shelves (/kluvs-backend/supabase/seed.sql):
 * - currently_reading (session): books 1 (The Republic), 4 (1984)
 * - read: book 3 (Capital, session), book 9 (Meditations, manual)
 * - want_to_read (manual): books 5, 7, 8
 * - not_finished (manual): books 2, 6
 *
 * Mutating tests restore Ivan's seeded shelf state before finishing.
 */
class ShelfServiceIntegrationTest {

    private suspend fun shelfService(): ShelfService =
        ShelfServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testGetAllShelves() = runTest {
        // When: fetching all shelf entries for Ivan
        val response = shelfService().getAll()

        // Then: all 9 seeded books are shelved
        assertTrue(response.success == true)
        val shelves = response.shelves ?: emptyList()
        assertTrue(shelves.size >= 9, "Ivan should have at least 9 shelved books")

        // Session-sourced currently_reading entry (The Republic, active session)
        val republic = shelves.find { it.book?.id == 1 }
        assertNotNull(republic, "The Republic should be shelved")
        assertEquals(ShelfStatusDto.currently_reading, republic.shelf)
        assertEquals(ShelfEntryDto.Source.session, republic.source)

        // Manually-shelved read entry (Meditations)
        val meditations = shelves.find { it.book?.id == 9 }
        assertNotNull(meditations, "Meditations should be shelved")
        assertEquals(ShelfStatusDto.read, meditations.shelf)
        assertEquals(ShelfEntryDto.Source.manual, meditations.source)

        // Every entry carries full book details
        shelves.forEach { entry ->
            assertNotNull(entry.book, "Shelf entry should include its book")
            assertNotNull(entry.book?.title, "Shelved book should have a title")
        }
    }

    @Test
    fun testGetShelfForBook() = runTest {
        // Given: book 5 (Brave New World) is seeded as want_to_read
        // When: fetching the shelf for that single book
        val response = shelfService().getForBook(5)

        // Then: returns the single-book shape with the seeded shelf value
        assertTrue(response.success == true)
        assertEquals(ShelfStatusDto.want_to_read, response.shelf)
    }

    @Test
    fun testAssignShelf() = runTest {
        val service = shelfService()
        try {
            // Given: book 2 (Nicomachean Ethics) is seeded as not_finished
            // When: moving it to read
            val response = service.assign(
                ShelfAssignRequestDto(bookId = 2, shelf = ShelfStatusDto.read)
            )

            // Then: assignment succeeds and persists
            assertTrue(response.success == true, "Shelf assignment should succeed")
            assertEquals(ShelfStatusDto.read, response.shelf)
            assertEquals(ShelfStatusDto.read, service.getForBook(2).shelf)
        } finally {
            // Cleanup: restore the seeded shelf value
            try {
                service.assign(ShelfAssignRequestDto(bookId = 2, shelf = ShelfStatusDto.not_finished))
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testRemoveShelf() = runTest {
        val service = shelfService()
        try {
            // Given: book 6 is seeded as not_finished
            // When: removing it from the shelf
            val response = service.remove(6)

            // Then: removal succeeds and the book is unshelved
            assertTrue(response.success == true, "Shelf removal should succeed")
            assertNull(response.shelf, "Removed shelf should be null")
            assertNull(service.getForBook(6).shelf, "Book should no longer be shelved")
        } finally {
            // Cleanup: restore the seeded shelf value
            try {
                service.assign(ShelfAssignRequestDto(bookId = 6, shelf = ShelfStatusDto.not_finished))
            } catch (_: Exception) { }
        }
    }
}
