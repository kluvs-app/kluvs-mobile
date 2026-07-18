package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.api.models.MemberCreateRequestDto
import com.ivangarzab.kluvs.api.models.MemberCreateResponseDto
import com.ivangarzab.kluvs.api.models.MemberGetResponseDto
import com.ivangarzab.kluvs.api.models.MemberUpdateRequestDto
import com.ivangarzab.kluvs.api.models.MemberUpdateResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface MemberService {
    suspend fun get(memberId: String): MemberGetResponseDto
    suspend fun getByUserId(userId: String): MemberGetResponseDto
    suspend fun create(request: MemberCreateRequestDto): MemberCreateResponseDto
    suspend fun update(request: MemberUpdateRequestDto): MemberUpdateResponseDto
    suspend fun delete(memberId: String): DeleteResponseDto
}

@OptIn(InternalAPI::class)
internal class MemberServiceImpl(private val supabase: SupabaseClient) : MemberService {

    override suspend fun get(memberId: String): MemberGetResponseDto {
        Bark.d("Fetching member (ID: $memberId)")
        return try {
            val response = supabase.functions.invoke("member") {
                method = HttpMethod.Get
                url { parameters.append("id", memberId) }
            }.body<MemberGetResponseDto>()
            Bark.v("Member fetched successfully (ID: $memberId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch member (ID: $memberId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun getByUserId(userId: String): MemberGetResponseDto {
        Bark.d("Fetching member by user ID (User: $userId)")
        return try {
            val response = supabase.functions.invoke("member") {
                method = HttpMethod.Get
                url { parameters.append("user_id", userId) }
            }.body<MemberGetResponseDto>()
            Bark.v("Member fetched by user ID successfully (User: $userId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch member by user ID (User: $userId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: MemberCreateRequestDto): MemberCreateResponseDto {
        Bark.d("Creating member")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("member") {
                method = HttpMethod.Post
                body = jsonString
            }.body<MemberCreateResponseDto>()
            Bark.v("Member created successfully")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create member. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: MemberUpdateRequestDto): MemberUpdateResponseDto {
        Bark.d("Updating member (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("member") {
                method = HttpMethod.Put
                body = jsonString
            }.body<MemberUpdateResponseDto>()
            Bark.v("Member updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update member (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(memberId: String): DeleteResponseDto {
        Bark.d("Deleting member (ID: $memberId)")
        return try {
            val response = supabase.functions.invoke("member") {
                method = HttpMethod.Delete
                url { parameters.append("id", memberId) }
            }.body<DeleteResponseDto>()
            Bark.v("Member deleted successfully (ID: $memberId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to delete member (ID: $memberId). Check network/API status and retry.", error)
            throw error
        }
    }
}
