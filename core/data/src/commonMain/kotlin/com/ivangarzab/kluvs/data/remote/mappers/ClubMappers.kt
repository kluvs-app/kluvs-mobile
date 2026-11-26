package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.bark.Bark
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
            ClubDetailDto.JoinPolicy.unknown_default_open_api -> unknownJoinPolicy(id)
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
        joinPolicy = joinPolicy.toDomain(id),
        inviteToken = inviteToken
    )
}

private fun ClubDto.JoinPolicy.toDomain(clubId: String): JoinPolicy = when (this) {
    ClubDto.JoinPolicy.PRIVATE -> JoinPolicy.PRIVATE
    ClubDto.JoinPolicy.INVITE_LINK -> JoinPolicy.INVITE_LINK
    ClubDto.JoinPolicy.unknown_default_open_api -> unknownJoinPolicy(clubId)
}

private fun MemberClubEntryDto.JoinPolicy.toDomain(clubId: String): JoinPolicy = when (this) {
    MemberClubEntryDto.JoinPolicy.PRIVATE -> JoinPolicy.PRIVATE
    MemberClubEntryDto.JoinPolicy.INVITE_LINK -> JoinPolicy.INVITE_LINK
    MemberClubEntryDto.JoinPolicy.unknown_default_open_api -> unknownJoinPolicy(clubId)
}

/**
 * Fallback for a join policy this app version doesn't know: [JoinPolicy.PRIVATE], the most
 * restrictive one, so the app never offers an invite link the server may not honor.
 * The generated serializer has already replaced the raw wire value, so it can't be logged.
 */
internal fun unknownJoinPolicy(clubId: String): JoinPolicy {
    Bark.w("Unknown join policy from API; falling back to PRIVATE (Club ID: $clubId)")
    return JoinPolicy.PRIVATE
}

private fun MemberClubEntryDto.Role.toDomain(clubId: String): Role = when (this) {
    MemberClubEntryDto.Role.unknown_default_open_api -> unknownRole("Club ID: $clubId")
    else -> Role.fromString(value)
}

/**
 * Fallback for a club role this app version doesn't know: [Role.MEMBER], the least
 * privileged one, so the app never shows owner/admin actions the server would reject.
 */
internal fun unknownRole(context: String): Role {
    Bark.w("Unknown club role from API; falling back to MEMBER ($context)")
    return Role.MEMBER
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
        role = role.toDomain(id),
        members = null,
        activeSession = null,
        pastSessions = null,
        joinPolicy = joinPolicy.toDomain(id),
        inviteToken = inviteToken
    )
}
