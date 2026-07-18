package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.JoinRequestDto
import com.ivangarzab.kluvs.data.remote.source.JoinRemoteDataSource
import com.ivangarzab.kluvs.model.ClubPreview

/**
 * Repository for the club invite-link join flow.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a signed-in user session is required.
 */
interface JoinRepository {

    /**
     * Previews the club behind an invite token, without joining it.
     *
     * @param token The invite token from the shared link
     * @return Result containing the [ClubPreview] if the token is valid,
     *         or an error if it is invalid or expired
     */
    suspend fun previewInvite(token: String): Result<ClubPreview>

    /**
     * Joins the club behind an invite token.
     *
     * @param token The invite token from the shared link
     * @return Result containing the joined club's ID if successful
     */
    suspend fun joinClub(token: String): Result<String>
}

internal class JoinRepositoryImpl(
    private val joinRemoteDataSource: JoinRemoteDataSource
) : JoinRepository {

    override suspend fun previewInvite(token: String): Result<ClubPreview> {
        return joinRemoteDataSource.previewInvite(token)
    }

    override suspend fun joinClub(token: String): Result<String> {
        Bark.d("Joining club via invite token")
        return joinRemoteDataSource.joinClub(JoinRequestDto(token = token))
    }
}
