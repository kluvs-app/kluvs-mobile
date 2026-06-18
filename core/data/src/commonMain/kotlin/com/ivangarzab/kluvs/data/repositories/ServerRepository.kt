package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.ServerCreateRequestDto
import com.ivangarzab.kluvs.api.models.ServerUpdateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.ServerLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.ServerRemoteDataSource
import com.ivangarzab.kluvs.model.Server
import com.ivangarzab.bark.Bark

/**
 * Repository for managing Server data with local caching.
 */
interface ServerRepository {

    /**
     * Retrieves a single server by its ID.
     *
     * @param serverId The ID of the server to retrieve
     * @param forceRefresh If true, bypasses cache and fetches fresh data from remote
     * @return Result containing the Server if successful, or an error if the operation failed
     */
    suspend fun getServer(serverId: String, forceRefresh: Boolean = false): Result<Server>

    /**
     * Retrieves all servers.
     *
     * @param forceRefresh If true, bypasses cache and fetches fresh data from remote
     * @return Result containing a list of all Servers if successful, or an error if the operation failed
     */
    suspend fun getAllServers(forceRefresh: Boolean = false): Result<List<Server>>

    /**
     * Creates a new server.
     *
     * @param name The name of the server to create
     * @return Result containing the created Server if successful, or an error if the operation failed
     */
    suspend fun createServer(name: String): Result<Server>

    /**
     * Updates an existing server.
     *
     * @param serverId The ID of the server to update
     * @param name Optional new name for the server (null to keep current value)
     * @return Result containing the updated Server if successful, or an error if the operation failed
     */
    suspend fun updateServer(serverId: String, name: String?): Result<Server>

    /**
     * Deletes a server by its ID.
     *
     * @param serverId The ID of the server to delete
     * @return Result containing success message if deletion was successful, or an error if the operation failed
     */
    suspend fun deleteServer(serverId: String): Result<String>
}

/**
 * Implementation of [ServerRepository] with local caching using TTL strategy.
 */
internal class ServerRepositoryImpl(
    private val serverRemoteDataSource: ServerRemoteDataSource,
    private val serverLocalDataSource: ServerLocalDataSource,
    private val cachePolicy: CachePolicy
) : ServerRepository {

    override suspend fun getServer(serverId: String, forceRefresh: Boolean): Result<Server> {
        if (!forceRefresh) {
            val cached = serverLocalDataSource.getServer(serverId)
            val lastFetchedAt = serverLocalDataSource.getLastFetchedAt(serverId)

            if (cached != null && cachePolicy.isFresh(lastFetchedAt, CacheTTL.SERVER)) {
                Bark.d("Cache hit for server (ID: $serverId)")
                return Result.success(cached)
            }
            Bark.d("Cache miss for server (ID: $serverId)")
        }

        Bark.d("Fetching server (ID: $serverId) from remote")
        val result = serverRemoteDataSource.getServer(serverId)

        result.onSuccess { server ->
            Bark.d("Caching server (ID: ${server.id})")
            try {
                serverLocalDataSource.insertServer(server)
                Bark.d("Successfully cached server (ID: ${server.id})")
            } catch (e: Exception) {
                Bark.e("Failed to cache server (ID: ${server.id}). Retry on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch server (ID: $serverId). Serving cached data if available.", error)
        }

        return result
    }

    override suspend fun getAllServers(forceRefresh: Boolean): Result<List<Server>> {
        Bark.d("Fetching all servers from remote")
        val result = serverRemoteDataSource.getAllServers()

        result.onSuccess { servers ->
            Bark.d("Caching ${servers.size} servers")
            try {
                servers.forEach { serverLocalDataSource.insertServer(it) }
                Bark.i("Successfully cached ${servers.size} servers")
            } catch (e: Exception) {
                Bark.e("Failed to cache servers. Retry on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch all servers. Serving cached data if available.", error)
        }

        return result
    }

    override suspend fun createServer(name: String): Result<Server> =
        serverRemoteDataSource.createServer(ServerCreateRequestDto(name = name))

    override suspend fun updateServer(serverId: String, name: String?): Result<Server> =
        serverRemoteDataSource.updateServer(ServerUpdateRequestDto(id = serverId, name = name))

    override suspend fun deleteServer(serverId: String): Result<String> =
        serverRemoteDataSource.deleteServer(serverId)
}
