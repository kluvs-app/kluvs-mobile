package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.ServerDto
import com.ivangarzab.kluvs.api.models.ServerGetListResponseDto
import com.ivangarzab.kluvs.api.models.ServerClubLatestSessionDto
import com.ivangarzab.kluvs.api.models.ServerClubSummaryDto
import com.ivangarzab.kluvs.api.models.ServerGetSingleResponseDto
import com.ivangarzab.kluvs.api.models.ServerCreateResponseDto
import com.ivangarzab.kluvs.api.models.ServerCreateRequestDto
import com.ivangarzab.kluvs.api.models.ServerUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.ServerService
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.api.models.ServerUpdateResponseDto
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerRemoteDataSourceTest {

    private lateinit var serverService: ServerService
    private lateinit var dataSource: ServerRemoteDataSource

    @BeforeTest
    fun setup() {
        serverService = mock<ServerService>()
        dataSource = ServerRemoteDataSourceImpl(serverService)
    }

    @Test
    fun `getAllServers success returns list of mapped Servers`() = runTest {
        // Given: Service returns the list-branch response
        val dto = ServerGetListResponseDto(
            servers = listOf(
                ServerDto(
                    id = "server-1",
                    name = "Production",
                    clubs = listOf(
                        com.ivangarzab.kluvs.api.models.ClubDto(
                            id = "club-1",
                            name = "Fiction",
                            discordChannel = "123456789"
                        )
                    )
                ),
                ServerDto(
                    id = "server-2",
                    name = "Test",
                    clubs = emptyList()
                )
            )
        )

        everySuspend { serverService.getAll() } returns dto

        // When: Getting all servers
        val result = dataSource.getAllServers()

        // Then: Result is success with list of servers
        assertTrue(result.isSuccess)
        val servers = result.getOrNull()!!
        assertEquals(2, servers.size)
        assertEquals("Production", servers[0].name)
        assertEquals(1, servers[0].clubs?.size)
        assertEquals("Test", servers[1].name)
        assertEquals(0, servers[1].clubs?.size)

        verifySuspend { serverService.getAll() }
    }

    @Test
    fun `getAllServers failure returns Result failure`() = runTest {
        // Given: Service throws exception
        val exception = Exception("Network error")
        everySuspend { serverService.getAll() } throws exception

        // When: Getting all servers
        val result = dataSource.getAllServers()

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        verifySuspend { serverService.getAll() }
    }

    @Test
    fun `getServer success returns mapped Server with clubs`() = runTest {
        // Given: Service returns the single-server branch response
        val dto = ServerGetSingleResponseDto(
            id = "server-1",
            name = "Main Server",
            clubs = listOf(
                ServerClubSummaryDto(
                    id = "club-1",
                    name = "Book Club",
                    discordChannel = "123456789",
                    memberCount = 15,
                    latestSession = ServerClubLatestSessionDto(
                        id = "session-1",
                        dueDate = null
                    )
                )
            )
        )

        everySuspend { serverService.get("server-1") } returns dto

        // When: Getting server
        val result = dataSource.getServer("server-1")

        // Then: Result is success with clubs
        assertTrue(result.isSuccess)
        val server = result.getOrNull()!!
        assertEquals("server-1", server.id)
        assertEquals("Main Server", server.name)
        assertEquals(1, server.clubs?.size)

        val club = server.clubs?.first()
        assertEquals("Book Club", club?.name)

        verifySuspend { serverService.get("server-1") }
    }

    @Test
    fun `getServer failure returns Result failure`() = runTest {
        // Given: Service throws exception
        val exception = Exception("Server not found")
        everySuspend { serverService.get("invalid") } throws exception

        // When: Getting server
        val result = dataSource.getServer("invalid")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        verifySuspend { serverService.get("invalid") }
    }

    @Test
    fun `createServer success returns created Server`() = runTest {
        // Given: Service returns success response
        val request = ServerCreateRequestDto(
            name = "New Server"
        )

        val responseDto = ServerCreateResponseDto(
            success = true,
            message = "Created",
            server = ServerDto(id = "server-3", name = "New Server")
        )

        everySuspend { serverService.create(request) } returns responseDto

        // When: Creating server
        val result = dataSource.createServer(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        val server = result.getOrNull()!!
        assertEquals("server-3", server.id)
        assertEquals("New Server", server.name)

        verifySuspend { serverService.create(request) }
    }

    @Test
    fun `updateServer success returns updated Server`() = runTest {
        // Given: Service returns success response
        val request = ServerUpdateRequestDto(
            id = "server-1",
            name = "Updated Name"
        )

        val responseDto = ServerUpdateResponseDto(
            success = true,
            message = "Updated",
            server = ServerDto(id = "server-1", name = "Updated Name")
        )

        everySuspend { serverService.update(request) } returns responseDto

        // When: Updating server
        val result = dataSource.updateServer(request)

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.name)

        verifySuspend { serverService.update(request) }
    }

    @Test
    fun `deleteServer success returns success message`() = runTest {
        // Given: Service returns success response
        val response = DeleteResponseDto(
            success = true,
            message = "Server deleted"
        )

        everySuspend { serverService.delete("server-1") } returns response

        // When: Deleting server
        val result = dataSource.deleteServer("server-1")

        // Then: Result is success
        assertTrue(result.isSuccess)
        assertEquals("Server deleted", result.getOrNull())

        verifySuspend { serverService.delete("server-1") }
    }

    @Test
    fun `deleteServer with success false returns failure`() = runTest {
        // Given: Service returns failure response
        val response = DeleteResponseDto(
            success = false,
            message = "Cannot delete server with clubs"
        )

        everySuspend { serverService.delete("server-1") } returns response

        // When: Deleting server
        val result = dataSource.deleteServer("server-1")

        // Then: Result is failure
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Cannot delete server with clubs") == true)

        verifySuspend { serverService.delete("server-1") }
    }

    @Test
    fun `getAllServers with empty list returns empty list`() = runTest {
        // Given: Service returns empty servers list
        val dto = ServerGetListResponseDto(servers = emptyList())

        everySuspend { serverService.getAll() } returns dto

        // When: Getting all servers
        val result = dataSource.getAllServers()

        // Then: Result is success with empty list
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)

        verifySuspend { serverService.getAll() }
    }
}
