package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubDetailDto
import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.MemberClubEntryDto
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.network.utils.parseDateOnlyString

/**
 * Maps a [ClubDetailDto] (the full `GET /club` response) to a [Club] domain model.
 *
 * This is the only club shape that carries `members` and `active_session`, so it's the
 * only one that produces a "complete" [Club] (non-null [Club.members]) for the cache.
 *
 * Note: [Club.role] is only ever populated when a club comes from inside a Member's
 * `clubs` list (see [MemberClubEntryDto.toDomain]); a standalone club fetch always has
 * a null role.
 *
 * Note: [Club.pastSessions] is left null — the API's `past_sessions` here is a
 * lightweight id+due_date shape with no book data, and [com.ivangarzab.kluvs.model.Session]
 * requires a non-null book, so it cannot be constructed from this response.
 */
fun ClubDetailDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discordChannel,
        serverId = serverId,
        foundedDate = parseDateOnlyString(foundedDate),
        shameList = shameList?.map { it.toString() } ?: emptyList(),
        role = null,
        members = members.map { it.toDomain() },
        activeSession = activeSession?.toDomain(),
        pastSessions = null,
        joinPolicy = when (joinPolicy) {
            ClubDetailDto.JoinPolicy.PRIVATE -> JoinPolicy.PRIVATE
            ClubDetailDto.JoinPolicy.INVITE_LINK -> JoinPolicy.INVITE_LINK
        },
        inviteToken = inviteToken
    )
}

/**
 * Maps a [ClubDto] (the plain club shape, e.g. a member's shame clubs or a club
 * create/update echo) to a [Club] domain model. It has no members or sessions, so
 * [Club.members] stays null and the club counts as incomplete for the cache.
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
        members = null,
        activeSession = null,
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
        role = Role.fromString(role.value),
        members = null,
        activeSession = null,
        pastSessions = null,
        joinPolicy = joinPolicy.toDomain(),
        inviteToken = inviteToken
    )
}
