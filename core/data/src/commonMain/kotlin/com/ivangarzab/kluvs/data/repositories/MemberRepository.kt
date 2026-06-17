package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.MemberLocalDataSource
import com.ivangarzab.kluvs.data.remote.dtos.CreateMemberRequestDto
import com.ivangarzab.kluvs.data.remote.dtos.UpdateMemberRequestDto
import com.ivangarzab.kluvs.data.remote.source.MemberRemoteDataSource
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.bark.Bark

/**
 * Repository for managing Member data.
 *
 * This repository abstracts the data source layer and provides a clean API for accessing
 * member-related data. Currently delegates to remote data source, but can be extended
 * to support local caching and offline capabilities.
 */
interface MemberRepository {

    /**
     * Retrieves a single member by their ID.
     *
     * @param memberId The ID of the member to retrieve
     * @param forceRefresh If true, bypasses cache and fetches from remote
     * @return Result containing the Member (with nested clubs, shame clubs, etc.) if successful,
     *         or an error if the operation failed
     */
    suspend fun getMember(memberId: String, forceRefresh: Boolean = false): Result<Member>

    /**
     * Retrieves a member by their Discord user ID.
     *
     * @param userId The Discord user ID
     * @param forceRefresh If true, bypasses cache and fetches from remote
     * @return Result containing the Member if successful, or an error if the operation failed
     */
    suspend fun getMemberByUserId(userId: String, forceRefresh: Boolean = false): Result<Member>

    /**
     * Creates a new member.
     *
     * @param name The name of the member
     * @param userId Optional Discord user ID to associate with this member
     * @param role Optional role for the member
     * @param clubIds Optional list of club IDs to add this member to
     * @return Result containing the created Member if successful, or an error if the operation failed
     */
    suspend fun createMember(
        name: String,
        userId: String?,
        role: String?,
        clubIds: List<String>? = null
    ): Result<Member>

    /**
     * Updates an existing member.
     *
     * Uses PATCH semantics - only fields that are non-null will be updated.
     * Pass null for any field you want to leave unchanged.
     *
     * @param memberId The ID of the member to update
     * @param name Optional new name for the member (null to keep current value)
     * @param handle Optional new handle (null to keep current value)
     * @param userId Optional new Discord user ID (null to keep current value)
     * @param role Optional new role (null to keep current value)
     * @param booksRead Optional new books read count (null to keep current value)
     * @param avatarPath Optional new avatar path (null to keep current value)
     * @param clubIds Optional list of club IDs to set as the member's clubs (null to keep current clubs).
     *                When provided, this REPLACES all club memberships with the new list.
     * @return Result containing the updated Member if successful, or an error if the operation failed
     */
    suspend fun updateMember(
        memberId: String,
        name: String? = null,
        handle: String? = null,
        userId: String? = null,
        role: String? = null,
        booksRead: Int? = null,
        avatarPath: String? = null,
        clubIds: List<String>? = null,
        clubRoles: Map<String, String>? = null
    ): Result<Member>

    /**
     * Deletes a member by their ID.
     *
     * @param memberId The ID of the member to delete
     * @return Result containing success message if deletion was successful, or an error if the operation failed
     */
    suspend fun deleteMember(memberId: String): Result<String>
}

/**
 * Implementation of [MemberRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern:
 * - Read operations check local cache first (24h TTL)
 * - Cache misses fetch from remote and populate cache
 * - Write operations invalidate cache and delegate to remote
 *
 * Note: The API returns nested data (clubs, shame clubs) with Member responses.
 * Future implementations may decompose this nested data and coordinate with other
 * repositories for caching purposes.
 */
internal class MemberRepositoryImpl(
    private val memberRemoteDataSource: MemberRemoteDataSource,
    private val memberLocalDataSource: MemberLocalDataSource,
    private val cachePolicy: CachePolicy
) : MemberRepository {

    override suspend fun getMember(memberId: String, forceRefresh: Boolean): Result<Member> {
        if (!forceRefresh) {
            val cachedMember = memberLocalDataSource.getMember(memberId)
            val lastFetchedAt = memberLocalDataSource.getLastFetchedAt(memberId)

            if (cachedMember != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.MEMBER)) {
                Bark.d("Cache hit for member (ID: $memberId)")
                return Result.success(cachedMember)
            }
            Bark.d("Cache miss for member (ID: $memberId)")
        }

        Bark.d("Fetching member from remote (ID: $memberId)")
        val result = memberRemoteDataSource.getMember(memberId)

        result.onSuccess { member ->
            Bark.v("Persisting member to cache (ID: ${member.id})")
            try {
                memberLocalDataSource.insertMember(member)
                Bark.d("Member cached (ID: ${member.id})")
            } catch (e: Exception) {
                Bark.e("Member cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch member. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun getMemberByUserId(userId: String, forceRefresh: Boolean): Result<Member> {
        if (forceRefresh) {
            Bark.d("Forced cache refresh for user (ID: $userId)")
        }

        if (!forceRefresh) {
            val cachedMember = memberLocalDataSource.getMemberByUserId(userId)
            val lastFetchedAt = cachedMember?.let {
                memberLocalDataSource.getLastFetchedAt(it.id)
            }

            if (cachedMember != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.MEMBER)) {
                Bark.d("Cache hit for user (ID: $userId, Member: ${cachedMember.id})")
                return Result.success(cachedMember)
            }
            Bark.d("Cache miss for user (ID: $userId)")
        }

        Bark.d("Fetching member from remote (User ID: $userId)")
        val result = memberRemoteDataSource.getMemberByUserId(userId)

        result.onSuccess { member ->
            Bark.v("Persisting member to cache (ID: ${member.id})")
            try {
                memberLocalDataSource.insertMember(member)
                Bark.d("Member cached (ID: ${member.id})")
            } catch (e: Exception) {
                Bark.e("Member cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch member by user ID. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun createMember(
        name: String,
        userId: String?,
        role: String?,
        clubIds: List<String>?
    ): Result<Member> {
        Bark.d("Creating member: $name")
        val result = memberRemoteDataSource.createMember(
            CreateMemberRequestDto(
                name = name,
                user_id = userId,
                role = role,
                clubs = clubIds
            )
        )

        result.onSuccess { member ->
            Bark.v("Persisting new member to cache (ID: ${member.id})")
            try {
                memberLocalDataSource.insertMember(member)
                Bark.i("Member created and cached (ID: ${member.id})")
            } catch (e: Exception) {
                Bark.e("Member cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Member creation failed. Check input and retry.", error)
        }

        return result
    }

    override suspend fun updateMember(
        memberId: String,
        name: String?,
        handle: String?,
        userId: String?,
        role: String?,
        booksRead: Int?,
        avatarPath: String?,
        clubIds: List<String>?,
        clubRoles: Map<String, String>?
    ): Result<Member> {
        Bark.d("Updating member (ID: $memberId)")
        val result = memberRemoteDataSource.updateMember(
            UpdateMemberRequestDto(
                id = memberId,
                name = name,
                handle = handle,
                user_id = userId,
                role = role,
                books_read = booksRead,
                avatar_path = avatarPath,
                clubs = clubIds,
                club_roles = clubRoles
            )
        )

        // Note: intentionally NOT caching the update result. The API returns a partial
        // MemberDto (basic info only, fields like name/handle may be null), which would
        // corrupt the shared memberDao used by ClubLocalDataSource to load club members.
        // Fresh data is fetched via a force-refresh of club data after the mutation completes.
        result
            .onSuccess { member -> Bark.i("Member updated (ID: ${member.id})") }
            .onFailure { error -> Bark.e("Member update failed. Verify input and retry.", error) }

        return result
    }

    override suspend fun deleteMember(memberId: String): Result<String> {
        Bark.d("Deleting member (ID: $memberId)")
        val result = memberRemoteDataSource.deleteMember(memberId)

        result.onSuccess {
            Bark.v("Removing member from cache (ID: $memberId)")
            memberLocalDataSource.deleteMember(memberId)
            Bark.i("Member deleted (ID: $memberId)")
        }.onFailure { error ->
            Bark.e("Member deletion failed. Verify member exists and retry.", error)
        }

        return result
    }
}
