package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSource
import com.ivangarzab.kluvs.model.Server
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ServerRepositoryTest {

    private lateinit var remoteDataSource: ServerRemoteDataSource
    private lateinit var localDataSource: ServerLocalDataSource
    private lateinit var cachePolicy: CachePolicy
    private lateinit var repository: ServerRepository

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<ServerRemoteDataSource>()
        localDataSource = mock<ServerLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = ServerRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getServer(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.getAllServers() } returns emptyList()
        everySuspend { localDataSource.insertServer(any()) } returns Unit
    }

    // ========================================
    // GET SERVER
    // ========================================

    @Test
    fun `getServer success returns Server with nested clubs`() = runTest {
        val serverId = "server-123"
        val expectedServer = Server(id = serverId, name = "Test Server", clubs = emptyList())
        everySuspend { remoteDataSource.getServer(serverId) } returns Result.success(expectedServer)

        val result = repository.getServer(serverId)

        assertTrue(result.isSuccess)
        assertEquals(expectedServer, result.getOrNull())
        verifySuspend { remoteDataSource.getServer(serverId) }
    }

    @Test
    fun `getServer failure returns Result failure`() = runTest {
        val serverId = "server-123"
        val exception = Exception("Server not found")
        everySuspend { remoteDataSource.getServer(serverId) } returns Result.failure(exception)

        val result = repository.getServer(serverId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getServer(serverId) }
    }

    // ========================================
    // GET ALL SERVERS
    // ========================================

    @Test
    fun `getAllServers success returns list of Servers`() = runTest {
        val expectedServers = listOf(
            Server(id = "server-1", name = "Server 1", clubs = emptyList()),
            Server(id = "server-2", name = "Server 2", clubs = emptyList())
        )
        everySuspend { remoteDataSource.getAllServers() } returns Result.success(expectedServers)

        val result = repository.getAllServers()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(expectedServers, result.getOrNull())
        verifySuspend { remoteDataSource.getAllServers() }
    }

    @Test
    fun `getAllServers returns empty list when no servers exist`() = runTest {
        everySuspend { remoteDataSource.getAllServers() } returns Result.success(emptyList())

        val result = repository.getAllServers()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
        verifySuspend { remoteDataSource.getAllServers() }
    }

    @Test
    fun `getAllServers failure returns Result failure`() = runTest {
        val exception = Exception("Network error")
        everySuspend { remoteDataSource.getAllServers() } returns Result.failure(exception)

        val result = repository.getAllServers()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.getAllServers() }
    }

    // ========================================
    // CREATE SERVER
    // ========================================

    @Test
    fun `createServer success creates server`() = runTest {
        val serverName = "New Server"
        val expectedServer = Server(id = "server-new", name = serverName, clubs = null)
        everySuspend { remoteDataSource.createServer(any()) } returns Result.success(expectedServer)

        val result = repository.createServer(name = serverName)

        assertTrue(result.isSuccess)
        assertEquals(expectedServer, result.getOrNull())
        assertEquals(serverName, result.getOrNull()?.name)
        verifySuspend { remoteDataSource.createServer(any()) }
    }

    @Test
    fun `createServer failure returns Result failure`() = runTest {
        val exception = Exception("Failed to create server")
        everySuspend { remoteDataSource.createServer(any()) } returns Result.failure(exception)

        val result = repository.createServer(name = "New Server")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.createServer(any()) }
    }

    // ========================================
    // UPDATE SERVER
    // ========================================

    @Test
    fun `updateServer with name updates server name`() = runTest {
        val serverId = "server-123"
        val newName = "Updated Server"
        val expectedServer = Server(id = serverId, name = newName, clubs = null)
        everySuspend { remoteDataSource.updateServer(any()) } returns Result.success(expectedServer)

        val result = repository.updateServer(serverId = serverId, name = newName)

        assertTrue(result.isSuccess)
        assertEquals(newName, result.getOrNull()?.name)
        verifySuspend { remoteDataSource.updateServer(any()) }
    }

    @Test
    fun `updateServer with null name does not update name`() = runTest {
        val serverId = "server-123"
        val expectedServer = Server(id = serverId, name = "Unchanged", clubs = null)
        everySuspend { remoteDataSource.updateServer(any()) } returns Result.success(expectedServer)

        val result = repository.updateServer(serverId = serverId, name = null)

        assertTrue(result.isSuccess)
        assertEquals("Unchanged", result.getOrNull()?.name)
        verifySuspend { remoteDataSource.updateServer(any()) }
    }

    @Test
    fun `updateServer failure returns Result failure`() = runTest {
        val exception = Exception("Failed to update server")
        everySuspend { remoteDataSource.updateServer(any()) } returns Result.failure(exception)

        val result = repository.updateServer(serverId = "server-123", name = "Updated")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.updateServer(any()) }
    }

    // ========================================
    // DELETE SERVER
    // ========================================

    @Test
    fun `deleteServer success returns success message`() = runTest {
        val serverId = "server-123"
        val successMessage = "Server deleted successfully"
        everySuspend { remoteDataSource.deleteServer(serverId) } returns Result.success(successMessage)

        val result = repository.deleteServer(serverId)

        assertTrue(result.isSuccess)
        assertEquals(successMessage, result.getOrNull())
        verifySuspend { remoteDataSource.deleteServer(serverId) }
    }

    @Test
    fun `deleteServer failure returns Result failure`() = runTest {
        val serverId = "server-123"
        val exception = Exception("Failed to delete server")
        everySuspend { remoteDataSource.deleteServer(serverId) } returns Result.failure(exception)

        val result = repository.deleteServer(serverId)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verifySuspend { remoteDataSource.deleteServer(serverId) }
    }

    @Test
    fun `deleteServer with non-existent server returns failure`() = runTest {
        val serverId = "non-existent"
        val exception = Exception("Server not found")
        everySuspend { remoteDataSource.deleteServer(serverId) } returns Result.failure(exception)

        val result = repository.deleteServer(serverId)

        assertTrue(result.isFailure)
        verifySuspend { remoteDataSource.deleteServer(serverId) }
    }

    // ========================================
    // KT-323: OFFLINE FALLBACK
    // ========================================

    @Test
    fun `getServer serves the cached server when the remote fetch fails`() = runTest {
        val cached = Server(id = "server-123", name = "Cached", clubs = emptyList())
        everySuspend { remoteDataSource.getServer("server-123") } returns Result.failure(Exception("offline"))
        everySuspend { localDataSource.getServer("server-123") } returns cached

        assertEquals(cached, repository.getServer("server-123", forceRefresh = true).getOrNull())
    }

    @Test
    fun `getAllServers serves the cached servers when the remote fetch fails`() = runTest {
        val cached = listOf(Server(id = "server-123", name = "Cached", clubs = emptyList()))
        everySuspend { remoteDataSource.getAllServers() } returns Result.failure(Exception("offline"))
        everySuspend { localDataSource.getAllServers() } returns cached

        assertEquals(cached, repository.getAllServers().getOrNull())
    }
}
