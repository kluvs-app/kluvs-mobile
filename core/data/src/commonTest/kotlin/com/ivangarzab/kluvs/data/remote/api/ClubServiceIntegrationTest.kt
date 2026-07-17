package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.network.BuildKonfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for ClubDataSource using local Supabase instance with seed data.
 *
 * Test data is defined in /kluvs-backend/supabase/seed.sql:
 *
 * Servers:
 * - 1039326367428395038: Production Server
 * - 1234567890123456789: Test Server Alpha
 * - 987654321098765432: Test Server Beta
 *
 * Clubs:
 * - club-1: "Freaks & Geeks" (channel: 987654321098765432, server: 1039326367428395038)
 * - club-2: "Blingers Pilingers" (channel: 876543210987654321, server: 1039326367428395038)
 * - club-3: "Trifecta" (channel: 765432109876543210, server: 1234567890123456789)
 * - club-4: "Mystery Readers" (channel: 555666777888999000, server: 1234567890123456789)
 * - club-5: "Sci-Fi Enthusiasts" (channel: 111222333444555666, server: 987654321098765432)
 *
 * Members (sample):
 * - 1: Ivan Garza (clubs: club-1, club-2)
 * - 2: Monica Morales (clubs: club-1, club-2)
 * - 3: Marco Rivera (clubs: club-3)
 * - 4: Anacleto Longoria (clubs: club-3, club-4)
 * - 5: Joel Salinas (clubs: club-1, club-2)
 * - 6: Jorge Longoria (clubs: club-5)
 *
 * Shame Lists:
 * - club-1: [2, 5]
 * - club-2: [1, 2]
 * - club-3: [4]
 * - club-4: [7]
 * - club-5: [6]
 */
class ClubServiceIntegrationTest {

    private lateinit var clubService: ClubService

    // Test server IDs from seed.sql
    private val productionServerId = "1039326367428395038"
    private val testServerAlphaId = "1234567890123456789"
    private val testServerBetaId = "987654321098765432"

    @BeforeTest
    fun setup() {
        val url = BuildKonfig.TEST_SUPABASE_URL
        val key = BuildKonfig.TEST_SUPABASE_KEY
        println("DEBUG: TEST_SUPABASE_URL = $url")
        println("DEBUG: TEST_SUPABASE_KEY = $key")

        val supabase = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key,
        ) {
            // Set custom serializer for all Supabase operations
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = true
            })

            install(Functions)
        }

        clubService = ClubServiceImpl(supabase)
    }

    @Test
    fun testGetClub() = runTest {
        // Given: club-1 exists in seed data with known values
        // When: getting club-1 from Production Server
        val response = clubService.get(
            clubId = "club-1",
            serverId = productionServerId
        )

        // Then: should return correct club data
        assertEquals("club-1", response.id)
        assertEquals("Freaks & Geeks", response.name)
        assertEquals("1039326367973642363", response.discordChannel)
        assertEquals(productionServerId, response.serverId)

        // Should have members (from MemberClubs table)
        assertTrue((response.members?.size ?: 0) >= 3, "club-1 should have at least 3 members")

        // Should have shame list
        assertEquals(2, response.shameList?.size, "club-1 shame list should have 2 members")
        assertTrue(response.shameList?.contains(2) == true, "Member 2 should be in shame list")
        assertTrue(response.shameList?.contains(5) == true, "Member 5 should be in shame list")
    }

    @Test
    fun testGetClubFromDifferentServer() = runTest {
        // Given: club-3 exists on Test Server Alpha
        // When: getting club-3
        val response = clubService.get(
            clubId = "club-3",
            serverId = testServerAlphaId
        )

        // Then: should return correct club data
        assertEquals("club-3", response.id)
        assertEquals("Trifecta", response.name)
        assertEquals("765432109876543210", response.discordChannel)
        assertEquals(testServerAlphaId, response.serverId)
    }

    @Test
    fun testGetClubWithSession() = runTest {
        // Given: club-1 has active sessions in seed data
        // When: getting club-1
        val response = clubService.get(
            clubId = "club-1",
            serverId = productionServerId
        )

        // Then: should have session data
        // Note: active_session vs past_sessions depends on due_date
        val totalSessions = (if (response.activeSession != null) 1 else 0) + (response.pastSessions?.size ?: 0)
        assertTrue(totalSessions >= 1, "club-1 should have at least one session")

        if (response.activeSession != null) {
            assertNotNull(response.activeSession?.book)
            assertNotNull(response.activeSession?.dueDate)
        }
    }

    @Test
    fun testGetClubWithMembers() = runTest {
        // Given: club-1 has multiple members
        // When: getting club-1
        val response = clubService.get(
            clubId = "club-1",
            serverId = productionServerId
        )

        // Then: should include all members
        assertTrue((response.members?.size ?: 0) >= 3, "club-1 should have at least 3 members")

        // Check specific members exist
        val memberNames = response.members?.map { it.name } ?: emptyList()
        assertTrue(memberNames.contains("Ivan Garza"), "Should include Ivan Garza")
        assertTrue(memberNames.contains("Monica Morales"), "Should include Monica Morales")
        assertTrue(memberNames.contains("Joel Salinas"), "Should include Joel Salinas")

        // All members should have valid IDs
        response.members?.forEach { clubMember ->
            assertNotNull(clubMember.id, "Member should have ID")
        }
    }

    @Test
    fun testGetClubByDiscordChannel() = runTest {
        // Given: club-2 has discord channel 876543210987654321
        // When: getting club by channel
        val response = clubService.getByChannel(
            channel = "876543210987654321",
            serverId = productionServerId
        )

        // Then: should return club-2
        assertEquals("club-2", response.id)
        assertEquals("Blingers Pilingers", response.name)
        assertEquals("876543210987654321", response.discordChannel)
    }

    @Test
    fun testGetNonExistentClub() = runTest {
        // When: trying to get a non-existent club
        // Then: should throw an exception
        assertFailsWith<Exception> {
            clubService.get("non-existent-club-id", productionServerId)
        }
    }

    @Test
    fun testGetClubWithWrongServerId() = runTest {
        // Given: club-1 exists on Production Server
        // When: trying to get it with wrong server ID
        // Then: should throw an exception or return error
        assertFailsWith<Exception> {
            clubService.get("club-1", "999999999999999999")
        }
    }

    @Test
    fun testGetClubFromWrongServer() = runTest {
        // Given: club-1 is on Production Server (1039326367428395038)
        // When: trying to get it from Test Server Alpha
        // Then: should fail (club doesn't exist on that server)
        assertFailsWith<Exception> {
            clubService.get("club-1", testServerAlphaId)
        }
    }

    @Test
    fun testMultipleClubsOnSameServer() = runTest {
        // Given: Production Server has multiple clubs
        // When: getting different clubs from same server
        val club1 = clubService.get("club-1", productionServerId)
        val club2 = clubService.get("club-2", productionServerId)

        // Then: should get different clubs
        assertEquals("club-1", club1.id)
        assertEquals("Freaks & Geeks", club1.name)

        assertEquals("club-2", club2.id)
        assertEquals("Blingers Pilingers", club2.name)

        // They should be on the same server
        assertEquals(productionServerId, club1.serverId)
        assertEquals(productionServerId, club2.serverId)
    }

    @Test
    fun testClubShameListIsIntegerList() = runTest {
        // Given: club-2 has shame list [1, 2]
        // When: getting club-2
        val response = clubService.get("club-2", productionServerId)

        // Then: shame list should contain member IDs as integers
        assertEquals(2, response.shameList?.size)
        assertTrue(response.shameList?.contains(1) == true)
        assertTrue(response.shameList?.contains(2) == true)
    }

    @Test
    fun testDiscordChannelIsBigint() = runTest {
        // Given: club-1 has discord_channel as bigint
        // When: getting club-1
        val response = clubService.get("club-1", productionServerId)

        // Then: discord_channel should be a valid Discord Snowflake (large number as string)
        assertNotNull(response.discordChannel)
        assertEquals("1039326367973642363", response.discordChannel)

        // Should be convertible to Long
        assertTrue(response.discordChannel!!.toLongOrNull() != null,
            "Discord channel should be a valid long/bigint")
        assertTrue(response.discordChannel!!.toLong() > 1_000_000_000_000L,
            "Discord channel should be a large snowflake ID")
    }

    @Test
    fun testServerIdIsBigint() = runTest {
        // Given: all clubs have server_id as bigint
        // When: getting any club
        val response = clubService.get("club-1", productionServerId)

        // Then: server_id should be a valid Discord Snowflake
        assertEquals(productionServerId, response.serverId)

        // Should be convertible to Long (server_id is non-nullable from schema)
        assertTrue(response.serverId?.toLongOrNull() != null,
            "Server ID should be a valid long/bigint")
        assertTrue((response.serverId?.toLong() ?: 0L) > 1_000_000_000_000L,
            "Server ID should be a large snowflake ID")
    }

    @Test
    fun testMemberIdsAreIntegers() = runTest {
        // Given: members have integer IDs
        // When: getting a club with members
        val response = clubService.get("club-1", productionServerId)

        // Then: all member IDs should be valid integers
        assertTrue(response.members?.isNotEmpty() == true, "Club should have members")
        response.members?.forEach { clubMember ->
            assertTrue((clubMember.id ?: 0) > 0, "Member ID should be positive")
        }
    }

    // ========================================
    // CREATE/UPDATE/DELETE TESTS
    // (These tests create their own data and clean up after themselves)
    // ========================================

    @Test
    fun testCreateClub() = runTest {
        // Given: a new club request (the generated request schema doesn't support an explicit ID)
        val request = ClubCreateRequestDto(
            name = "Test Create Club",
            serverId = productionServerId,
            discordChannel = "999999999999999999"
        )

        var clubId: String? = null
        try {
            // When: creating the club
            val response = clubService.create(request)

            // Then: should return success
            assertTrue(response.success == true, "Club creation should succeed")
            assertEquals("Test Create Club", response.club?.name)
            assertEquals(productionServerId, response.club?.serverId)
            clubId = response.club?.id

            // Verify it can be retrieved
            assertNotNull(clubId)
            val retrieved = clubService.get(clubId, productionServerId)
            assertEquals("Test Create Club", retrieved.name)
        } finally {
            // Cleanup: delete the test club
            clubId?.let {
                try {
                    clubService.delete(it, productionServerId)
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    @Test
    fun testUpdateClub() = runTest {
        // Given: a test club exists
        val createRequest = ClubCreateRequestDto(
            name = "Original Name",
            discordChannel = "888888888888888888",
            serverId = productionServerId
        )
        val created = clubService.create(createRequest)
        val clubId = created.club?.id
        assertNotNull(clubId)

        try {
            // When: updating the club
            val updateRequest = ClubUpdateRequestDto(
                id = clubId,
                serverId = productionServerId,
                name = "Updated Name"
            )
            val response = clubService.update(updateRequest)

            // Then: should return success with updated flag
            assertTrue(response.success == true, "Club update should succeed")
            assertTrue(response.clubUpdated == true, "Club should be marked as updated")
            assertEquals("Updated Name", response.club?.name)

            // Verify changes persisted
            val retrieved = clubService.get(clubId, productionServerId)
            assertEquals("Updated Name", retrieved.name)
        } finally {
            // Cleanup
            try {
                clubService.delete(clubId, productionServerId)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    fun testDeleteClub() = runTest {
        // Given: a test club exists
        val createRequest = ClubCreateRequestDto(
            name = "Club To Delete",
            discordChannel = "777777777777777777",
            serverId = productionServerId
        )
        val created = clubService.create(createRequest)
        val clubId = created.club?.id
        assertNotNull(clubId)

        // When: deleting the club
        val response = clubService.delete(clubId, productionServerId)

        // Then: should return success
        assertTrue(response.success, "Club deletion should succeed")

        // Verify it no longer exists
        assertFailsWith<Exception> {
            clubService.get(clubId, productionServerId)
        }
    }

    @Test
    fun testCreateClubWithoutId() = runTest {
        var clubId: String? = null
        try {
            // Given: a club request (IDs are always server-generated)
            val request = ClubCreateRequestDto(
                name = "Auto ID Club",
                discordChannel = "666666666666666666",
                serverId = productionServerId
            )

            // When: creating the club
            val response = clubService.create(request)

            // Then: should generate an ID
            assertTrue(response.success == true)
            assertNotNull(response.club?.id, "Should have generated ID")
            clubId = response.club?.id

            // Verify it exists
            assertNotNull(clubId)
            val retrieved = clubService.get(clubId, productionServerId)
            assertEquals("Auto ID Club", retrieved.name)
        } finally {
            // Cleanup
            clubId?.let {
                try {
                    clubService.delete(it, productionServerId)
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    @Test
    fun testUpdateClubShameList() = runTest {
        // Given: a test club exists
        val createRequest = ClubCreateRequestDto(
            name = "Shame Test Club",
            discordChannel = "555555555555555555",
            serverId = productionServerId
        )
        val created = clubService.create(createRequest)
        val clubId = created.club?.id
        assertNotNull(clubId)

        try {
            // When: updating the shame list
            val updateRequest = ClubUpdateRequestDto(
                id = clubId,
                serverId = productionServerId,
                shameList = listOf(1, 2, 5)
            )
            val response = clubService.update(updateRequest)

            // Then: should update shame list
            assertTrue(response.success == true)
            assertTrue(response.shameListUpdated == true, "Shame list should be marked as updated")

            // Verify shame list persisted
            val retrieved = clubService.get(clubId, productionServerId)
            assertEquals(3, retrieved.shameList?.size)
            assertTrue(retrieved.shameList?.containsAll(listOf(1, 2, 5)) == true)
        } finally {
            // Cleanup
            try {
                clubService.delete(clubId, productionServerId)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Create a mock [HttpClient] using Ktor's [MockEngine] for testing.
     * TODO: Delete?
     */
    private fun createMockHttpClient(responseContent: String): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
}
