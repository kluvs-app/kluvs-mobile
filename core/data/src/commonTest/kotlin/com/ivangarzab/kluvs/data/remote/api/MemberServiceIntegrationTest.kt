package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.MemberCreateRequestDto
import com.ivangarzab.kluvs.api.models.MemberUpdateRequestDto
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
 * - 1: Ivan Garza (clubs: club-owner, club-admin, club-member)
 * - 2: Monica Morales (clubs: club-owner, club-admin)
 * - 3: Marco Rivera (clubs: club-admin, club-member)
 * - 4: Anacleto Longoria (clubs: club-admin, club-member)
 * - 5: Joel Salinas (clubs: club-owner, club-admin)
 * - 6: Jorge Longoria (clubs: club-admin)
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
        assertEquals(1, response.id)
        assertEquals("Ivan Garza", response.name)
        assertTrue((response.booksRead ?: 0) >= 0, "Books read should be non-negative")

        // Should have clubs
        assertTrue(response.clubs?.isNotEmpty() == true, "Member should belong to clubs")
        val clubIds = response.clubs?.map { it.id } ?: emptyList()
        assertTrue(clubIds.contains("club-owner"), "Should belong to club-owner")
        assertTrue(clubIds.contains("club-admin"), "Should belong to club-admin")
        assertTrue(clubIds.contains("club-member"), "Should belong to club-member")
    }

    @Test
    fun testGetMemberWithShameClubs() = runTest {
        // Given: member 2 is in the club-owner shame list
        // When: getting member 2
        val response = memberService.get(memberId = "2")

        // Then: should include shame clubs
        assertEquals(2, response.id)
        assertEquals("Monica Morales", response.name)
        assertTrue(response.shameClubs?.isNotEmpty() == true, "Member should have shame clubs")
    }

    @Test
    fun testGetMemberByUserId() = runTest {
        // Given: a member with a user_id exists
        // When: getting member by user_id (if any exist in seed data)
        // Note: This test may need adjustment based on your seed data

        // For now, test that the method works even with null user_id
        val member = memberService.get("1")
        if (member.userId != null) {
            val response = memberService.getByUserId(member.userId!!)
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
        response.clubs?.forEach { club ->
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
        val request = MemberCreateRequestDto(
            name = "Test Member Create",
            booksRead = 0
        )

        var memberId: Int? = null
        try {
            // When: creating the member
            val response = memberService.create(request)

            // Then: should return success
            assertTrue(response.success == true, "Member creation should succeed")
            assertEquals("Test Member Create", response.member?.name)
            assertEquals(0, response.member?.booksRead)
            memberId = response.member?.id

            // Verify it can be retrieved
            assertNotNull(memberId)
            val retrieved = memberService.get(memberId.toString())
            assertEquals("Test Member Create", retrieved.name)
        } finally {
            // Cleanup
            memberId?.let {
                try {
                    memberService.delete(it.toString())
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdateMember() = runTest {
        // Given: a test member exists
        val createRequest = MemberCreateRequestDto(
            name = "Original Member Name",
            booksRead = 1
        )
        val created = memberService.create(createRequest)
        val memberId = created.member?.id
        assertNotNull(memberId)

        try {
            // When: updating the member
            val updateRequest = MemberUpdateRequestDto(
                id = memberId,
                name = "Updated Member Name",
                booksRead = 5
            )
            val response = memberService.update(updateRequest)

            // Then: should return success
            assertTrue(response.success == true, "Member update should succeed")
            assertEquals("Updated Member Name", response.member?.name)
            assertEquals(5, response.member?.booksRead)

            // Verify changes persisted
            val retrieved = memberService.get(memberId.toString())
            assertEquals("Updated Member Name", retrieved.name)
            assertEquals(5, retrieved.booksRead)
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId.toString())
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testDeleteMember() = runTest {
        // Given: a test member exists
        val createRequest = MemberCreateRequestDto(
            name = "Member To Delete",
            booksRead = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member?.id
        assertNotNull(memberId)

        // When: deleting the member
        val response = memberService.delete(memberId.toString())

        // Then: should return success
        assertTrue(response.success, "Member deletion should succeed")

        // Verify it no longer exists
        assertFailsWith<Exception> {
            memberService.get(memberId.toString())
        }
    }

    @Test
    fun testUpdateMemberHandle() = runTest {
        // Given: a test member exists
        val createRequest = MemberCreateRequestDto(
            name = "Handle Test Member",
            booksRead = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member?.id
        assertNotNull(memberId)

        try {
            // When: updating the member's handle
            val updateRequest = MemberUpdateRequestDto(
                id = memberId,
                handle = "handle_test_user"
            )
            val response = memberService.update(updateRequest)

            // Then: update should succeed
            assertTrue(response.success == true, "Member update should succeed")

            // Verify handle persisted on re-fetch
            val retrieved = memberService.get(memberId.toString())
            assertEquals("handle_test_user", retrieved.handle, "Handle should be persisted")
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId.toString())
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testUpdateMemberWithClubs() = runTest {
        // Given: a test member exists
        val createRequest = MemberCreateRequestDto(
            name = "Member With Clubs",
            booksRead = 0
        )
        val created = memberService.create(createRequest)
        val memberId = created.member?.id
        assertNotNull(memberId)

        try {
            // When: updating member to join clubs
            // Note: a member field (name) is included alongside clubs because the backend
            // returns a partial member ({ id } only) on clubs-only updates, which the
            // spec-generated MemberDto cannot deserialize. Known backend/spec gap.
            val updateRequest = MemberUpdateRequestDto(
                id = memberId,
                name = "Member With Clubs",
                clubs = listOf("club-owner", "club-member")
            )
            val response = memberService.update(updateRequest)

            // Then: should update clubs
            assertTrue(response.success == true)
            assertTrue(response.clubsUpdated == true, "Clubs should be marked as updated")

            // Verify member is in clubs
            val retrieved = memberService.get(memberId.toString())
            assertEquals(2, retrieved.clubs?.size)
            val clubIds = retrieved.clubs?.map { it.id } ?: emptyList()
            assertTrue(clubIds.contains("club-owner"))
            assertTrue(clubIds.contains("club-member"))
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId.toString())
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testCreateMemberWithDiscordId() = runTest {
        // Given: a member with a Discord ID (the generated POST schema has no
        // field for setting a Supabase Auth user_id at creation time — that's
        // populated later via the OAuth link flow)
        val request = MemberCreateRequestDto(
            name = "Discord Linked Member",
            booksRead = 0,
            discordId = "test-discord-123"
        )

        var memberId: Int? = null
        try {
            // When: creating member
            val response = memberService.create(request)

            // Then: member should be created
            assertTrue(response.success == true)
            memberId = response.member?.id
        } finally {
            // Cleanup
            memberId?.let {
                try {
                    memberService.delete(it.toString())
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testMemberPointsAndBooksRead() = runTest {
        // Given: a member with initial stats
        val request = MemberCreateRequestDto(
            name = "Stats Member",
            booksRead = 10
        )
        val created = memberService.create(request)
        val memberId = created.member?.id
        assertNotNull(memberId)

        try {
            // Verify initial stats
            assertEquals(10, created.member?.booksRead)

            // When: incrementing stats
            val updateRequest = MemberUpdateRequestDto(
                id = memberId,
                booksRead = 15
            )
            memberService.update(updateRequest)

            // Then: stats should be updated
            val retrieved = memberService.get(memberId.toString())
            assertEquals(15, retrieved.booksRead)
        } finally {
            // Cleanup
            try {
                memberService.delete(memberId.toString())
            } catch (_: Exception) { }
        }
    }
}
