package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ClientConfigDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod

/**
 * Service for the client configuration endpoint.
 *
 * [get] needs no auth: the backend resolves the config from the `X-Kluvs-Client` header alone,
 * so it can be called before sign-in.
 */
interface ConfigService {
    suspend fun get(): ClientConfigDto
}

internal class ConfigServiceImpl(private val supabase: SupabaseClient) : ConfigService {

    override suspend fun get(): ClientConfigDto {
        Bark.d("Fetching client config")
        return try {
            val response = supabase.functions.invoke("config") {
                method = HttpMethod.Get
            }.body<ClientConfigDto>()
            Bark.v("Client config fetched (min supported version: ${response.minSupportedVersion})")
            response
        } catch (error: Exception) {
            Bark.w("Failed to fetch client config. Check network/API status.", error)
            throw error
        }
    }
}
