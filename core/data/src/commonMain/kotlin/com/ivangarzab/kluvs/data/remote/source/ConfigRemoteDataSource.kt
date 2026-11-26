package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.remote.api.ConfigService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.ClientConfig

/**
 * Remote data source for the client configuration.
 *
 * Responsibilities:
 * - Calls [ConfigService] to fetch the config resolved for this app
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface ConfigRemoteDataSource {

    /**
     * Fetches the client config resolved for this installed app.
     */
    suspend fun getConfig(): Result<ClientConfig>
}

class ConfigRemoteDataSourceImpl(
    private val configService: ConfigService
) : ConfigRemoteDataSource {

    override suspend fun getConfig(): Result<ClientConfig> {
        return try {
            val config = configService.get().toDomain()
            Bark.d("Fetched client config (flags: ${config.flags.size})")
            Result.success(config)
        } catch (e: Exception) {
            Bark.w("Failed to fetch client config.", e)
            Result.failure(e)
        }
    }
}
