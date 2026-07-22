package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.MemberClubEntryDto
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.network.utils.parseDateOnlyString

/**
 * Maps a [ClubDto] from the API to a [Club] domain model.
 *
 * Note: [Club.role] is only ever populated when a club comes from inside a Member's
 * `clubs` list (see [MemberClubEntryDto.toDomain]); a standalone
 * club fetch always has a null role.
 *
 * Note: [Club.pastSessions] is left null — the API's `past_sessions` here is a
 * lightweight id+due_date shape with no book data, and [com.ivangarzab.kluvs.model.Session]
 * requires a non-null book, so it cannot be constructed from this response.
 */
fun ClubDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discordChannel,
        serverId = serverId,
        foundedDate = parseDateOnlyString(foundedDate),
        shameList = shameList?.map { it.toString() } ?: emptyList(),
        role = null,
        members = members?.map { it.toDomain() },
        activeSession = activeSession?.toDomain(),
        pastSessions = null,
        joinPolicy = joinPolicy.toDomain(),
        inviteToken = inviteToken
    )
}

private fun ClubDto.JoinPolicy.toDomain(): JoinPolicy = when (this) {
    ClubDto.JoinPolicy.PRIVATE -> JoinPolicy.PRIVATE
    ClubDto.JoinPolicy.INVITE_LINK -> JoinPolicy.INVITE_LINK
}

private fun MemberClubEntryDto.JoinPolicy.toDomain(): JoinPolicy = when (this) {
    MemberClubEntryDto.JoinPolicy.PRIVATE -> JoinPolicy.PRIVATE
    MemberClubEntryDto.JoinPolicy.INVITE_LINK -> JoinPolicy.INVITE_LINK
}

/**
 * Maps a [MemberClubEntryDto] (a club embedded within a member's
 * `clubs` list, including the member's per-club role) to a [Club] domain model.
 *
 * This is the only context where [Club.role] gets populated from real data.
 */
fun MemberClubEntryDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discordChannel,
        serverId = serverId,
        foundedDate = parseDateOnlyString(foundedDate),
        shameList = shameList?.map { it.toString() } ?: emptyList(),
        role = role?.let { Role.fromString(it.value) },
        members = members?.map { it.toDomain() },
        activeSession = activeSession?.toDomain(),
        pastSessions = null,
        joinPolicy = joinPolicy.toDomain(),
        inviteToken = inviteToken
    )
}
