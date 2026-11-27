package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.remote.source.ConfigRemoteDataSource
import com.ivangarzab.kluvs.model.ClientConfig

/**
 * Repository for the backend's client configuration (feature flags and minimum supported version).
 *
 * Not cached: it is fetched once per launch and needs no sign-in.
 */
interface ConfigRepository {

    /**
     * Fetches the client config resolved for this installed app.
     *
     * @return Result containing the [ClientConfig], or an error if the request failed
     */
    suspend fun getConfig(): Result<ClientConfig>
}

internal class ConfigRepositoryImpl(
    private val configRemoteDataSource: ConfigRemoteDataSource
) : ConfigRepository {

    override suspend fun getConfig(): Result<ClientConfig> {
        return configRemoteDataSource.getConfig()
    }
}
