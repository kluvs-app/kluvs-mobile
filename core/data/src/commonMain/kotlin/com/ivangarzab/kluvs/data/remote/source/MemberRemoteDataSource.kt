package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.MemberCreateRequestDto
import com.ivangarzab.kluvs.api.models.MemberUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.MemberService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.Member

/**
 * Remote data source for Member operations.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.MemberService] to fetch/mutate member data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface MemberRemoteDataSource {

    /**
     * Fetches a member by ID.
     *
     * Returns a [Member] with all nested relations populated:
     * - clubs (full Club objects the member belongs to)
     * - shameClubs (full Club objects where member is shamed)
     */
    suspend fun getMember(memberId: String): Result<Member>

    /**
     * Fetches a member by user ID (auth ID).
     *
     * Returns a [Member] with all nested relations populated.
     */
    suspend fun getMemberByUserId(userId: String): Result<Member>

    /**
     * Creates a new member.
     *
     * Returns the created [Member] (basic info only, no nested relations).
     */
    suspend fun createMember(request: MemberCreateRequestDto): Result<Member>

    /**
     * Updates an existing member.
     *
     * Returns the updated [Member] (basic info only, no nested relations).
     */
    suspend fun updateMember(request: MemberUpdateRequestDto): Result<Member>

    /**
     * Deletes a member by ID.
     *
     * Returns success message on successful deletion.
     */
    suspend fun deleteMember(memberId: String): Result<String>
}

class MemberRemoteDataSourceImpl(
    private val memberService: MemberService
) : MemberRemoteDataSource {

    override suspend fun getMember(memberId: String): Result<Member> {
        return try {
            val dto = memberService.get(memberId)
            Bark.i("Fetched member (ID: $memberId)")
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch member (ID: $memberId). Serving cached data if available.", e)
            Result.failure(e)
        }
    }

    override suspend fun getMemberByUserId(userId: String): Result<Member> {
        return try {
            val dto = memberService.getByUserId(userId)
            Bark.i("Fetched member by user ID (ID: $userId)")
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch member by user ID (ID: $userId). Serving cached data if available.", e)
            Result.failure(e)
        }
    }

    override suspend fun createMember(request: MemberCreateRequestDto): Result<Member> {
        return try {
            val response = memberService.create(request)
            val member = response.member ?: error("Member creation response missing member data")
            Bark.i("Member created (ID: ${member.id})")
            Result.success(member.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to create member. Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateMember(request: MemberUpdateRequestDto): Result<Member> {
        return try {
            val response = memberService.update(request)
            val member = response.member ?: error("Member update response missing member data")
            Bark.i("Member updated (ID: ${request.id})")
            Result.success(member.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to update member (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteMember(memberId: String): Result<String> {
        return try {
            val response = memberService.delete(memberId)
            if (response.success) {
                Bark.i("Member deleted (ID: $memberId)")
                Result.success(response.message)
            } else {
                Result.failure(Exception("Delete failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Bark.e("Failed to delete member (ID: $memberId). Please retry.", e)
            Result.failure(e)
        }
    }
}
