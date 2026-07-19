package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.api.models.MemberDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.ClubLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.ClubRemoteDataSource
import com.ivangarzab.kluvs.model.Club
import kotlinx.serialization.json.JsonObject

/**
 * Repository for managing Club data.
 *
 * This repository abstracts the data source layer and provides a clean API for accessing
 * club-related data. Currently delegates to remote data source, but can be extended
 * to support local caching and offline capabilities.
 */
interface ClubRepository {

    /**
     * Retrieves a single club by its ID with optional server ID.
     *
     * @param clubId The ID of the club to retrieve
     * @param serverId Optional server ID for Discord integration (defaults to null for mobile-only clubs)
     * @param forceRefresh If true, bypasses cache and fetches fresh data from remote
     * @return Result containing the Club (with nested members, sessions, etc.) if successful,
     *         or an error if the operation failed
     */
    suspend fun getClub(clubId: String, serverId: String? = null, forceRefresh: Boolean = false): Result<Club>

    /**
     * Creates a new club. The backend does not infer the caller as a member — the creator
     * must be passed explicitly as the first entry of the `members` array, which is how it
     * becomes the club's owner (see `club/handlers/create.ts`).
     *
     * @param name The name of the club
     * @param creatorMemberId The creating member's ID — becomes the club's owner
     * @param creatorMemberName The creating member's display name
     * @param creatorBooksRead The creating member's current books-read count (denormalized on the members row)
     * @param serverId Optional server ID for Discord integration (defaults to null for mobile-only clubs)
     * @param discordChannel Optional Discord channel to associate with this club
     * @return Result containing the created Club if successful, or an error if the operation failed
     */
    suspend fun createClub(
        name: String,
        creatorMemberId: String,
        creatorMemberName: String,
        creatorBooksRead: Int = 0,
        serverId: String? = null,
        discordChannel: String? = null
    ): Result<Club>

    /**
     * Updates an existing club.
     *
     * @param clubId The ID of the club to update
     * @param serverId Optional server ID for Discord integration (defaults to null for mobile-only clubs)
     * @param name Optional new name for the club (null to keep current value)
     * @param discordChannel Optional new Discord channel (null to keep current value)
     * @return Result containing the updated Club if successful, or an error if the operation failed
     */
    suspend fun updateClub(
        clubId: String,
        serverId: String? = null,
        name: String? = null,
        discordChannel: String? = null
    ): Result<Club>

    /**
     * Deletes a club by its ID.
     *
     * @param clubId The ID of the club to delete
     * @param serverId Optional server ID for Discord integration (defaults to null for mobile-only clubs)
     * @return Result containing success message if deletion was successful, or an error if the operation failed
     */
    suspend fun deleteClub(clubId: String, serverId: String? = null): Result<String>
}

/**
 * Implementation of [ClubRepository] with local caching.
 *
 * Implements TTL-based caching strategy:
 * 1. Checks local cache first (unless forceRefresh=true)
 * 2. Returns cached data if fresh (within TTL) AND complete
 * 3. Fetches from remote if cache is stale, missing, or incomplete
 * 4. Updates cache with fresh data from remote
 *
 * Caching Strategy:
 * - Caches complete club data including members, activeSession, book, and discussions
 * - Validates cache completeness (clubs without members are treated as incomplete)
 * - Incomplete clubs (e.g., those cached via Member responses) trigger a refresh
 */
internal class ClubRepositoryImpl(
    private val clubRemoteDataSource: ClubRemoteDataSource,
    private val clubLocalDataSource: ClubLocalDataSource,
    private val cachePolicy: CachePolicy
) : ClubRepository {

    override suspend fun getClub(clubId: String, serverId: String?, forceRefresh: Boolean): Result<Club> {
        // 1. Check local cache (unless force refresh)
        if (!forceRefresh) {
            val cachedClub = clubLocalDataSource.getClub(clubId)
            val lastFetchedAt = clubLocalDataSource.getLastFetchedAt(clubId)

            // Check if cached club is complete (has relationships loaded)
            // A complete club should have been fetched by ClubRepository and includes:
            // - members (may be empty list but should not be null for a complete fetch)
            // - activeSession (may be null if no active session)
            val isComplete = cachedClub?.let {
                // If members is null, this is an incomplete club (cached from Member response)
                it.members != null
            } ?: false

            if (cachedClub != null && isComplete && cachePolicy.isFresh(lastFetchedAt, CacheTTL.CLUB)) {
                Bark.d("Cache hit for club (ID: $clubId)")
                return Result.success(cachedClub)
            }
            Bark.d("Cache miss for club (ID: $clubId)")
        }

        // 2. Fetch from remote
        Bark.d("Fetching club (ID: $clubId) from remote")
        val result = clubRemoteDataSource.getClub(clubId, serverId)

        // 3. Cache on success
        result.onSuccess { club ->
            Bark.d("Caching club (ID: ${club.id})")
            try {
                clubLocalDataSource.insertClub(club)
                Bark.d("Successfully cached club (ID: ${club.id})")
            } catch (e: Exception) {
                Bark.e("Failed to cache club (ID: ${club.id}). Retry on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch club (ID: $clubId). Serving cached data if available.", error)
        }

        return result
    }

    override suspend fun createClub(
        name: String,
        creatorMemberId: String,
        creatorMemberName: String,
        creatorBooksRead: Int,
        serverId: String?,
        discordChannel: String?
    ): Result<Club> =
        clubRemoteDataSource.createClub(
            ClubCreateRequestDto(
                name = name,
                serverId = serverId,
                discordChannel = discordChannel,
                members = listOf(
                    MemberDto(
                        id = creatorMemberId.toIntOrNull() ?: 0,
                        name = creatorMemberName,
                        platformMetadata = JsonObject(emptyMap()),
                        booksRead = creatorBooksRead
                    )
                )
            )
        )

    override suspend fun updateClub(
        clubId: String,
        serverId: String?,
        name: String?,
        discordChannel: String?
    ): Result<Club> =
        clubRemoteDataSource.updateClub(
            ClubUpdateRequestDto(
                id = clubId,
                serverId = serverId,
                name = name,
                discordChannel = discordChannel
            )
        )

    override suspend fun deleteClub(clubId: String, serverId: String?): Result<String> =
        clubRemoteDataSource.deleteClub(clubId, serverId)
}
