package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.ClubDto
import com.ivangarzab.kluvs.data.remote.dtos.ClubResponseDto
import com.ivangarzab.kluvs.data.remote.dtos.ServerClubDto
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.network.utils.parseDateOnlyString

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.ClubDto] from the API to a [Club] domain model.
 *
 * Note: ClubDto contains only basic club info without nested relations.
 * Relations (members, sessions) will be null.
 */
fun ClubDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discord_channel,
        serverId = server_id,
        foundedDate = parseDateOnlyString(founded_date),
        shameList = emptyList(),
        role = role?.let { Role.fromString(it) },
        members = null,
        activeSession = null,
        pastSessions = null
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.ClubResponseDto] from the API to a [Club] domain model.
 *
 * This is the full club response with all nested relations populated:
 * - members (list of Member objects)
 * - active_session (Session object)
 * - past_sessions (list of Session objects)
 * - shame_list (list of member IDs)
 */
fun ClubResponseDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discord_channel,
        serverId = server_id,
        foundedDate = parseDateOnlyString(founded_date),
        shameList = shame_list,
        role = null,
        // Map nested DTOs to domain models using their respective mappers
        members = members.map { it.toDomain() },
        activeSession = active_session?.toDomain(),
        // Filter out past sessions without book data (backend only returns id + due_date for past sessions)
        pastSessions = past_sessions.mapNotNull { session ->
            if (session.book != null) session.toDomain() else null
        }
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.ServerClubDto] from the API to a [Club] domain model.
 *
 * Note: ServerClubDto is used in Server responses and contains:
 * - Basic club info (id, name, discord_channel)
 * - member_count (not stored in domain)
 * - latest_session (mapped to activeSession)
 *
 * Other relations (members, pastSessions) will be null.
 */
fun ServerClubDto.toDomain(): Club {
    return Club(
        id = id,
        name = name,
        discordChannel = discord_channel,
        serverId = null, // Not available in ServerClubDto
        foundedDate = parseDateOnlyString(founded_date),
        shameList = emptyList(),
        role = null,
        members = null,
        activeSession = latest_session?.toDomain(),
        pastSessions = null
    )
}
