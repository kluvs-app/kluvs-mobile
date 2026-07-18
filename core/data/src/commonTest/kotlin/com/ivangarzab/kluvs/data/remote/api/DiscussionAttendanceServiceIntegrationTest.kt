package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for [DiscussionAttendanceService] using local Supabase instance
 * with seed data.
 *
 * The discussion-attendance endpoint is member-scoped (user JWT only), so all calls
 * run as the seeded auth user Ivan Garza (member 1).
 *
 * Seeded RSVPs (/kluvs-backend/supabase/seed.sql):
 * - disc-owner-past-1: Ivan yes, Monica yes, Joel no
 * - disc-owner-past-2: Ivan yes, Monica maybe
 * - disc-owner-upcoming: Ivan yes
 * - disc-owner-future: intentionally unanswered (used by the mutation test)
 */
class DiscussionAttendanceServiceIntegrationTest {

    private suspend fun attendanceService(): DiscussionAttendanceService =
        DiscussionAttendanceServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testGetRoster() = runTest {
        // Given: disc-owner-past-1 has three seeded RSVPs
        val roster = attendanceService().getRoster("disc-owner-past-1")

        // Then: all three responses are present with the caller's own status
        assertEquals(3, roster.responses.size, "disc-owner-past-1 should have 3 RSVPs")
        assertEquals(DiscussionAttendanceRosterResponseDto.MyStatus.yes, roster.myStatus)
        assertTrue(roster.totalMembers >= 3, "club-owner has at least 3 members")

        val ivan = roster.responses.find { it.memberId == TEST_USER_MEMBER_ID }
        assertNotNull(ivan, "Ivan's RSVP should be in the roster")
        assertEquals(DiscussionAttendanceRosterEntryDto.Status.yes, ivan.status)

        val joel = roster.responses.find { it.memberId == 5 }
        assertNotNull(joel, "Joel's RSVP should be in the roster")
        assertEquals(DiscussionAttendanceRosterEntryDto.Status.no, joel.status)
    }

    @Test
    fun testUpsertAndClearRsvp() = runTest {
        val service = attendanceService()
        try {
            // Given: disc-owner-future is seeded with no RSVPs
            assertNull(service.getRoster("disc-owner-future").myStatus,
                "disc-owner-future should start unanswered")

            // When: RSVPing maybe
            val response = service.upsert(
                DiscussionAttendanceUpsertRequestDto(
                    discussionId = "disc-owner-future",
                    status = DiscussionAttendanceUpsertRequestDto.Status.maybe,
                )
            )

            // Then: the RSVP is recorded for the authenticated member
            assertEquals("disc-owner-future", response.discussionId)
            assertEquals(TEST_USER_MEMBER_ID, response.memberId)
            assertEquals(DiscussionAttendanceDto.Status.maybe, response.status)
            assertEquals(DiscussionAttendanceRosterResponseDto.MyStatus.maybe,
                service.getRoster("disc-owner-future").myStatus)

            // When: changing the answer (upsert semantics)
            val updated = service.upsert(
                DiscussionAttendanceUpsertRequestDto(
                    discussionId = "disc-owner-future",
                    status = DiscussionAttendanceUpsertRequestDto.Status.yes,
                )
            )
            assertEquals(DiscussionAttendanceDto.Status.yes, updated.status)

            // When: clearing the RSVP (backend responds 204)
            service.clear("disc-owner-future")

            // Then: the discussion is unanswered again
            assertNull(service.getRoster("disc-owner-future").myStatus,
                "Cleared RSVP should leave the discussion unanswered")
        } finally {
            // Cleanup: restore the seeded unanswered state
            try { service.clear("disc-owner-future") } catch (_: Exception) { }
        }
    }
}
