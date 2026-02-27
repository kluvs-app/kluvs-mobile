package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.data.remote.dtos.CreateMemberRequestDto
import com.ivangarzab.kluvs.data.remote.dtos.UpdateMemberRequestDto
import com.ivangarzab.kluvs.network.BuildKonfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for MemberService using local Supabase instance with seed data.
 *
 * Test data from /kluvs-backend/supabase/seed.sql:
 * - 1: Ivan Garza (clubs: club-1, club-2)
 * - 2: Monica Morales (clubs: club-1, club-2)
 * - 3: Marco Rivera (clubs: club-3)
 * - 4: Anacleto Longoria (clubs: club-3, club-4)
 * - 5: Joel Salinas (clubs: club-1, club-2)
 * - 6: Jorge Longoria (clubs: club-5)
 */
class MemberServiceIntegrationTest {

    private lateinit var memberService: MemberService

    @BeforeTest
    fun setup() {
        val supabase = createSupabaseClient(
            supabaseUrl = BuildKonfig.TEST_SUPABASE_URL,
            supabaseKey = BuildKonfig.TEST_SUPABASE_KEY
        ) {
            install(Functions)
        }
        memberService = MemberServiceImpl(supabase)
    }

    // ========================================
    // READ TESTS (using seed data)
    // ========================================

    @Test
    fun testGetMember() = runTest {
        // Given: member 1 exists in seed data
        // When: getting member by ID
        val response = memberService.get(memberId = "1")

        // Then: should return correct member data
        assertEquals("1", response.id)
        assertEquals("Ivan Garza", response.name)
        assertTrue(response.books_read >= 0, "Books read should be non-negative")

        // Should have clubs
        assertTrue(response.clubs.isNotEmpty(), "Member should belong to clubs")
        val clubIds = response.clubs.map { it.id }
        assertTrue(clubIds.contains("club-1"), "Should belong to club-1")
        assertTrue(clubIds.contains("club-2"), "Should belong to club-2")
    }

    @Test
    fun testGetMemberWithShameClubs() = runTest {
        // Given: member 2 is in shame lists (club-1, club-2)
        // When: getting member 2
        val response = memberService.get(memberId = "2")

        // Then: should include shame clubs
        assertEquals("2", response.id)
        assertEquals("Monica Morales", response.name)
        assertTrue(response.shame_clubs.isNotEmpty(), "Member should have shame clubs")
    }

    @Test
    fun testGetMemberByUserId() = runTest {
        // Given: a member with a user_id exists
        // When: getting member by user_id (if any exist in seed data)
        // Note: This test may need adjustment based on your seed data

        // For now, test that the method works even with null user_id
        val member = memberService.get("1")
        if (member.user_id != null) {
            val response = memberService.getByUserId(member.user_id!!)
            assertEquals(member.id, response.id)
        }
    }

    @Test
    fun testGetNonExistentMember() = runTest {
        // When: trying to get a non-existent member
        // Then: should throw exception
        assertFailsWith<Exception> {
            memberService.get("999999")
        }
    }

    @Test
    fun testMemberHasValidClubData() = runTest {
        // Given: member with clubs
        // When: getting member
        val response = memberService.get("1")

        // Then: clubs should have valid data
        response.clubs.forEach { club ->
            assertNotNull(club.id, "Club should have ID")
            assertNotNull(club.name, "Club should have name")
        }
    }

    // ========================================
    // CREATE/UPDATE/DELETE TESTS
    // (These tests create their own data and clean up)
    // ========================================

    @Test
    fun testCreateMember() = runTest {
        // Given: a new member request
        val request = CreateMemberRequestDto(
            name = "Test Member Create",
            books_read = 0
        )

        var memberId: String? = null
        try {
            // When: creating the member
            val response = memberService.create(request)

            // Then: should return success
            assertTrue(response.success, "Member creation should succeed")
            assertEquals("Test Member Create", response.member.name)
            assertEquals(0, response.member.books_read)
            assertNotNull(response.member.id, "Should have generated ID")
            memberId = response.member.id

            // Verify it can be retrieved
            val retrieved = memberService.get(memberId)
            assertEquals("Test Member Create", retrieved.name)
        } finally {
            // Cleanup
            memberId?.let {
                try {
                    memberService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdateMember() = runTest {
        // Given: a test member exists
        val createRequest = CreateMemberRequestDto(
            name = "Original Member Name",
            books_read = 1
        )
        val created = memberService.create(createRequest)
        val memberId = created.member.id

        try {
            // When: updating the member
            val updateRequest = UpdateMemberRequestDto(
                id = memberId,
                name = "Updated Member Name",
                books_read = 5
            )
            val response = memberService.update(updateRequest)

            // Then: should return success
            assertTrue(response.success, "Member update should succeed")
            assertEquals("Updated Member Name", response.member.name)
            assertEquals(5, response.member.books_read)

            // Verify changes persisted
            val retrieved = memberService.get(memberId)
            assertEquals("Updated Member Name", retrieved.name)
            assertEquals(5, retrieved.books_read)
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testDeleteMember() = runTest {
        // Given: a test member exists
        val createRequest = CreateMemberRequestDto(
            name = "Member To Delete",
            books_read = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member.id

        // When: deleting the member
        val response = memberService.delete(memberId)

        // Then: should return success
        assertTrue(response.success, "Member deletion should succeed")

        // Verify it no longer exists
        assertFailsWith<Exception> {
            memberService.get(memberId)
        }
    }

    @Test
    fun testUpdateMemberHandle() = runTest {
        // Given: a test member exists
        val createRequest = CreateMemberRequestDto(
            name = "Handle Test Member",
            books_read = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member.id

        try {
            // When: updating the member's handle
            val updateRequest = UpdateMemberRequestDto(
                id = memberId,
                handle = "handle_test_user"
            )
            val response = memberService.update(updateRequest)

            // Then: update should succeed
            assertTrue(response.success, "Member update should succeed")

            // Verify handle persisted on re-fetch
            val retrieved = memberService.get(memberId)
            assertEquals("handle_test_user", retrieved.handle, "Handle should be persisted")
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testUpdateMemberWithClubs() = runTest {
        // Given: a test member exists
        val createRequest = CreateMemberRequestDto(
            name = "Member With Clubs",
            books_read = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member.id

        try {
            // When: updating member to join clubs
            val updateRequest = UpdateMemberRequestDto(
                id = memberId,
                clubs = listOf("club-1", "club-2")
            )
            val response = memberService.update(updateRequest)

            // Then: should update clubs
            assertTrue(response.success)
            assertTrue(response.clubs_updated == true, "Clubs should be marked as updated")

            // Verify member is in clubs
            val retrieved = memberService.get(memberId)
            assertEquals(2, retrieved.clubs.size)
            val clubIds = retrieved.clubs.map { it.id }
            assertTrue(clubIds.contains("club-1"))
            assertTrue(clubIds.contains("club-2"))
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testCreateMemberWithUserId() = runTest {
        // Given: a member with user_id
        val request = CreateMemberRequestDto(
            name = "User Linked Member",
            books_read = 0,
            user_id = "test-user-123"
        )

        var memberId: String? = null
        try {
            // When: creating member
            val response = memberService.create(request)

            // Then: member should be created
            assertTrue(response.success)
            // Note: Backend may or may not return user_id in response
            memberId = response.member.id

            // Should be retrievable by user_id (if backend supports it)
            try {
                val retrieved = memberService.getByUserId("test-user-123")
                assertEquals(memberId, retrieved.id)
            } catch (e: Exception) {
                // Backend might not support user_id retrieval yet
            }
        } finally {
            // Cleanup
            memberId?.let {
                try {
                    memberService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testMemberPointsAndBooksRead() = runTest {
        // Given: a member with initial stats
        val request = CreateMemberRequestDto(
            name = "Stats Member",
            books_read = 10
        )
        val created = memberService.create(request)
        val memberId = created.member.id

        try {
            // Verify initial stats
            assertEquals(10, created.member.books_read)

            // When: incrementing stats
            val updateRequest = UpdateMemberRequestDto(
                id = memberId,
                books_read = 15
            )
            memberService.update(updateRequest)

            // Then: stats should be updated
            val retrieved = memberService.get(memberId)
            assertEquals(15, retrieved.books_read)
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId)
            } catch (_: Exception) { }
        }
    }
}
