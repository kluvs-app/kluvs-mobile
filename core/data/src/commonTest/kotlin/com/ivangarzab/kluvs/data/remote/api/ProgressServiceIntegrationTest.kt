package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for [ProgressService] using local Supabase instance.
 *
 * The progress endpoint is member-scoped (user JWT only), so all calls run as the
 * seeded auth user Ivan Garza (member 1).
 *
 * No reading progress is seeded — every test creates its own entries and cleans up.
 */
class ProgressServiceIntegrationTest {

    private suspend fun progressService(): ProgressService =
        ProgressServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testCreatePageProgress() = runTest {
        val service = progressService()
        var progressId: String? = null
        try {
            // When: creating page-based progress on book 5
            val progress = service.create(
                ProgressCreateRequestDto(
                    bookId = 5,
                    progressType = ProgressCreateRequestDto.ProgressType.page,
                    currentPage = 50,
                )
            )
            progressId = progress.id

            // Then: the entry is created for the authenticated member
            assertEquals(TEST_USER_MEMBER_ID, progress.memberId)
            assertEquals(5, progress.bookId)
            assertEquals(ReadingProgressDto.ProgressType.page, progress.progressType)
            assertEquals(50, progress.currentPage)
            assertEquals(ReadingProgressDto.Status.in_progress, progress.status)

            // And: it shows up in the list filtered by book
            val list = service.getAll(bookId = 5)
            assertTrue(list.any { it.id == progressId },
                "Created entry should appear in the progress list")
        } finally {
            progressId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdatePercentProgress() = runTest {
        val service = progressService()
        var progressId: String? = null
        try {
            // Given: percent-based progress on book 7
            val created = service.create(
                ProgressCreateRequestDto(
                    bookId = 7,
                    progressType = ProgressCreateRequestDto.ProgressType.percent,
                    percentComplete = 25f,
                )
            )
            progressId = created.id

            // When: bumping the percentage
            val updated = service.update(
                ProgressUpdateRequestDto(
                    id = progressId,
                    progressType = ProgressUpdateRequestDto.ProgressType.percent,
                    percentComplete = 80f,
                )
            )

            // Then: the update persists
            assertEquals(80f, updated.percentComplete)
        } finally {
            progressId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testCompleteProgress() = runTest {
        val service = progressService()
        var progressId: String? = null
        try {
            // Given: page-based progress on book 8
            val created = service.create(
                ProgressCreateRequestDto(
                    bookId = 8,
                    progressType = ProgressCreateRequestDto.ProgressType.page,
                    currentPage = 100,
                )
            )
            progressId = created.id

            // When: marking it completed
            val updated = service.update(
                ProgressUpdateRequestDto(
                    id = progressId,
                    progressType = ProgressUpdateRequestDto.ProgressType.page,
                    status = ProgressUpdateRequestDto.Status.completed,
                )
            )

            // Then: the entry is completed with a completion timestamp
            assertEquals(ReadingProgressDto.Status.completed, updated.status)
            assertNotNull(updated.completedAt, "Completed entry should have completed_at")
        } finally {
            progressId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testDeleteProgress() = runTest {
        val service = progressService()

        // Given: a progress entry on book 6
        val created = service.create(
            ProgressCreateRequestDto(
                bookId = 6,
                progressType = ProgressCreateRequestDto.ProgressType.page,
                currentPage = 10,
            )
        )

        // When: deleting it
        val response = service.delete(created.id)

        // Then: it is gone from the list
        assertTrue(response.success == true, "Progress deletion should succeed")
        val list = service.getAll(bookId = 6)
        assertTrue(list.none { it.id == created.id },
            "Deleted entry should not appear in the progress list")
    }
}
