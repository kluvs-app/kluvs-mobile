package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for [LikeService] using local Supabase instance with seed data.
 *
 * The like endpoint is member-scoped (user JWT only), so all calls run as the
 * seeded auth user Ivan Garza (member 1).
 *
 * Ivan's seeded likes (/kluvs-backend/supabase/seed.sql): books 1, 4, 7.
 *
 * Mutating tests restore Ivan's seeded like state before finishing.
 */
class LikeServiceIntegrationTest {

    private suspend fun likeService(): LikeService =
        LikeServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testGetLikeStatusForLikedBook() = runTest {
        // Given: book 1 (The Republic) is seeded as liked
        val response = likeService().getStatus(1)

        // Then: status is liked
        assertTrue(response.success == true)
        assertEquals(true, response.liked)
    }

    @Test
    fun testGetLikeStatusForUnlikedBook() = runTest {
        // Given: book 2 (Nicomachean Ethics) is not liked in seed data
        val response = likeService().getStatus(2)

        // Then: status is not liked
        assertTrue(response.success == true)
        assertEquals(false, response.liked)
    }

    @Test
    fun testToggleLike() = runTest {
        val service = likeService()
        try {
            // Given: book 5 (Brave New World) is not liked in seed data
            // When: toggling the like on
            val likeResponse = service.toggle(LikeToggleRequestDto(bookId = 5))

            // Then: the book is liked
            assertTrue(likeResponse.success == true)
            assertEquals(true, likeResponse.liked)
            assertEquals(true, service.getStatus(5).liked)

            // When: toggling again
            val unlikeResponse = service.toggle(LikeToggleRequestDto(bookId = 5))

            // Then: the like is removed
            assertTrue(unlikeResponse.success == true)
            assertEquals(false, unlikeResponse.liked)
            assertEquals(false, service.getStatus(5).liked)
        } finally {
            // Cleanup: ensure book 5 ends unliked (its seeded state)
            try {
                if (service.getStatus(5).liked == true) {
                    service.toggle(LikeToggleRequestDto(bookId = 5))
                }
            } catch (_: Exception) { }
        }
    }
}
