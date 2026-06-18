package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.ClubCreateResponseDto
import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateResponseDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface ClubService {
    suspend fun get(clubId: String, serverId: String? = null): ClubDto
    suspend fun getByChannel(channel: String, serverId: String): ClubDto
    suspend fun create(request: ClubCreateRequestDto): ClubCreateResponseDto
    suspend fun update(request: ClubUpdateRequestDto): ClubUpdateResponseDto
    suspend fun delete(clubId: String, serverId: String? = null): DeleteResponseDto
}

@OptIn(InternalAPI::class)
internal class ClubServiceImpl(private val supabase: SupabaseClient) : ClubService {

    override suspend fun get(clubId: String, serverId: String?): ClubDto {
        Bark.d("Fetching club (ID: $clubId, Server: $serverId)")
        return try {
            val response = supabase.functions.invoke("club") {
                method = HttpMethod.Get
                url {
                    parameters.append("id", clubId)
                    // Only append server_id if provided (Discord use case)
                    serverId?.let { parameters.append("server_id", it) }
                }
            }.body<ClubDto>()
            Bark.v("Club fetched successfully (ID: $clubId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch club (ID: $clubId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun getByChannel(channel: String, serverId: String): ClubDto {
        Bark.d("Fetching club by channel (Channel: $channel, Server: $serverId)")
        return try {
            val response = supabase.functions.invoke("club") {
                method = HttpMethod.Get
                url {
                    parameters.append("discord_channel", channel)
                    parameters.append("server_id", serverId)
                }
            }.body<ClubDto>()
            Bark.v("Club fetched by channel successfully (Channel: $channel)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch club by channel (Channel: $channel). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: ClubCreateRequestDto): ClubCreateResponseDto {
        Bark.d("Creating club")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("club") {
                method = HttpMethod.Post
                body = jsonString
            }.body<ClubCreateResponseDto>()
            Bark.v("Club created successfully")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create club. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: ClubUpdateRequestDto): ClubUpdateResponseDto {
        Bark.d("Updating club (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("club") {
                method = HttpMethod.Put
                body = jsonString
            }.body<ClubUpdateResponseDto>()
            Bark.v("Club updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update club (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(clubId: String, serverId: String?): DeleteResponseDto {
        Bark.d("Deleting club (ID: $clubId, Server: $serverId)")
        return try {
            val response = supabase.functions.invoke("club") {
                method = HttpMethod.Delete
                url {
                    parameters.append("id", clubId)
                    // Only append server_id if provided (Discord use case)
                    serverId?.let { parameters.append("server_id", it) }
                }
            }.body<DeleteResponseDto>()
            Bark.v("Club deleted successfully (ID: $clubId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to delete club (ID: $clubId). Check network/API status and retry.", error)
            throw error
        }
    }
}
