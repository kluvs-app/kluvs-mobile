package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.ClubService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.Club

/**
 * Remote data source for Club operations.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.ClubService] to fetch/mutate club data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface ClubRemoteDataSource {

    /**
     * Fetches a club by ID with optional server ID.
     *
     * Returns a [Club] with all nested relations populated:
     * - members (full Member objects)
     * - activeSession (full Session object)
     * - pastSessions (full Session objects)
     * - shameList (member IDs)
     *
     * @param clubId The ID of the club to retrieve
     * @param serverId Optional server ID for Discord integration (null for mobile-only clubs)
     */
    suspend fun getClub(clubId: String, serverId: String? = null): Result<Club>

    /**
     * Fetches a club by Discord channel ID and server ID.
     *
     * Returns a [Club] with all nested relations populated.
     */
    suspend fun getClubByChannel(channel: String, serverId: String): Result<Club>

    /**
     * Creates a new club.
     *
     * Returns the created [Club] (basic info only, no nested relations).
     */
    suspend fun createClub(request: ClubCreateRequestDto): Result<Club>

    /**
     * Updates an existing club.
     *
     * Returns the updated [Club] (basic info only, no nested relations).
     */
    suspend fun updateClub(request: ClubUpdateRequestDto): Result<Club>

    /**
     * Deletes a club by ID with optional server ID.
     *
     * Returns success message on successful deletion.
     *
     * @param clubId The ID of the club to delete
     * @param serverId Optional server ID for Discord integration (null for mobile-only clubs)
     */
    suspend fun deleteClub(clubId: String, serverId: String? = null): Result<String>
}

class ClubRemoteDataSourceImpl(
    private val clubService: ClubService
) : ClubRemoteDataSource {

    override suspend fun getClub(clubId: String, serverId: String?): Result<Club> {
        return try {
            val dto = clubService.get(clubId, serverId)
            Bark.i("Fetched club (ID: $clubId)")
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch club (ID: $clubId). Serving cached data if available.", e)
            Result.failure(e)
        }
    }

    override suspend fun getClubByChannel(channel: String, serverId: String): Result<Club> {
        return try {
            val dto = clubService.getByChannel(channel, serverId)
            Bark.i("Fetched club by channel (ID: $channel)")
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch club by channel (ID: $channel). Serving cached data if available.", e)
            Result.failure(e)
        }
    }

    override suspend fun createClub(request: ClubCreateRequestDto): Result<Club> {
        return try {
            val response = clubService.create(request)
            val club = response.club ?: error("Club creation response missing club data")
            Bark.i("Club created (name: ${request.name})")
            Result.success(club.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to create club. Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateClub(request: ClubUpdateRequestDto): Result<Club> {
        return try {
            val response = clubService.update(request)
            val club = response.club ?: error("Club update response missing club data")
            Bark.i("Club updated (ID: ${request.id})")
            Result.success(club.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to update club (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteClub(clubId: String, serverId: String?): Result<String> {
        return try {
            val response = clubService.delete(clubId, serverId)
            if (response.success) {
                Bark.i("Club deleted (ID: $clubId)")
                Result.success(response.message)
            } else {
                Result.failure(Exception("Delete failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Bark.e("Failed to delete club (ID: $clubId). Please retry.", e)
            Result.failure(e)
        }
    }
}
