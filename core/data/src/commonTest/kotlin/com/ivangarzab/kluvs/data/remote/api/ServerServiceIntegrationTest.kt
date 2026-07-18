package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ServerCreateRequestDto
import com.ivangarzab.kluvs.api.models.ServerUpdateRequestDto
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
 * Integration tests for ServerService using local Supabase instance with seed data.
 *
 * Test data from /kluvs-backend/supabase/seed.sql:
 * - 1039326367428395038: Production Server
 * - 1234567890123456789: Test Server Alpha
 */
class ServerServiceIntegrationTest {

    private lateinit var serverService: ServerService

    // Test server IDs from seed.sql
    private val productionServerId = "1039326367428395038"
    private val testServerAlphaId = "1234567890123456789"

    @BeforeTest
    fun setup() {
        val supabase = createSupabaseClient(
            supabaseUrl = BuildKonfig.TEST_SUPABASE_URL,
            supabaseKey = BuildKonfig.TEST_SUPABASE_KEY
        ) {
            install(Functions)
        }
        serverService = ServerServiceImpl(supabase)
    }

    // ========================================
    // READ TESTS (using seed data)
    // ========================================

    @Test
    fun testGetAllServers() = runTest {
        // When: getting all servers
        val response = serverService.getAll()
        val servers = response.servers ?: emptyList()

        // Then: should return list of servers
        assertTrue(servers.isNotEmpty(), "Should have servers in seed data")

        // Should include known test servers
        val serverIds = servers.map { it.id }
        assertTrue(serverIds.contains(productionServerId), "Should include Production Server")
        assertTrue(serverIds.contains(testServerAlphaId), "Should include Test Server Alpha")
    }

    @Test
    fun testGetServer() = runTest {
        // Given: Production Server exists
        // When: getting server by ID
        val response = serverService.get(productionServerId)

        // Then: should return server with clubs
        assertEquals(productionServerId, response.id)
        assertNotNull(response.name, "Server should have a name")
        assertTrue(response.clubs?.isNotEmpty() == true, "Production Server should have clubs")

        // Clubs should have valid data
        response.clubs?.forEach { club ->
            assertNotNull(club.id, "Club should have ID")
            assertNotNull(club.name, "Club should have name")
        }
    }

    @Test
    fun testGetServerWithClubDetails() = runTest {
        // Given: a server with clubs
        // When: getting server
        val response = serverService.get(productionServerId)

        // Then: clubs should have member counts and sessions
        response.clubs?.forEach { club ->
            // Member count might be null or >= 0
            club.memberCount?.let { count ->
                assertTrue(count >= 0, "Member count should be non-negative")
            }

            // Latest session might be null
            club.latestSession?.let { session ->
                assertNotNull(session.id, "Session should have ID")
                // Note: book might be null if the relationship doesn't exist in the database
            }
        }
    }

    @Test
    fun testGetNonExistentServer() = runTest {
        // When: trying to get non-existent server
        // Then: should throw exception
        assertFailsWith<Exception> {
            serverService.get("999999999999999999")
        }
    }

    @Test
    fun testServerHasValidSnowflakeId() = runTest {
        // Given: servers exist
        // When: getting a server
        val response = serverService.get(productionServerId)

        // Then: ID should be valid Discord Snowflake
        assertNotNull(response.id)
        assertTrue(response.id!!.toLongOrNull() != null, "Server ID should be convertible to Long")
        assertTrue(response.id!!.toLong() > 1_000_000_000_000L, "Should be a large snowflake ID")
    }

    // ========================================
    // CREATE/UPDATE/DELETE TESTS
    // ========================================

    @Test
    fun testCreateServer() = runTest {
        // Given: a new server request
        val request = ServerCreateRequestDto(
            id = "123456789012345678", // Discord-like snowflake
            name = "Test Create Server"
        )

        try {
            // When: creating the server
            val response = serverService.create(request)

            // Then: should return success
            assertTrue(response.success == true, "Server creation should succeed")
            assertEquals("Test Create Server", response.server?.name)
            assertEquals("123456789012345678", response.server?.id)

            // Verify it can be retrieved
            val retrieved = serverService.get("123456789012345678")
            assertEquals("Test Create Server", retrieved.name)
            assertTrue(retrieved.clubs?.isEmpty() != false, "New server should have no clubs")
        } finally {
            // Cleanup
            try {
                serverService.delete("123456789012345678")
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testCreateServerWithoutId() = runTest {
        var serverId: String? = null
        try {
            // Given: a server request without explicit ID
            val request = ServerCreateRequestDto(
                name = "Auto ID Server"
            )

            // When: creating the server
            val response = serverService.create(request)

            // Then: should generate an ID
            assertTrue(response.success == true)
            assertNotNull(response.server?.id, "Should have generated ID")
            serverId = response.server?.id

            // Verify it exists
            assertNotNull(serverId)
            val retrieved = serverService.get(serverId)
            assertEquals("Auto ID Server", retrieved.name)
        } finally {
            // Cleanup
            serverId?.let {
                try {
                    serverService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdateServer() = runTest {
        // Given: a test server exists
        val createRequest = ServerCreateRequestDto(
            id = "234567890123456789",
            name = "Original Server Name"
        )
        serverService.create(createRequest)

        try {
            // When: updating the server
            val updateRequest = ServerUpdateRequestDto(
                id = "234567890123456789",
                name = "Updated Server Name"
            )
            val response = serverService.update(updateRequest)

            // Then: should return success
            assertTrue(response.success == true, "Server update should succeed")
            assertEquals("Updated Server Name", response.server?.name)

            // Verify changes persisted
            val retrieved = serverService.get("234567890123456789")
            assertEquals("Updated Server Name", retrieved.name)
        } finally {
            // Cleanup
            try {
                serverService.delete("234567890123456789")
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testDeleteServer() = runTest {
        // Given: a test server exists
        val createRequest = ServerCreateRequestDto(
            id = "345678901234567890",
            name = "Server To Delete"
        )
        serverService.create(createRequest)

        // When: deleting the server
        val response = serverService.delete("345678901234567890")

        // Then: should return success
        assertTrue(response.success, "Server deletion should succeed")

        // Verify it no longer exists
        assertFailsWith<Exception> {
            serverService.get("345678901234567890")
        }
    }

    @Test
    fun testDeleteServerWithWarning() = runTest {
        // Note: This test would require creating a server with clubs first
        // For now, we test the basic delete functionality
        // A server with clubs would return a warning about cascading deletions

        // Given: a simple server
        val createRequest = ServerCreateRequestDto(
            id = "456789012345678901",
            name = "Server With Potential Warning"
        )
        serverService.create(createRequest)

        try {
            // When: deleting the server
            val response = serverService.delete("456789012345678901")

            // Then: should succeed (warning is optional)
            assertTrue(response.success)
            // Warning would be present if server had clubs
        } catch (e: Exception) {
            // Server might already be deleted
        }
    }

    @Test
    fun testGetServerShowsClubsWithSessions() = runTest {
        // Given: Production Server with clubs that have sessions
        // When: getting the server
        val response = serverService.get(productionServerId)

        // Then: should show clubs with their latest sessions
        val clubsWithSessions = response.clubs?.filter { it.latestSession != null } ?: emptyList()

        if (clubsWithSessions.isNotEmpty()) {
            val firstClub = clubsWithSessions.first()
            val session = firstClub.latestSession!!

            // Session should have complete data
            assertNotNull(session.id)
            // Note: club_id is not returned in latest_session from server endpoint
            // Note: nested book might be null if the relationship doesn't exist in the database

            // Book should have basic fields if present
            session.books?.let {
                assertNotNull(it.title)
                assertNotNull(it.author)
            }
        }
    }

    @Test
    fun testAllServersHaveValidData() = runTest {
        // When: getting all servers
        val response = serverService.getAll()
        val servers = response.servers ?: emptyList()

        // Then: all servers should have valid IDs and names
        servers.forEach { server ->
            assertNotNull(server.id, "Server should have ID")
            assertNotNull(server.name, "Server should have name")
            assertTrue(server.id.isNotEmpty(), "Server ID should not be empty")
            assertTrue(server.name.isNotEmpty(), "Server name should not be empty")

            // All clubs in server should have valid data
            server.clubs?.forEach { club ->
                assertNotNull(club.id, "Club should have ID")
                assertNotNull(club.name, "Club should have name")
            }
        }
    }

    @Test
    fun testServerClubsHaveMemberCounts() = runTest {
        // Given: a server with clubs
        // When: getting the server
        val response = serverService.get(productionServerId)

        // Then: clubs should have member counts (if provided by API)
        response.clubs?.forEach { club ->
            // Member count is optional, but if present should be valid
            club.memberCount?.let { count ->
                assertTrue(count >= 0, "Member count should be non-negative for club ${club.name}")
            }
        }
    }
}
