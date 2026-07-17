package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ServerGetListResponseDto
import com.ivangarzab.kluvs.api.models.ServerGetSingleResponseDto
import com.ivangarzab.kluvs.api.models.ServerCreateResponseDto
import com.ivangarzab.kluvs.api.models.ServerCreateRequestDto
import com.ivangarzab.kluvs.api.models.ServerUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ServerUpdateResponseDto
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface ServerService {
    suspend fun getAll(): ServerGetListResponseDto
    suspend fun get(serverId: String): ServerGetSingleResponseDto
    suspend fun create(request: ServerCreateRequestDto): ServerCreateResponseDto
    suspend fun update(request: ServerUpdateRequestDto): ServerUpdateResponseDto
    suspend fun delete(serverId: String): DeleteResponseDto
}

@OptIn(InternalAPI::class)
internal class ServerServiceImpl(private val supabase: SupabaseClient) : ServerService {

    override suspend fun getAll(): ServerGetListResponseDto {
        Bark.d("Fetching all servers")
        return try {
            val response = supabase.functions.invoke("server") {
                method = HttpMethod.Get
            }.body<ServerGetListResponseDto>()
            Bark.v("All servers fetched successfully (count: ${response.servers?.size ?: 0})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch all servers. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun get(serverId: String): ServerGetSingleResponseDto {
        Bark.d("Fetching server (ID: $serverId)")
        return try {
            val response = supabase.functions.invoke("server") {
                method = HttpMethod.Get
                url { parameters.append("id", serverId) }
            }.body<ServerGetSingleResponseDto>()
            Bark.v("Server fetched successfully (ID: $serverId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch server (ID: $serverId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: ServerCreateRequestDto): ServerCreateResponseDto {
        Bark.d("Creating server")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("server") {
                method = HttpMethod.Post
                body = jsonString
            }.body<ServerCreateResponseDto>()
            Bark.v("Server created successfully")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create server. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: ServerUpdateRequestDto): ServerUpdateResponseDto {
        Bark.d("Updating server (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("server") {
                method = HttpMethod.Put
                body = jsonString
            }.body<ServerUpdateResponseDto>()
            Bark.v("Server updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update server (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(serverId: String): DeleteResponseDto {
        Bark.d("Deleting server (ID: $serverId)")
        return try {
            val response = supabase.functions.invoke("server") {
                method = HttpMethod.Delete
                url { parameters.append("id", serverId) }
            }.body<DeleteResponseDto>()
            Bark.v("Server deleted successfully (ID: $serverId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to delete server (ID: $serverId). Check network/API status and retry.", error)
            throw error
        }
    }
}
