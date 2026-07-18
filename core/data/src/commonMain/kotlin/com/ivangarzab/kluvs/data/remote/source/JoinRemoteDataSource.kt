package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.JoinRequestDto
import com.ivangarzab.kluvs.data.remote.api.JoinService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.ClubPreview

/**
 * Remote data source for the club invite-link join flow.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.JoinService] to preview and redeem invite tokens
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface JoinRemoteDataSource {

    /**
     * Previews the club behind an invite token.
     *
     * Fails when the token is invalid or expired (the backend responds 404).
     */
    suspend fun previewInvite(token: String): Result<ClubPreview>

    /**
     * Joins the club behind an invite token, returning the joined club's ID.
     */
    suspend fun joinClub(request: JoinRequestDto): Result<String>
}

class JoinRemoteDataSourceImpl(
    private val joinService: JoinService
) : JoinRemoteDataSource {

    override suspend fun previewInvite(token: String): Result<ClubPreview> {
        return try {
            val response = joinService.preview(token)
            if (response.valid != true) {
                throw Exception("Invite token is not valid")
            }
            val club = response.club?.toDomain()
                ?: throw Exception("Invite preview succeeded but no club returned")
            Bark.d("Previewed invite for club: ${club.name}")
            Result.success(club)
        } catch (e: Exception) {
            Bark.e("Failed to preview invite. The link may be invalid or expired.", e)
            Result.failure(e)
        }
    }

    override suspend fun joinClub(request: JoinRequestDto): Result<String> {
        return try {
            val response = joinService.join(request)
            if (response.success == false) {
                throw Exception("Join request was rejected")
            }
            val clubId = response.clubId
                ?: throw Exception("Join succeeded but no club ID returned")
            Bark.i("Joined club (ID: $clubId)")
            Result.success(clubId)
        } catch (e: Exception) {
            Bark.e("Failed to join club. The link may be invalid or expired.", e)
            Result.failure(e)
        }
    }
}
